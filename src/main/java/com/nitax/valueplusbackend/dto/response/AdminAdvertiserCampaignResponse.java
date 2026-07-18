package com.nitax.valueplusbackend.dto.response;

import lombok.Data;

@Data
public class AdminAdvertiserCampaignResponse {
    private String campaignId;
    private String campaignName;
    private String advertiserName;
    private String status;
    private String createdDate;
    private String scheduledDate;
}
