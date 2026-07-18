package com.nitax.valueplusbackend.service.impl;

import com.nitax.valueplusbackend.domain.ConversionDecision;
import com.nitax.valueplusbackend.domain.ReasonCode;
import com.nitax.valueplusbackend.domain.SubscriberEvent;
import com.nitax.valueplusbackend.domain.ValidationDecision;
import com.nitax.valueplusbackend.repository.ConversionDecisionRepository;
import com.nitax.valueplusbackend.service.ConversionDecisionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversionDecisionServiceImpl implements ConversionDecisionService {

    private final ConversionDecisionRepository conversionDecisionRepository;

    @Override
    public ConversionDecision recordDecision(
            SubscriberEvent event,
            String affiliateId,
            ValidationDecision decision,
            ReasonCode reasonCode,
            String message) {
        return save(event, affiliateId, decision, reasonCode, message, false);
    }

    @Override
    public ConversionDecision recordReplayDecision(
            SubscriberEvent event,
            String affiliateId,
            ValidationDecision decision,
            ReasonCode reasonCode,
            String message) {
        return save(event, affiliateId, decision, reasonCode, message, true);
    }

    private ConversionDecision save(
            SubscriberEvent event,
            String affiliateId,
            ValidationDecision decision,
            ReasonCode reasonCode,
            String message,
            boolean replay) {
        ConversionDecision record = new ConversionDecision();
        record.setSubscriberEvent(event);
        record.setMsisdn(event.getSubscriber().getMsisdn());
        record.setServiceId(event.getSubscriber().getServiceId());
        record.setAffiliateId(affiliateId);
        record.setDecision(decision);
        record.setReasonCode(reasonCode == null ? ReasonCode.NONE : reasonCode);
        record.setMessage(message);
        record.setDecisionTime(Instant.now());
        record.setReplay(replay);
        record.setReviewerStatus(
                decision == ValidationDecision.FLAG
                        ? ConversionDecision.ReviewerStatus.PENDING_REVIEW
                        : ConversionDecision.ReviewerStatus.NOT_REQUIRED);

        ConversionDecision saved = conversionDecisionRepository.save(record);
        log.info(
                "Conversion decision recorded: eventId={}, msisdn={}, decision={}, reasonCode={}, replay={}",
                event.getId(),
                record.getMsisdn(),
                decision,
                record.getReasonCode(),
                replay);
        return saved;
    }

    @Override
    public List<ConversionDecision> findByEventId(Long subscriberEventId) {
        return conversionDecisionRepository.findBySubscriberEventId(subscriberEventId);
    }

    @Override
    public Page<ConversionDecision> findByMsisdn(String msisdn, Pageable pageable) {
        return conversionDecisionRepository.findByMsisdnOrderByDecisionTimeDesc(msisdn, pageable);
    }
}
