package com.nitax.valueplusbackend.dto.request;

import com.nitax.valueplusbackend.utils.enums.CampaignTypes;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import lombok.Data;

@Data
public class CreateCampaignForAdminDTO {
  @NotBlank private String campaignName;
  @NotBlank private String ageRange;
  @NotBlank private String campaignUrl;
  @NotBlank private String preferredGender;
  private Long campaignBudget;
  private String preferenceTraffic;
  private Long dailyBudget;
  private String interests;
  private String country;
  private String campaignImage;
  private String objective;
  private Instant startDate;
  private Instant endDate;
  private CampaignTypes campaignType = CampaignTypes.CPA;
  private String status;
  private Double costPerUser;
  private Double cpaCostPerUser;
  private String carrierConnection;
  private String trafficQuality;
  private Long advertiserId;
  private String flow;

  private String restrictionType;
  private String payoutModel;
  private String connectionType;
}
