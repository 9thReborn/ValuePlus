package com.nitax.valueplusbackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CampaignReport {

  private Long id;
  private String name;
  private Double costPerUser;
  private Long acquisition;
  private Long reach;
  private String status;
  private Double campaignCost;
  private Long budget;
  private String campaignId;
}
