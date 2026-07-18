package com.nitax.valueplusbackend.domain;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Data;
import org.hibernate.annotations.Subselect;

@Entity
@Table(name = "hook_notifications_view")
@Data
@Subselect("SELECT * FROM hook_notifications_view")
public class NotificationView extends BaseEntity {

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private Notification.NotificationStatus status;

  @Column(name = "campaign_id")
  private String campaignId;

  @Column(name = "product_id")
  private String productId;

  @Column(name = "transaction_id", nullable = false)
  private String transactionId;

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

  @Column(name = "activation")
  private String activation;

  public Notification toNotification(NotificationView view) {
    Notification notification = new Notification();
    notification.setCampaignId(view.getCampaignId());
    notification.setProductId(view.getProductId());
    notification.setTransactionId(view.getTransactionId());
    notification.setPublisherId(view.getPublisherId());
    notification.setMessage(view.getMessage());
    notification.setSourceId(view.getSourceId());
    notification.setMsisdn(view.getMsisdn());
    notification.setUnsubscribeTimestamp(view.getUnsubscribeTimestamp());
    notification.setDuration(view.getDuration());
    notification.setCpaRevenue(view.getCpaRevenue());
    notification.setActivation(view.getActivation());
    notification.setStatus(view.getStatus());
    return notification;
  }
}
