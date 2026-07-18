package com.nitax.valueplusbackend.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SmsPointInfoResponse {
    private BigDecimal costPerSms;
    private BigDecimal pointsToBeAssigned;
    private BigDecimal pointBalance;
}
