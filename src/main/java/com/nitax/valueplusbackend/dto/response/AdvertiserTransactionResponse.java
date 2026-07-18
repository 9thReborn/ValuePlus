package com.nitax.valueplusbackend.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class AdvertiserTransactionResponse {
    private Date transactionDate;
    private String transactionId;
    private String transactionType;
    private String status;
    private BigDecimal amount;
    private double point;
    private double CPA;// Cost per SMS
}
