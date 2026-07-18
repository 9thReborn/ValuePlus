package com.nitax.valueplusbackend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SpendSummaryDto {
  private final long totalAmountSpent;
  private final long totalAmountSpentLastMonth;
  private final double percentageChange;
  private long[] chartNumbers;
}
