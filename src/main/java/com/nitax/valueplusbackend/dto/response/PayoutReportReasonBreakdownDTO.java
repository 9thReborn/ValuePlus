package com.nitax.valueplusbackend.dto.response;

import com.nitax.valueplusbackend.domain.ReasonCode;
import lombok.Data;

@Data
public class PayoutReportReasonBreakdownDTO {

    private final ReasonCode reasonCode;
    private final long count;
    private final double valueRemoved;

    public PayoutReportReasonBreakdownDTO(ReasonCode reasonCode, long count, Double valueRemoved) {
        this.reasonCode = reasonCode;
        this.count = count;
        this.valueRemoved = valueRemoved == null ? 0.0 : valueRemoved;
    }
}
