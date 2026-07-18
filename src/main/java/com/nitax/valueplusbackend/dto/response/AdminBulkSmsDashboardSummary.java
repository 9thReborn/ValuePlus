package com.nitax.valueplusbackend.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminBulkSmsDashboardSummary {
    private long totalBulkSMSAdvertisers;
    private long activeBulkSMSAdvertisers;
    private BigDecimal totalBulkSMSPoints;
    private BigDecimal averageCpa;

}
