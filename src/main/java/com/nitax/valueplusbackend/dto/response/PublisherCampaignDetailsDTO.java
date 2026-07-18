package com.nitax.valueplusbackend.dto.response;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class PublisherCampaignDetailsDTO {
    private String campaignName;
    private String campaignId;
    private String campaignStatus;
    private Long totalClicks;
    private String campaignLink;
    private String publisherName;
    private Long totalConversions;
    private BigDecimal totalAmountMade;
    private Double publisherCpa;
    private Long publisherRank;
    private Long campaignRank;
    private String country;
    private double cpa;
    private String MNO;
    private String flow;
    private String gender;
    private String ageRange;
    private String interest;
    private String budget;
    private String startDate;
    private String endDate;
    private String image;
    private String carrierConnection;




    // Constructors, Getters, and Setter
}
