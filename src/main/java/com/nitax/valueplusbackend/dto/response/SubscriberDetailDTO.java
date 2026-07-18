package com.nitax.valueplusbackend.dto.response;

import java.time.Instant;
import java.util.List;

import com.nitax.valueplusbackend.domain.Subscriber;
import com.nitax.valueplusbackend.domain.SubscriberEvent;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubscriberDetailDTO {

  private Subscriber subscriber;

  private List<SubscriberEvent> events;

  private List<BillingInfo> billingHistory;

  private BillingSummary billingSummary;

  @Data
  @Builder
  public static class BillingInfo {
    private Long id;
    private String transactionId;
    private String status;
    private String campaignId;
    private String publisherId;
    private Double cpaRevenue;
    private Double vpRevenue;
    private String activation;
    private Instant createdDate;
    private Instant unsubscribeTimestamp;
    private Long duration;
  }

  @Data
  @Builder
  public static class BillingSummary {
    private long totalBillingEvents;
    private long activations;
    private long unsubscribes;
    private Double totalCpaRevenue;
    private Double totalVpRevenue;
  }
}
