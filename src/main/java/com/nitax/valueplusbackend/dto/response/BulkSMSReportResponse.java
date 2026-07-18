package com.nitax.valueplusbackend.dto.response;

import lombok.Data;

@Data
public class BulkSMSReportResponse {
    private String date;
    private String campaignName;
    private String deliveryBreakdown;
    private String publisherApi;
    private String advertiserName;
    private long totalSmsTarget;
    private double totalAmountSpent;
}
