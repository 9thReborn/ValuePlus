package com.nitax.valueplusbackend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(
    name = "subscribers",
    indexes = {
      @Index(name = "idx_subscriber_msisdn", columnList = "msisdn"),
      @Index(name = "idx_subscriber_service_id", columnList = "service_id"),
      @Index(name = "idx_subscriber_msisdn_service", columnList = "msisdn, service_id"),
      @Index(name = "idx_subscriber_trx_id", columnList = "trx_id"),
      @Index(name = "idx_subscriber_advertiser_id", columnList = "advertiser_id")
    })
@Data
@EqualsAndHashCode(callSuper = true)
public class Subscriber extends BaseEntity {

  @Column(name = "msisdn", nullable = false)
  private String msisdn;

  @Column(name = "service_id")
  private String serviceId;

  @Column(name = "auto_renew")
  private Boolean autoRenew;

  @Column(name = "advertiser_id")
  private String advertiserId;

  @Column(name = "click_id")
  private String clickId;

  @Column(name = "trx_id")
  private String trxId;

  @Column(name = "campaign_id")
  private String campaignId;

  @Column(name = "publisher_id")
  private String publisherId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private SubscriberStatus status = SubscriberStatus.ACTIVE;

  public enum SubscriberStatus {
    ACTIVE,
    INACTIVE,
    CHURNED
  }
}
