package com.nitax.valueplusbackend.dto.response;

import lombok.Data;

@Data
public class AdvertiserConversionDTO {

  private String status;
  private String advertiserName;
  private String campaignName;
  private Long conversions;
  private Long clicks;
  private Long budget;
  private Long amountSpent;
  private Double cpa;
  private Long churn;
  private int rank;
  private String cr;
  private String eCPM;
  private String campaignId;
  private String country;

  public AdvertiserConversionDTO(
      String status,
      String advertiserName,
      String campaignName,
      Long conversions,
      Long clicks,
      Long churn,
      Long budget,
      String country,
      Double cpa,
      Double amountSpent) {
    this.status = status;
    this.advertiserName = advertiserName;
    this.campaignName = campaignName;
    this.conversions = conversions;
    this.clicks = clicks;
    this.budget = budget;
    this.amountSpent = (long) (amountSpent * 100);
    this.cpa = cpa;
    this.churn = churn;
    this.country = country;
  }

  public AdvertiserConversionDTO(
      String status,
      String advertiserName,
      String campaignName,
      Long conversions,
      Long clicks,
      Long churn,
      Long budget,
      Double cpa,
      String campaignId) {
    this.status = status;
    this.advertiserName = advertiserName;
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
