package com.nitax.valueplusbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for advertiser conversion report with CPA breakdown.
 * Shows conversions at each unique CPA rate for accurate amount tracking.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdvertiserConversionCpaBreakdownDTO {

  private String advertiserName;
  private String campaignName;
  private String campaignId;
  private String status;
  private String country;
  private Long budget;
  
  // CPA rate at which conversions occurred
  private Double cpaRate;
  
  // Number of conversions at this CPA rate
  private Long conversions;
  
  // Clicks associated with this campaign
  private Long clicks;
  
  // Churn count
  private Long churn;
  
  // Amount spent at this CPA rate (cpaRate × conversions)
  private Double amountSpent;
}
