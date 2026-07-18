package com.nitax.valueplusbackend.dto;

import lombok.Data;

@Data
public class PublisherCampaignMetricsDto {
    private String campaignName;
    private String campaignCountry;
    private String sourceId;
    private int totalClicks;
    private int totalConversions;
    private int churnCount;
    private double cr; // Conversion Rate
    private double ecpm; // Effective CPM
    private double totalAmountSpent;
    private double totalChurnHours;
}
