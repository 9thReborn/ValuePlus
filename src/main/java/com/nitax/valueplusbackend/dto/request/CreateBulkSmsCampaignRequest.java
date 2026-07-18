package com.nitax.valueplusbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.util.Date;
import java.util.List;

@Data
public class CreateBulkSmsCampaignRequest {
    @NotBlank(message = "Campaign name cannot be blank")
    private String campaignName;
    @NotBlank(message = "Sender Id  cannot be blank")
    private String senderId;
    @NotBlank(message = "Campaign Content  cannot be blank")
    private String campaignContent;
    @NotBlank(message = "Campaign Country  cannot be blank")
    private String country;
    private String scheduledDate;
    private int numberOfTarget;
    private String lga;
    private String city;
    private String state;
    private String sector;
    private String advertiserId;
    private List<String> excludedNumbers;
}
