package com.nitax.valueplusbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
public class BulkSmsDashboardSummaryDto {
    private double totalAvailablePoints;
    private long numberOfCompletedCampaigns;
    private long numberOfUpcomingCampaigns;
    private List<String> upcomingCampaigns;
    private long totalSmsSentForTheMonth;
    private Instant lastCampaignDay;
    private double pointUsedForTheMonth;
    private double monthlyAveragePercentage;
}
