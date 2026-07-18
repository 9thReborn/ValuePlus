package com.nitax.valueplusbackend.dto.response;

import lombok.Data;

@Data
public class PublisherCampaignDto {
    private String id;
    private String campaignName;
    private String status;
    private long clicks;
    private String campaignLink;
    private  String publisherName;
    private String conversion;
    private double amountMade;
    private double cpa;
    private long campaignRank;
    private long publisherRank;
}
