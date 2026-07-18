package com.nitax.valueplusbackend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PublisherCampaignDashboardResponse {
    private long totalNumberOfCampaign;
    private long totalNumberOfActiveCampaign;
    private long totalNumberOfPausedCampaign ;
    private long totalNumberOfDisabledCampaign;
}
