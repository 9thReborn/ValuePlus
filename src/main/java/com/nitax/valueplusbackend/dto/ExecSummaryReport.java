package com.nitax.valueplusbackend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExecSummaryReport {
  private long totalNumberOfAdvertisers;
  private long totalNumOfCampaigns;
  private long totalNumOfActiveCampaigns;
  private long totalAcquisition;
  private long totalGoodAcquisition;
  private long totalChurnCount;
  private String totalChurnCost;
  private String totalRevenue;
  private String churnGrade;
  private long churnPercent;
  private String income;
}
