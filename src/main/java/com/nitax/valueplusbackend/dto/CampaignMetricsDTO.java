package com.nitax.valueplusbackend.dto;

import lombok.Data;

@Data
public class CampaignMetricsDTO {
  private long conversions;
  private double vpCost;
  private long clicks;
  private double cpaCost;

  public CampaignMetricsDTO(long conversions, double vpCost, long clicks, double cpaCost) {
    this.conversions = conversions;
    this.clicks = clicks;
    this.cpaCost = cpaCost;
    this.vpCost = vpCost;
  }
}
