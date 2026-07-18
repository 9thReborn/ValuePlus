package com.nitax.valueplusbackend.dto.request;

import lombok.Data;

import java.util.List;
@Data
public class SmsDeliveryReportDto {
    private List<String> messageIDs;
    private String status;
    private String statusText;
    private String error;
    private String ts;
}
