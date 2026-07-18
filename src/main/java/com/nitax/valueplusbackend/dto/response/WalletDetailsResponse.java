package com.nitax.valueplusbackend.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletDetailsResponse {
    private BigDecimal walletBalance;
    private BigDecimal pointBalance;
    private BankDetailsResponse bankDetails;
}
