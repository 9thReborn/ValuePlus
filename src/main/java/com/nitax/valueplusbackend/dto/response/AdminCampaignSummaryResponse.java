package com.nitax.valueplusbackend.dto.response;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class AdminCampaignSummaryResponse {
    private long totalCampaigns;
    private long activeCampaigns;
    private long completedCampaigns;
    private long cancelledCampaigns;
    private long pendingCampaigns;
    private long scheduledCampaigns;
    private long totalMessagesSent;
    private String prohibitedWords;
    private BigDecimal deliveryStatistics;
}
