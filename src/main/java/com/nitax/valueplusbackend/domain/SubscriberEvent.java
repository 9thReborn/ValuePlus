package com.nitax.valueplusbackend.domain;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "subscriber_events", indexes = {
    @Index(name = "idx_event_subscriber_id", columnList = "subscriber_id"),
    @Index(name = "idx_event_type", columnList = "event_type"),
    @Index(name = "idx_event_timestamp", columnList = "event_timestamp"),
    @Index(name = "idx_event_idempotency_key", columnList = "idempotency_key", unique = true)
})
@Data
@EqualsAndHashCode(callSuper = true)
public class SubscriberEvent extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subscriber_id", nullable = false)
  private Subscriber subscriber;

  @Enumerated(EnumType.STRING)
  @Column(name = "event_type", nullable = false)
  private EventType eventType;

  @Column(name = "payload_json", columnDefinition = "TEXT")
  private String payloadJson;

  @Column(name = "event_timestamp")
  private Instant eventTimestamp;

  @Column(name = "idempotency_key", unique = true)
  private String idempotencyKey;

  // Revenue fields
  @Column(name = "billing_amount", precision = 19, scale = 4)
  private BigDecimal billingAmount;

  @Column(name = "currency", length = 10)
  private String currency;

  @Column(name = "billing_cycle", length = 20)
  private String billingCycle;

  public enum EventType {
    ACTIVATION,
    RENEWAL,
    DEACTIVATION
  }
}
