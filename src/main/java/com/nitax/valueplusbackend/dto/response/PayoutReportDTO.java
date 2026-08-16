package com.nitax.valueplusbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
public class PayoutReportDTO {

    private Instant dateFrom;
    private Instant dateTo;
    private long totalConversions;
    private long invalidCount;
    private long payableCount;
    private double invalidValueRemoved;
    private List<PayoutReportReasonBreakdownDTO> invalidBreakdown;
}