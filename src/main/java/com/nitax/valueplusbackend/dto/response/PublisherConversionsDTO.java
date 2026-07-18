package com.nitax.valueplusbackend.dto.response;

import lombok.Data;

@Data
public class PublisherConversionsDTO {

  private String publisherName;
  private Long conversions;
  private Long clicks;
  private Long churn;
  private String amountSpent;
  private Double CPA;
  private int rank;
  private Double cr;
  private Double eCPM;

  public PublisherConversionsDTO(
      String publisherName, Long conversions, Long clicks, Long churn, Double CPA) {
    this.publisherName = publisherName;
    this.conversions = conversions;
    this.clicks = clicks;
    this.CPA = CPA;
    this.churn = churn;
  }
}
