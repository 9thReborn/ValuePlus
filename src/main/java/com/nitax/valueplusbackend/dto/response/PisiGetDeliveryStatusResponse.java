package com.nitax.valueplusbackend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PisiGetDeliveryStatusResponse {
    @JsonProperty("PartnerTransactionId")
    private String partnerTransactionId;
    @JsonProperty("TotalSms")
    private String totalSms;
    @JsonProperty("DlrDateTime")
    private String deliveryDateTime;
    @JsonProperty("Pending")
    private boolean pending;
    @JsonProperty("Delivered")
    private boolean delivered;
    @JsonProperty("Undelivered")
    private String undelivered;
    @JsonProperty("UndeliveredDND")
    private String undeliveredDND;
    @JsonProperty("Expired")
    private String expired;
    @JsonProperty("Unknown")
    private String unknown;


}
