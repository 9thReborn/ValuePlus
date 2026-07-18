package com.nitax.valueplusbackend.service;

import com.nitax.valueplusbackend.domain.ConversionDecision;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.nitax.valueplusbackend.domain.Subscriber;
import com.nitax.valueplusbackend.domain.SubscriberEvent;
import com.nitax.valueplusbackend.dto.request.SubscriberEventFilter;
import com.nitax.valueplusbackend.dto.request.SubscriptionWebhookRequest;
import com.nitax.valueplusbackend.dto.response.SubscriberDetailDTO;

public interface SubscriberService {

  Subscriber processSubscriptionWebhook(SubscriptionWebhookRequest request, String rawPayload);

  void backfillMissingUnsubscribedNotifications();

  Page<SubscriberEvent> searchEvents(SubscriberEventFilter filter, Pageable pageable);

  SubscriberDetailDTO getSubscriberDetail(Long subscriberId);
  ConversionDecision replayEvent(Long subscriberEventId);
}
