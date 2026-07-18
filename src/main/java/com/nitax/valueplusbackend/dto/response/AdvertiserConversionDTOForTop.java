package com.nitax.valueplusbackend.dto.response;

import lombok.Data;

@Data
public class AdvertiserConversionDTOForTop {

  private String status;
  private String businessName;
  private String campaignName;
  private Long conversions;
  private Long clicks;
  private Long budget;
  private String amountSpent;
  private Double cpa;
  private Long churn;
  private int rank;
  private String cr;
  private String eCPM;
  private String campaignId;

  public AdvertiserConversionDTOForTop(
      String status,
      String businessName,
      String campaignName,
      Long conversions,
      Long clicks,
      Long churn,
      Long budget,
      Double cpa) {
    this.status = status;
    this.businessName = businessName;
    this.campaignName = campaignName;
    this.conversions = conversions;
    this.clicks = clicks;
    this.budget = budget;
    this.amountSpent = amountSpent.toString();
    this.cpa = cpa;
    this.churn = churn;
  }

  public AdvertiserConversionDTOForTop(
      String status,
      String businessName,
      String campaignName,
      Long conversions,
      Long clicks,
      Long churn,
      Long budget,
      Double cpa,
      String campaignId,
      Double amountSpent) {
    this.status = status;
    this.businessName = businessName;
    this.campaignName = campaignName;
    this.conversions = conversions;
    this.clicks = clicks;
    this.budget = budget;
    this.amountSpent = amountSpent.toString();
    this.cpa = cpa;
    this.churn = churn;
    this.campaignId = campaignId;
  }
}
