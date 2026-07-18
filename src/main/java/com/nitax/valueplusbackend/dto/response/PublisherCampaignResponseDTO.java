package com.nitax.valueplusbackend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PublisherCampaignResponseDTO {
    private long id;
    private String campaignName;
    private String status;
    private long clicks;
    private String campaignLink;
    private String publisherName;
    private long conversion;
    private BigDecimal amountMade;
    private double cpa;
    private long campaignRank;
    private long publisherRank;

    public PublisherCampaignResponseDTO(long id, String campaignName, String status, long clicks, String campaignLink, String publisherName, long conversion, BigDecimal amountMade, double cpa, long campaignRank, long publisherRank) {
        this.id = id;
        this.campaignName = campaignName;
        this.status = status;
        this.clicks = clicks;
        this.campaignLink = campaignLink;
        this.publisherName = publisherName;
        this.conversion = conversion;
        this.amountMade = amountMade;
        this.cpa = cpa;
        this.campaignRank = campaignRank;
        this.publisherRank = publisherRank;
    }



    public PublisherCampaignResponseDTO(Long id, String name, String s, Long clicks, String campaignLink, String publisherName,double cpa) {
        this.id = id;
        this.campaignName = name;
        this.status = s;
        this.clicks = clicks;
        this.campaignLink = campaignLink;
        this.publisherName = publisherName;
        this.cpa =  cpa;
    }

    public PublisherCampaignResponseDTO() {
    }
}
