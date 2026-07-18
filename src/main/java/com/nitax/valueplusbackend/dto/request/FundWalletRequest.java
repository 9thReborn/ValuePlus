package com.nitax.valueplusbackend.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FundWalletRequest {
    private BigDecimal amount;
    private String advertiserId;

    public FundWalletRequest() {
    }

}
