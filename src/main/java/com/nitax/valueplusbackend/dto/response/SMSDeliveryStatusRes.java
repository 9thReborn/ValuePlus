package com.nitax.valueplusbackend.dto.response;

public interface SMSDeliveryStatusRes {
    String getTargetNumber();
    String getStatus();
    String getDeliveryTimestamp();
    String getCountry();;
    String getRoute();
    String getCampaignId();
}
