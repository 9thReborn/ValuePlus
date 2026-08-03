package com.nitax.valueplusbackend.service.impl;

import com.nitax.valueplusbackend.config.FraudRuleProperties;
import com.nitax.valueplusbackend.domain.*;
import com.nitax.valueplusbackend.domain.SubscriberEvent.EventType;
import com.nitax.valueplusbackend.dto.request.SubscriberEventFilter;
import com.nitax.valueplusbackend.dto.request.SubscriptionWebhookRequest;
import com.nitax.valueplusbackend.dto.request.UnsubscribeRequest;
import com.nitax.valueplusbackend.dto.response.SubscriberDetailDTO;
import com.nitax.valueplusbackend.dto.response.SubscriberDetailDTO.BillingInfo;
import com.nitax.valueplusbackend.exception.AppException;
import com.nitax.valueplusbackend.repository.NotificationRepository;
import com.nitax.valueplusbackend.repository.SubscriberEventRepository;
import com.nitax.valueplusbackend.repository.SubscriberRepository;
import com.nitax.valueplusbackend.service.*;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import com.nitax.valueplusbackend.utils.MsisdnUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriberServiceImpl implements SubscriberService {

  private final SubscriberRepository subscriberRepository;
  private final SubscriberEventRepository subscriberEventRepository;
  private final NotificationRepository notificationRepository;
  private final CampaignService campaignService;
  private final ClicksConversionsService clicksConversionsService;
  private final AdvertiserService advertiserService;
  private final ConversionDecisionService conversionDecisionService;
  private final FraudRuleProperties fraudRuleProperties;
  private final BlocklistService blocklistService;

  @Override
  @Transactional
  public Subscriber processSubscriptionWebhook(
      SubscriptionWebhookRequest request, String rawPayload) {
    log.info(
        "Processing subscription webhook: msisdn={}, eventType={}, trxId={}",
        request.getMsisdn(),
        request.getEventType(),
        request.getTrxId());

      String normalizedMsisdn = MsisdnUtil.normalize(request.getMsisdn());
      if (!java.util.Objects.equals(normalizedMsisdn, request.getMsisdn())) {
          log.info("Normalized msisdn {} -> {}", request.getMsisdn(), normalizedMsisdn);
      }
      request.setMsisdn(normalizedMsisdn);

    String rawTrxId = request.getTrxId();
    String[] trxIdParts = rawTrxId.split("_");
    String campaignId;
    String publisherId;
    Optional<Notification> clickRecord =
        (rawTrxId != null && !rawTrxId.contains("_"))
            ? notificationRepository.findFirstByShortTrxIdAndStatusOrderByCreatedDateDesc(
                rawTrxId, Notification.NotificationStatus.PUBLISHER_HOOK_RECEIVED)
            : Optional.empty();

    if (clickRecord.isPresent()) {
      campaignId = clickRecord.get().getCampaignId();
      publisherId = clickRecord.get().getPublisherId();
    } else {
      campaignId = trxIdParts.length > 1 ? trxIdParts[0] : request.getCampaignId();
      publisherId = trxIdParts.length > 1 ? trxIdParts[1] : null;
    }

    log.info("Extracted campaignId={}, publisherId={} from trxId", campaignId, publisherId);

    // Parse event type
    EventType eventType = parseEventType(request.getEventType());

    // Generate idempotency key from trxId + eventType + timestamp
    String idempotencyKey = generateIdempotencyKey(request, eventType);

    // Check for duplicate event
    if (subscriberEventRepository.existsByIdempotencyKey(idempotencyKey)) {
      log.info("Duplicate webhook detected, idempotencyKey={}", idempotencyKey);
      throw new AppException("Duplicate webhook event");
    }

    // Find or create subscriber
    Subscriber subscriber = findOrCreateSubscriber(request, campaignId, publisherId);

    // Save subscriber
    subscriber = subscriberRepository.save(subscriber);

      FraudRuleOutcome fraudOutcome =
              evaluateFraudRules(request.getMsisdn(), request.getServiceId(), eventType, true);
      ValidationDecision decision = fraudOutcome.decision();
      ReasonCode reasonCode = fraudOutcome.reasonCode();
      String decisionMessage = fraudOutcome.message();

    // Create and save event with idempotency key
    SubscriberEvent event = createEvent(subscriber, eventType, request, rawPayload, idempotencyKey);
    subscriberEventRepository.save(event);

    conversionDecisionService.recordDecision(event, publisherId, decision, reasonCode, decisionMessage);

    log.info(
        "Subscription webhook processed: subscriberId={}, eventType={}, idempotencyKey={}",
        subscriber.getId(),
        eventType,
        idempotencyKey);

    // Register conversion and send to publisher (only for ACTIVATION events with renewalFlag enabled)
    // and only if no fraud rule blocked this event
    boolean hasUsableTrxId = trxIdParts.length > 1 || clickRecord.isPresent();
      if (eventType == EventType.DEACTIVATION) {
          UnsubscribeRequest unsubscribeRequest = new UnsubscribeRequest();
          unsubscribeRequest.setMsisdn(request.getMsisdn());
          unsubscribeRequest.setClickId(request.getTrxId());
          DateTimeFormatter formatter =
                  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);
          unsubscribeRequest.setUnsubscribeDateTime(formatter.format(Instant.now()));
          advertiserService.handleUnsubscription(unsubscribeRequest);

          subscriber.setStatus(Subscriber.SubscriberStatus.CHURNED);
          subscriberRepository.save(subscriber);
      } else if (decision == ValidationDecision.BLOCK) {
          log.info(
                  "Skipping publisher postback: event blocked by fraud rule, reasonCode={}, trxId={}",
                  reasonCode,
                  request.getTrxId());
      } else if (hasUsableTrxId && eventType == EventType.ACTIVATION) {
          if (Boolean.FALSE.equals(request.getRenewalFlag()) && !campaignId.equals("vly9DZ1Qv0")) {
              log.info(
                      "Skipping publisher notification: renewalFlag is false for trxId={}",
                      request.getTrxId());
          } else {
              String sourceId = extractSourceId(request.getTrxId());
              clicksConversionsService.handleAdvertiserPostbackByGET2(
                      request.getTrxId(), sourceId, request.getMsisdn(), "Success", "1");
              log.info("Conversion processing triggered via handleAdvertiserPostbackByGET2");
          }
      }

      return subscriber;
  }

    private record FraudRuleOutcome(ValidationDecision decision, ReasonCode reasonCode, String message) {}

    private FraudRuleOutcome evaluateFraudRules(
            String msisdn, String serviceId, EventType eventType, boolean enforce) {
        if (eventType == EventType.ACTIVATION) {
            Optional<Blocklist> activeBlock = blocklistService.findActiveGlobalBlock(msisdn);
            if (activeBlock.isPresent()) {
                Blocklist block = activeBlock.get();
                String message =
                        "Global Block: msisdn "
                                + msisdn
                                + " under active global block (id="
                                + block.getId()
                                + ", reason="
                                + block.getReasonCode()
                                + ") until "
                                + (block.getExpiresAt() != null ? block.getExpiresAt() : "released manually");
                log.info(
                        "Rule B BLOCK: msisdn={}, blockId={}, expiresAt={}",
                        msisdn,
                        block.getId(),
                        block.getExpiresAt());
                return new FraudRuleOutcome(ValidationDecision.BLOCK, ReasonCode.GLOBAL_CHURN_LOOP, message);
            }
            Optional<SubscriberEvent> priorActivation = findRecentSameServiceActivation(msisdn, serviceId);
            if (priorActivation.isPresent()) {
                SubscriberEvent prior = priorActivation.get();
                String message =
                        "Duplicate Block: msisdn "
                                + msisdn
                                + " already subscribed to service "
                                + serviceId
                                + " at "
                                + prior.getEventTimestamp()
                                + ", inside "
                                + fraudRuleProperties.getSameServiceCooldownHours()
                                + "h cooldown";
                log.info(
                        "Rule A BLOCK: msisdn={}, serviceId={}, previousActivationAt={}",
                        msisdn,
                        serviceId,
                        prior.getEventTimestamp());
                return new FraudRuleOutcome(ValidationDecision.BLOCK, ReasonCode.DUPLICATE_SERVICE_SUB, message);
            }
        }else if (eventType == EventType.DEACTIVATION) {
            List<SubscriberEvent> priorChurns = findRecentChurns(msisdn);
            if (!priorChurns.isEmpty()) {
                if (enforce) {
                    Blocklist block =
                            blocklistService.createOrRefreshGlobalBlock(
                                    msisdn, ReasonCode.GLOBAL_CHURN_LOOP, "SYSTEM:Global Block");
                    String message =
                            "Rule B: msisdn "
                                    + msisdn
                                    + " churned "
                                    + (priorChurns.size() + 1)
                                    + " time(s) within "
                                    + fraudRuleProperties.getChurnFrequencyWindowHours()
                                    + "h; global block "
                                    + (block.getId() != null ? "id=" + block.getId() : "")
                                    + " active until "
                                    + block.getExpiresAt();
                    log.info(
                            "Global Block triggered: msisdn={}, churnCountInWindow={}, blockId={}, blockExpiresAt={}",
                            msisdn,
                            priorChurns.size() + 1,
                            block.getId(),
                            block.getExpiresAt());
                    return new FraudRuleOutcome(ValidationDecision.BLOCK, ReasonCode.GLOBAL_CHURN_LOOP, message);
                }else {
                    String message =
                            "Global Block: msisdn "
                                    + msisdn
                                    + " churned "
                                    + (priorChurns.size() + 1)
                                    + " time(s) within "
                                    + fraudRuleProperties.getChurnFrequencyWindowHours()
                                    + "h; would trigger a global block if this were live (replay — no block written)";
                    log.info(
                            "Global block wasn't triggerred (no write): msisdn={}, churnCountInWindow={}",
                            msisdn,
                            priorChurns.size() + 1);
                    return new FraudRuleOutcome(ValidationDecision.BLOCK, ReasonCode.GLOBAL_CHURN_LOOP, message);
                }
            }
        }
        return new FraudRuleOutcome(ValidationDecision.ALLOW, ReasonCode.NONE, "No fraud rule triggered");
    }
    private Optional<SubscriberEvent> findRecentSameServiceActivation(String msisdn, String serviceId) {
        Instant cooldownStart =
                Instant.now().minus(Duration.ofHours(fraudRuleProperties.getSameServiceCooldownHours()));
        List<SubscriberEvent> priorActivations =
                subscriberEventRepository.findRecentActivationsForMsisdnAndService(
                        msisdn, serviceId, cooldownStart);
        return priorActivations.isEmpty() ? Optional.empty() : Optional.of(priorActivations.get(0));
    }

    private List<SubscriberEvent> findRecentChurns(String msisdn) {
        Instant windowStart =
                Instant.now().minus(Duration.ofHours(fraudRuleProperties.getChurnFrequencyWindowHours()));
        return subscriberEventRepository.findRecentDeactivationsForMsisdn(msisdn, windowStart);
    }

  /** Extract sourceId from trxId if present (format: ...SRCID{sourceId}) */
  private String extractSourceId(String trxId) {
    if (trxId != null && trxId.contains("SRCID")) {
      String[] parts = trxId.split("SRCID");
      if (parts.length > 1) {
        return parts[1];
      }
    }
    return "";
  }

  private String generateIdempotencyKey(SubscriptionWebhookRequest request, EventType eventType) {
    // Key format: trxId:eventType:timestamp or msisdn:serviceId:eventType:timestamp if no trxId
    String baseKey;
    if (request.getTrxId() != null && !request.getTrxId().isEmpty()) {
      baseKey = request.getTrxId() + ":" + eventType.name();
    } else {
      baseKey = request.getMsisdn() + ":" + request.getServiceId() + ":" + eventType.name();
    }

    // Add timestamp if provided for more granular deduplication
    if (request.getTimestamp() != null && !request.getTimestamp().isEmpty()) {
      baseKey += ":" + request.getTimestamp();
    }

    return baseKey;
  }

  @Override
  @Async
  public void backfillMissingUnsubscribedNotifications() {
    log.info("Starting backfill of missing UNSUBSCRIBED notifications for deactivation events");

    DateTimeFormatter formatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

    int pageSize = 500;
    int pageNumber = 0;
    int totalSuccess = 0;
    int totalFailed = 0;

    while (true) {
      Page<SubscriberEvent> page =
          subscriberEventRepository.findDeactivationsWithoutUnsubscribedNotification(
              PageRequest.of(pageNumber, pageSize));

      if (page.isEmpty()) break;

      log.info("Processing batch {}/{} ({} records)", pageNumber + 1, page.getTotalPages(), page.getNumberOfElements());

      for (SubscriberEvent event : page.getContent()) {
        try {
          UnsubscribeRequest request = new UnsubscribeRequest();
          request.setMsisdn(event.getSubscriber().getMsisdn());
          request.setClickId(event.getSubscriber().getTrxId());
          request.setUnsubscribeDateTime(
              formatter.format(
                  event.getEventTimestamp() != null ? event.getEventTimestamp() : Instant.now()));
          advertiserService.handleUnsubscription(request);
          totalSuccess++;
        } catch (Exception e) {
          log.error(
              "Failed to backfill notification for msisdn={}, eventId={}: {}",
              event.getSubscriber().getMsisdn(),
              event.getId(),
              e.getMessage());
          totalFailed++;
        }
      }

      if (!page.hasNext()) break;
      pageNumber++;

      try {
        Thread.sleep(1000);
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
        log.warn("Backfill interrupted at batch {}", pageNumber);
        break;
      }
    }

    log.info("Backfill complete. totalSuccess={}, totalFailed={}", totalSuccess, totalFailed);
  }

  @Override
  public Page<SubscriberEvent> searchEvents(SubscriberEventFilter filter, Pageable pageable) {
    return subscriberEventRepository.findByFilters(
        filter.getAdvertiserId(),
        filter.getMsisdn(),
        filter.getServiceId(),
        filter.getEventType(),
        filter.getStartDate(),
        filter.getEndDate(),
        pageable);
  }

    @Override
    @Transactional
    public ConversionDecision replayEvent(Long subscriberEventId) {
        SubscriberEvent event =
                subscriberEventRepository
                        .findById(subscriberEventId)
                        .orElseThrow(() -> new AppException("Subscriber event not found: " + subscriberEventId));

        log.info(
                "Replaying event {} (msisdn={}, eventType={}) for investigation — read-only, no side effects",
                event.getId(),
                event.getSubscriber().getMsisdn(),
                event.getEventType());

        FraudRuleOutcome fraudOutcome =
                evaluateFraudRules(
                        event.getSubscriber().getMsisdn(),
                        event.getSubscriber().getServiceId(),
                        event.getEventType(),
                        false);

        return conversionDecisionService.recordReplayDecision(
                event,
                event.getSubscriber().getPublisherId(),
                fraudOutcome.decision(),
                fraudOutcome.reasonCode(),
                "Replay recompute: " + fraudOutcome.message());
    }

    @Override
    @Transactional
    public ConversionDecision replayEventByMsisdn(String msisdn) {
        String normalizedMsisdn = MsisdnUtil.normalize(msisdn);
        List<SubscriberEvent> events =
                subscriberEventRepository.findAllByMsisdnOrderByEventTimestampDesc(normalizedMsisdn);
        if (events.isEmpty()) {
            throw new AppException("No subscriber events found for msisdn: " + msisdn);
        }

        SubscriberEvent mostRecent = events.get(0);
        log.info(
                "Resolved msisdn {} -> most recent SubscriberEvent id={} (eventType={}, eventTimestamp={}) for replay",
                normalizedMsisdn,
                mostRecent.getId(),
                mostRecent.getEventType(),
                mostRecent.getEventTimestamp());


        return replayEvent(mostRecent.getId());
    }

  @Override
  public SubscriberDetailDTO getSubscriberDetail(Long subscriberId) {
    Subscriber subscriber =
        subscriberRepository
            .findById(subscriberId)
            .orElseThrow(() -> new AppException("Subscriber not found"));

    // Get events
    List<SubscriberEvent> events =
        subscriberEventRepository.findBySubscriberIdOrderByEventTimestampAsc(subscriberId);

    // Get billing history from notifications table
    List<Notification> notifications =
        notificationRepository.findByMsisdnOrderByCreatedDateDesc(subscriber.getMsisdn());

    List<BillingInfo> billingHistory = notifications.stream().map(this::mapToBillingInfo).toList();

    return SubscriberDetailDTO.builder()
        .subscriber(subscriber)
        .events(events)
        .billingHistory(billingHistory)
        .billingSummary(null)
        .build();
  }

  private BillingInfo mapToBillingInfo(Notification notification) {
    return BillingInfo.builder()
        .id(notification.getId())
        .transactionId(notification.getTransactionId())
        .status(notification.getStatus().name())
        .campaignId(notification.getCampaignId())
        .publisherId(notification.getPublisherId())
        .cpaRevenue(notification.getCpaRevenue())
        .vpRevenue(notification.getVpRevenue())
        .activation(notification.getActivation())
        .createdDate(notification.getCreatedDate())
        .unsubscribeTimestamp(notification.getUnsubscribeTimestamp())
        .duration(notification.getDuration())
        .build();
  }

  private Subscriber findOrCreateSubscriber(
      SubscriptionWebhookRequest request, String campaignId, String publisherId) {
    Optional<Subscriber> existingSubscriber =
        subscriberRepository.findByMsisdnAndServiceId(request.getMsisdn(), request.getServiceId());

    if (existingSubscriber.isPresent()) {
      Subscriber subscriber = existingSubscriber.get();
      // Update trxId if provided and not already set
      if (request.getTrxId() != null
          && !request.getTrxId().isEmpty()
          && (subscriber.getTrxId() == null || subscriber.getTrxId().isEmpty())) {
        subscriber.setTrxId(request.getTrxId());
      }
      // Update campaignId and publisherId
      subscriber.setCampaignId(campaignId);
      if (publisherId != null) {
        subscriber.setPublisherId(publisherId);
      }
      // Update advertiserId from campaign
      Campaign campaign = campaignService.findCampaignById(campaignId).orElse(null);
      if (campaign != null && campaign.getAdvertiser() != null) {
        subscriber.setAdvertiserId(campaign.getAdvertiser().getId().toString());
      }
      return subscriber;
    }

    // Create new subscriber
    Subscriber subscriber = new Subscriber();
    subscriber.setMsisdn(request.getMsisdn());
    subscriber.setServiceId(request.getServiceId());
    subscriber.setTrxId(request.getTrxId());
    subscriber.setAutoRenew(request.getRenewalFlag());
    subscriber.setCampaignId(campaignId);
    if (publisherId != null) {
      subscriber.setPublisherId(publisherId);
    }

    // Get advertiserId from campaign
    Campaign campaign = campaignService.findCampaignById(campaignId).orElse(null);
    if (campaign != null && campaign.getAdvertiser() != null) {
      subscriber.setAdvertiserId(campaign.getAdvertiser().getId().toString());
    }

    return subscriber;
  }

  private SubscriberEvent createEvent(
      Subscriber subscriber,
      EventType eventType,
      SubscriptionWebhookRequest request,
      String rawPayload,
      String idempotencyKey) {
    SubscriberEvent event = new SubscriberEvent();
    event.setSubscriber(subscriber);
    event.setEventType(eventType);
    event.setPayloadJson(rawPayload);
    event.setIdempotencyKey(idempotencyKey);

    // Set revenue fields
    event.setBillingAmount(request.getBillingAmount());
    event.setCurrency(request.getCurrency());
    event.setBillingCycle(request.getBillingCycle());

    // Parse timestamp
    if (request.getTimestamp() != null && !request.getTimestamp().isEmpty()) {
      try {
        event.setEventTimestamp(Instant.parse(request.getTimestamp()));
      } catch (DateTimeParseException e) {
        log.warn("Could not parse timestamp: {}, using current time", request.getTimestamp());
        event.setEventTimestamp(Instant.now());
      }
    } else {
      event.setEventTimestamp(Instant.now());
    }

    return event;
  }

  private EventType parseEventType(String eventType) {
    if (eventType == null || eventType.isEmpty()) {
      log.warn("Event type is null or empty, defaulting to ACTIVATION");
      return EventType.ACTIVATION;
    }

    return switch (eventType.toUpperCase()) {
      case "ACTIVATION", "SUBSCRIPTION", "ACTIVATE", "SUBSCRIBE", "1" -> EventType.ACTIVATION;
      case "RENEWAL", "RENEW", "REBILL" -> EventType.RENEWAL;
      case "DEACTIVATION", "DEACTIVATE", "UNSUBSCRIBE", "CHURN", "0" -> EventType.DEACTIVATION;
      default -> {
        log.warn("Unknown event type: {}, defaulting to ACTIVATION", eventType);
        yield EventType.ACTIVATION;
      }
    };
  }
}
