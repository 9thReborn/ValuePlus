package com.nitax.valueplusbackend.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdvertiserSummaryReportDto {
  private long numberOfCampaigns;
  private long numberOfActiveCampaigns;
  private long numberOfConversions;
  private String totalCost;
  private List<CampaignCostReportDto> campaignCostReports;
}
