package com.nitax.valueplusbackend.dto.response;

import lombok.Data;

@Data
public class GeminiRetrieveAccountBalanceResponse {
    private String balance;
    private String currency;
}
