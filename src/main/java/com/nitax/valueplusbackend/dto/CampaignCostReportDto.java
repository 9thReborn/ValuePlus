package com.nitax.valueplusbackend.dto;

import com.univocity.parsers.annotations.Parsed;
import com.univocity.parsers.annotations.Trim;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CampaignCostReportDto {

  @Trim
  @Parsed(field = "Campaign Name")
  private String campaignName;

  @Trim
  @Parsed(field = "Total Acquisition")
  private long totalAcquisition;

  @Trim
  @Parsed(field = "Campaign Cost")
  private String campaignCost;

  @Trim
  @Parsed(field = "Campaign Status")
  private String campaignStatus;

  private Long clicks;
}
