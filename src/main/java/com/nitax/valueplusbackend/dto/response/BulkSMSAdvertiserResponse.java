package com.nitax.valueplusbackend.dto.response;

import lombok.Data;

@Data
public class BulkSMSAdvertiserResponse {
    private String status;
    private String email;
    private String businessName;
    private String skype;
    private double pointBalance;
    private double cpa;
    private String advertiserId;
}
