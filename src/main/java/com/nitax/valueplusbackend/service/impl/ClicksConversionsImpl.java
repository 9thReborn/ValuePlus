package com.nitax.valueplusbackend.service.impl;

import com.nitax.valueplusbackend.domain.Campaign;
import com.nitax.valueplusbackend.domain.Notification;
import com.nitax.valueplusbackend.domain.Publisher;
import com.nitax.valueplusbackend.domain.PublisherCampaign;
import com.nitax.valueplusbackend.dto.SecureDNotificationDto;
import com.nitax.valueplusbackend.dto.request.ClickTrackingDto;
import com.nitax.valueplusbackend.service.CampaignService;
import com.nitax.valueplusbackend.service.ClicksConversionsService;
import com.nitax.valueplusbackend.service.NotificationService;
import com.nitax.valueplusbackend.service.PhoneNumberService;
import com.nitax.valueplusbackend.service.PublisherCampaignService;
import com.nitax.valueplusbackend.service.PublisherService;
import com.nitax.valueplusbackend.utils.AppUtils;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClicksConversionsImpl implements ClicksConversionsService {

  private final NotificationService notificationService;
  private final CampaignService campaignService;
  private final PublisherService publisherService;
  private final PublisherCampaignService publisherCampaignService;
  private final PhoneNumberService phoneNumberService;
  private final AppUtils appUtils;

  @Override
  public String getRedirectUrl(String campaignId, ClickTrackingDto clickTrackingDto) {
    try {
      Optional<Campaign> campaignCheck = null;

      if (campaignId != null) {
        campaignCheck = campaignService.findCampaignById(campaignId);
      }

      //      Optional<Campaign> campaignCheck = campaignService.findCampaignById(campaignId);
      //      if (campaignCheck.isEmpty()) {
      //        return null;
      //      }

      Campaign campaign = campaignCheck.get();
      String campaignUrl = campaign.getUrl();
      if (Objects.isNull(campaignUrl)) {
        return null;
      }
      String[] trxParts = clickTrackingDto.getTrxId().trim().split("_");
      String publisherId = trxParts.length > 1 ? trxParts[1] : null;

      Publisher publisher = null;
      if (publisherId != null) {
        publisher = publisherService.findByPubId(publisherId);
      }
      String trafficSource = pickIdentifier(publisher);

      String shortTrxId = appUtils.generateShortTrxId();

      notificationService.recordClickEvent(
          campaignId,
          clickTrackingDto.getTrxId(),
          shortTrxId,
          clickTrackingDto.getSourceId(),
          campaign.getCpaCampaignCost(),
          campaign.getCostPerUser());

      String redirectUrl =
          campaignUrl
              .replace("{click_id}", shortTrxId)
              .replace("{source}", trafficSource)
              .replace(
                  "{source_id}",
                  Objects.nonNull(clickTrackingDto.getSourceId())
                      ? clickTrackingDto.getSourceId()
                      : "")
              .trim();

      // https://clicks.valueplusbackend.com/cc/redirect/1?trxId=1&trfsrc=1&sourceId=1
      log.info("Redirecting to: {}", redirectUrl);
      return redirectUrl;
    } catch (Exception e) {
      log.error("Error in getRedirectUrl: ", e);
      return null;
    }
  }

  private String pickIdentifier(Publisher publisher) {
    if (publisher == null || publisher.getIdentifier() == null) {
      return "mac";
    }
    String[] parts = publisher.getIdentifier().split(",");
    java.util.List<String> options = new java.util.ArrayList<>(parts.length);
    for (String p : parts) {
      String trimmed = p.trim();
      if (!trimmed.isEmpty()) {
        options.add(trimmed);
      }
    }
    if (options.isEmpty()) {
      return "mac";
    }
    return options.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(options.size()));
  }

  @Override
  @Async
  public void handleAdvertiserPostbackByGET(
      String advertiserId,
      String campaignId,
      String trxId,
      String sourceId,
      String msisdn,
      String message,
      String activation) {
    try {
      // Check for duplicate trxId

      // remove trailing + sign from msisdn
      if (msisdn != null && msisdn.startsWith("+")) {
        msisdn = msisdn.substring(1);
      }
      // log all parameter
      log.info(
          "advertiserId: {}, campaignId: {}, trxId: {}, sourceId: {}, msisdn: {}, message: {}, activation: {}",
          advertiserId,
          campaignId,
          trxId,
          sourceId,
          msisdn,
          message,
          activation);

      Optional<Notification> shortMatch = notificationService.findClickByShortTrxId(trxId);

      String shortTrxId;
      String publisherId;
      String fullTrxId;
      if (shortMatch.isPresent()) {
        Notification clickRecord = shortMatch.get();
        shortTrxId = trxId;
        fullTrxId = clickRecord.getTransactionId();
        publisherId = clickRecord.getPublisherId();
        if (sourceId == null || sourceId.isEmpty()) {
          sourceId = clickRecord.getSourceId();
        }
        if (campaignId == null || campaignId.isEmpty()) {
          campaignId = clickRecord.getCampaignId();
        }
      } else {
        shortTrxId = null;
        fullTrxId = trxId;
        publisherId = trxId.trim().split("_")[1];
      }

      String clickId =
          fullTrxId
              .trim()
              .replace("valueplus_" + publisherId + "_", "")
              .replace("valueplus2_" + publisherId + "_", "")
              .replace("vpbcairtel_" + publisherId + "_", "")
              .replace("vpmpesa_" + publisherId + "_", "")
              .replace(campaignId + "_" + publisherId + "_", "")
              .split("SRCID")[0];
      if (notificationService.existsByTransactionId(clickId)) {
        log.warn("Duplicate trxId detected: {}", trxId);
        Notification dupNotification = new Notification();
        dupNotification.setCampaignId(campaignId);
        dupNotification.setTransactionId(clickId);
        dupNotification.setShortTrxId(shortTrxId);
        dupNotification.setSourceId(sourceId);
        dupNotification.setStatus(Notification.NotificationStatus.INVALID);
        dupNotification.setMsisdn(msisdn);
        dupNotification.setMessage("Duplicate trxId: " + trxId);
        dupNotification.setActivation(activation);
        dupNotification.setYear(LocalDate.now().getYear());
        dupNotification.setMonth(LocalDate.now().getMonthValue());
        dupNotification.setDay(LocalDate.now().getDayOfMonth());
        notificationService.saveNotification(dupNotification);
        return;
      }

      List whiteListedMsisdns =
          List.of(
              "2348068898035",
              "2348062765439",
              "254722000000",
              "2347063581411",
              "2348089893819",
              "2349123232030",
              "2349010368608");

      if (msisdn != null
          && !msisdn.isEmpty()
          && !whiteListedMsisdns.contains(msisdn)
          && notificationService.existsByMsisdn(msisdn)) {
        log.warn("Duplicate msisdn detected: {}", msisdn);
        Notification dupNotification = new Notification();
        dupNotification.setCampaignId(campaignId);
        dupNotification.setTransactionId(clickId);
        dupNotification.setShortTrxId(shortTrxId);
        dupNotification.setSourceId(sourceId);
        dupNotification.setStatus(Notification.NotificationStatus.INVALID);
        dupNotification.setMsisdn(msisdn);
        dupNotification.setMessage("Duplicate msisdn: " + msisdn);
        dupNotification.setActivation(activation);
        dupNotification.setYear(LocalDate.now().getYear());
        dupNotification.setMonth(LocalDate.now().getMonthValue());
        dupNotification.setDay(LocalDate.now().getDayOfMonth());
        notificationService.saveNotification(dupNotification);
        return;
      }

      Notification notification = new Notification();
      notification.setCampaignId(campaignId);
      notification.setTransactionId(clickId);
      notification.setShortTrxId(shortTrxId);
      notification.setSourceId(sourceId);
      notification.setStatus(Notification.NotificationStatus.ADVERTISER_HOOK_RECEIVED);
      notification.setMsisdn(msisdn);
      notification.setMessage(message);
      notification.setActivation(activation);
      notification.setYear(LocalDate.now().getYear());
      notification.setMonth(LocalDate.now().getMonthValue());
      notification.setDay(LocalDate.now().getDayOfMonth());
      notification = notificationService.saveNotification(notification);

      Campaign campaign;
      Publisher publisher;
      Double cpaRevenue = 0.0;
      PublisherCampaign publisherCampaign =
          publisherCampaignService.findByPublisherIdAndCampaignId(campaignId, publisherId);

      if (publisherCampaign == null) {
        campaign = campaignService.findCampaignById(campaignId).orElse(null);
        publisher = publisherService.findByPubId(publisherId);
        assert campaign != null;
        cpaRevenue = campaign.getCpaCostPerUser();
        log.error("PublisherCampaign not found, falling back to campaign and publisher");
      } else {
        campaign = publisherCampaign.getCampaign();
        publisher = publisherCampaign.getPublisher();
        cpaRevenue = publisherCampaign.getPublisherCpa();
      }

      if (campaign != null && campaign.getStatus().equalsIgnoreCase("ACTIVE")) {
        notification.setCampaignId(campaign.getCampaignId());
        notification.setCpaRevenue(cpaRevenue);
        notification.setVpRevenue(campaign.getCostPerUser());
        if (publisher != null) {
          notification.setPublisherId(publisher.getPubId());
          Notification savedNotification = notificationService.saveNotification(notification);
          if (activation.equalsIgnoreCase("1")) {
            publisherService.handlePublisherPostBack(savedNotification);
          }
        } else {
          notification.setStatus(Notification.NotificationStatus.INVALID);
          notification.setMessage("Publisher not found");
          notificationService.saveNotification(notification);
          log.info("Publisher not found");
        }
      } else if (campaign != null && campaign.getStatus().equalsIgnoreCase("INACTIVE")) {
        notification.setMessage("Campaign is not active");
        notification.setStatus(Notification.NotificationStatus.INVALID);
        notificationService.saveNotification(notification);
      } else if (campaign == null) {
        notification.setMessage("Campaign not found");
        notification.setStatus(Notification.NotificationStatus.INVALID);
        notificationService.saveNotification(notification);
      }
    } catch (Exception e) {
      log.error("Error in handleAdvertiserPostbackByGET:  " + e);
    }
  }

  @Override
  public void handleSecureDWebhook(SecureDNotificationDto secureDNotificationDto) {
    try {
      String advertiserId = secureDNotificationDto.getAdvertiserId();
      String campaignId = secureDNotificationDto.getCampaignId();
      String trxId = secureDNotificationDto.getTrxId().trim().split("SRCID")[0];
      String sourceId =
          secureDNotificationDto.getTrxId().trim().split("SRCID").length > 1
              ? secureDNotificationDto.getTrxId().trim().split("SRCID")[1]
              : "";
      String msisdn = secureDNotificationDto.getMsisdn();
      String message = secureDNotificationDto.getDescription();

      // log phone number
      if (msisdn != null && !msisdn.isEmpty()) {
        phoneNumberService.addPhoneNumberFromCampaign(msisdn, campaignId);
      }

      if (secureDNotificationDto.getActivation().equalsIgnoreCase("1")) {
        handleAdvertiserPostbackByGET(
            advertiserId,
            campaignId,
            trxId,
            sourceId,
            msisdn,
            message,
            secureDNotificationDto.getActivation());
      }
    } catch (Exception e) {
      log.error("Error in handleSecureDWebhook: ", e);
    }
  }

  @Override
  public void resendPendingConversions() {
    List<Notification> notifications = notificationService.getPendingNotifications();
    for (Notification notification : notifications) {
      String publisherId = notification.getPublisherId();
      if (publisherId == null || publisherId.isEmpty() || "unknown".equals(publisherId)) {
        String trxId = notification.getTransactionId();
        if (trxId != null && trxId.split("_").length > 1) {
          publisherId = trxId.split("_")[1];
        }
      }
      Publisher publisher = publisherId != null ? publisherService.findByPubId(publisherId) : null;
      if (publisher != null) {
        publisherService.handlePublisherPostBack(notification);
      }
    }
  }

  @Override
  @Async
  public void handleAdvertiserPostbackByGET2(
      String trxId, String sourceId, String msisdn, String success, String number) {
    // check if trxId is valid
    if (trxId == null || trxId.isEmpty()) {
      log.error("trxId is empty");
      return;
    }

    String campaignId;
    Optional<Notification> shortMatch = notificationService.findClickByShortTrxId(trxId);
    if (shortMatch.isPresent()) {
      campaignId = shortMatch.get().getCampaignId();
      if (sourceId == null || sourceId.isEmpty()) {
        sourceId = shortMatch.get().getSourceId();
      }
    } else if (trxId.split("_").length >= 2) {
      campaignId = trxId.split("_")[0];
    } else {
      log.error("trxId is invalid");
      return;
    }

    if (msisdn != null && !msisdn.isEmpty()) {
      //      phoneNumberService.addPhoneNumberFromCampaign(msisdn, campaignId);
    }
    handleAdvertiserPostbackByGET("", campaignId, trxId, sourceId, msisdn, success, number);
  }
}
