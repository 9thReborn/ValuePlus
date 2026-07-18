package com.nitax.valueplusbackend.dto.request;

import lombok.Data;

@Data
public class BankDetailsRequest {
    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private String accountHolderName;

}
