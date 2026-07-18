package com.nitax.valueplusbackend.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BulkSmsCampaignManagementResponse {
    private long totalCampaigns;
    private long activeCampaigns;
    private long scheduledCampaigns;
    private BigDecimal messagesSent;
    private BigDecimal overallSuccessRate;
    private String prohibitedWords;
}
