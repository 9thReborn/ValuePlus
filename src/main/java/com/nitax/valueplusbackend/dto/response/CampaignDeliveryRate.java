package com.nitax.valueplusbackend.dto.response;

public interface CampaignDeliveryRate {
    Long getCampaignId();
    String getCampaignName();
    String getProcessor();
    Long getTotalMessages();
    Long getDeliveredCount();
    Double getDeliveryRate();
}
