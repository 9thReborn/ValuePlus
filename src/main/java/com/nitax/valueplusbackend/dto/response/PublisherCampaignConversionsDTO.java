package com.nitax.valueplusbackend.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PublisherCampaignConversionsDTO {
  private String campaignId;
  private String publisherName;
  private String campaignName;
  private Long conversions;
  private Long clicks;
  private Long churn;
  private String amountSpent;
  private Double CPA;
  private int rank;
  private String cr;
  private String eCPM;
  private String status;

  public PublisherCampaignConversionsDTO(
      String publisherName,
      String campaignName,
      Long conversions,
      Long clicks,
      Long churn,
      Double CPA) {
    this.publisherName = publisherName;
    this.campaignName = campaignName;
    this.conversions = conversions;
    this.clicks = clicks;
    this.amountSpent = null;
    this.CPA = CPA;
    this.churn = churn;
  }

  public PublisherCampaignConversionsDTO(
      String publisherName,
      String campaignName,
      Long conversions,
      Long clicks,
      Long churn,
      Double CPA,
      Double amountSpent) {
    this.publisherName = publisherName;
    this.campaignName = campaignName;
    this.conversions = conversions;
    this.clicks = clicks;
    this.amountSpent = String.valueOf(amountSpent);
    this.CPA = CPA;
    this.churn = churn;
  }
}
