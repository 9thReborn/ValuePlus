package com.nitax.valueplusbackend.dto.response;

import lombok.Data;

@Data
public class BankDetailsResponse {
    private String bankName;
    private String accountNumber;
    private String accountName;
}
