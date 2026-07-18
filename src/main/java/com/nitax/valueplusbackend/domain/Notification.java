package com.nitax.valueplusbackend.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Data;

@Entity
@Table(name = "hook_notifications_archive_partitioned")
@Data
public class Notification extends BaseEntity {

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private NotificationStatus status;

  @Column(name = "campaign_id")
  private String campaignId;

  @Column(name = "product_id")
  private String productId;

  @Column(name = "transaction_id", nullable = false)
  private String transactionId;

  @Column(name = "short_trx_id", length = 16)
  private String shortTrxId;

  @Column(name = "publisher_id")
  private String publisherId;

  @Column(name = "message")
  private String message;

  @Column(name = "source_id")
  private String sourceId;

  @Column(name = "msisdn")
  private String msisdn;

  @Column(name = "unsubscribe_timestamp")
  private Instant unsubscribeTimestamp;

  @Column(name = "duration")
  private Long duration;

  @Column(name = "cpa_revenue")
  private Double cpaRevenue;

  @Column(name = "vp_revenue")
  private Double vpRevenue;

  @Column(name = "activation")
  private String activation;

  @Column(name = "month")
  private Integer month = LocalDate.now().getMonthValue();

  @Column(name = "year")
  private Integer year = LocalDate.now().getYear();

  @Column(name = "day")
  private Integer day = LocalDate.now().getDayOfMonth();

  public enum NotificationStatus {
    PUBLISHER_HOOK_RECEIVED,
    PUBLISHER_HOOK_SENT,
    ADVERTISER_HOOK_RECEIVED,
    UNSUBSCRIBED,
    ADVERTISER_HOOK_SENT,
    INVALID
  }
}
