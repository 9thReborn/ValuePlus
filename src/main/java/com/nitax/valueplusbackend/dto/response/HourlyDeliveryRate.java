package com.nitax.valueplusbackend.dto.response;

public interface HourlyDeliveryRate {
    Integer getDeliveryHour();
    Long getTotalMessages();
    Long getDeliveredCount();
    Double getDeliveryRate();
}
