package com.nitax.valueplusbackend.dto.response;

import lombok.Data;

@Data
public class CreateBulkSmsCampaignResponse {

    private String message;
    private boolean success;
    private BulkSmsCampaignResponse bulkSmsCampaign;

    public CreateBulkSmsCampaignResponse(String message, boolean success, BulkSmsCampaignResponse bulkSmsCampaign) {
        this.message = message;
        this.success = success;
        this.bulkSmsCampaign = bulkSmsCampaign;
    }
}
