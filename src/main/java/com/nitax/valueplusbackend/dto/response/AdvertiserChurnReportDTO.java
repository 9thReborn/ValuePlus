package com.nitax.valueplusbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
public class AdvertiserChurnReportDTO {
    private LocalDate reportDate;
    private String campaignName;
    private int acquisition;
    private int churned;
}
