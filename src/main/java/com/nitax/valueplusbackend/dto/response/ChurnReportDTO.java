package com.nitax.valueplusbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
public final class ChurnReportDTO implements ChurnReport {

    private LocalDate reportDate;
    private String publisherName;
    private String campaignName;
    private int acquisition;
    private int churned;
}
