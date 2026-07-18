package com.nitax.valueplusbackend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CampaignAnalyticsResponseDto {

  private int[] monthsLabel;

  private String dataLabel;

  private long[] data;
}
