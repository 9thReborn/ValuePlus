package com.nitax.valueplusbackend.dto.request;

import java.time.Instant;

import com.nitax.valueplusbackend.domain.SubscriberEvent.EventType;

import lombok.Data;

@Data
public class SubscriberEventFilter {

  private String advertiserId;

  private String msisdn;

  private String serviceId;

  private EventType eventType;

  private Instant startDate;

  private Instant endDate;
}
