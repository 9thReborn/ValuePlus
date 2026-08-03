package com.nitax.valueplusbackend.service;

import com.nitax.valueplusbackend.domain.ConversionDecision;
import com.nitax.valueplusbackend.domain.ReasonCode;
import com.nitax.valueplusbackend.domain.SubscriberEvent;
import com.nitax.valueplusbackend.domain.ValidationDecision;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ConversionDecisionService {
    ConversionDecision recordDecision(
            SubscriberEvent event,
            String publisherId,
            ValidationDecision decision,
            ReasonCode reasonCode,
            String message);

    ConversionDecision recordReplayDecision(
            SubscriberEvent event,
            String publisherId,
            ValidationDecision decision,
            ReasonCode reasonCode,
            String message);

    List<ConversionDecision> findByEventId(Long subscriberEventId);

    Page<ConversionDecision> findByMsisdn(String msisdn, Pageable pageable);
}
