package com.nitax.valueplusbackend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AdminChurnReportDto {
    private LocalDate acquisitionDay;
    private String publisherId;
    private String publisherName;
    private String sourceId;
    private String campaignName;
    private long totalAcquired;
    private long totalChurned;
    private long totalSurvived;
    private String churnPercent;
    private long totalAdvertiserHookReceived;
    private double amountSpent;

    // Business rule: when churn exceeds this threshold, the spend attributable to the churned
    // conversions is deducted from amountSpent. At or below it, no deduction is applied.
    private static final double CHURN_DEDUCTION_THRESHOLD = 5.0;

    public AdminChurnReportDto(LocalDate acquisitionDay, String publisherId, String publisherName,
                               String sourceId, String campaignName, long totalAcquired,
                               long totalChurned, long totalAdvertiserHookReceived,
                               double amountSpent, double churnedAmount) {
        this.acquisitionDay = acquisitionDay;
        this.publisherId = publisherId;
        this.publisherName = publisherName;
        this.sourceId = sourceId;
        this.campaignName = campaignName;
        this.totalAcquired = totalAcquired;
        this.totalChurned = totalChurned;
        this.totalSurvived = totalAcquired - totalChurned;
        double churnRate = totalAcquired == 0 ? 0.0 : (totalChurned * 100.0) / totalAcquired;
        this.churnPercent = String.format("%.2f%%", churnRate);
        this.totalAdvertiserHookReceived = totalAdvertiserHookReceived;
        this.amountSpent = churnRate > CHURN_DEDUCTION_THRESHOLD
                ? amountSpent - churnedAmount
                : amountSpent;
    }
}
