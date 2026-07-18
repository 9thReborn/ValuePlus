package com.nitax.valueplusbackend.dto.response.external;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GetWalletBalanceResponse {
    private BigDecimal balance;
    private BigDecimal smsPoints;
    private String message;
    private String status;
    private String currency;
}
