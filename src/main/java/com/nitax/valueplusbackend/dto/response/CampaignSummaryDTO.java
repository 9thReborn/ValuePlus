package com.nitax.valueplusbackend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CampaignSummaryDTO {

  private final long totalCampaignCount;
  private final double percentageChange;
  private long[] chartNumbers;
}
