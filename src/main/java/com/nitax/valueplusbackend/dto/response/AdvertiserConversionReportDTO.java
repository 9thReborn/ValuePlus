package com.nitax.valueplusbackend.dto.response;

import lombok.Data;

@Data
public class AdvertiserConversionReportDTO {

  private String status;
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

  public AdvertiserConversionReportDTO(
      String status,
      String campaignName,
      Long conversions,
      Long clicks,
      Long churn,
      Long budget,
      Double cpa) {
    this.status = status;
    this.campaignName = campaignName;
    this.conversions = conversions;
    this.clicks = clicks;
    this.budget = budget;
    this.amountSpent = null;
    this.cpa = cpa;
    this.churn = churn;
  }

  public AdvertiserConversionReportDTO(
      String status,
      String campaignName,
      Long conversions,
      Long clicks,
      Long churn,
      Long budget,
      Double cpa,
      String campaignId) {
    this.status = status;
    this.campaignName = campaignName;
    this.conversions = conversions;
    this.clicks = clicks;
    this.budget = budget;
    this.amountSpent = null;
    this.cpa = cpa;
    this.churn = churn;
    this.campaignId = campaignId;
  }
}
