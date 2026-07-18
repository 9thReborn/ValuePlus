package com.nitax.valueplusbackend.dto.request;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class PublisherCampaignRequest implements Serializable {
    @NotNull
    private String publisherId;

    @NotNull
    private String campaignId;

    @NotNull
    private Double publisherCpa;
}
