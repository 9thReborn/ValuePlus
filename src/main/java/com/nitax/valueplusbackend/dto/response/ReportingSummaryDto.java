package com.nitax.valueplusbackend.dto.response;

import lombok.Data;

@Data
public class ReportingSummaryDto {
  private int year;
  private int month;
  private Long campaignCost;
  private long conversionCount;
  private long clickCount;
  private String campaignName;
  private long budget;

  public ReportingSummaryDto(
      int year,
      int month,
      Double campaignCost,
      long conversionCount,
      long clickCount,
      String campaignName,
      long budget) {
    this.year = year;
    this.month = month;
    this.campaignCost = (long) (campaignCost * 100);
    this.conversionCount = conversionCount;
    this.clickCount = clickCount;
    this.campaignName = campaignName;
    this.budget = budget;
  }
}
