package com.nitax.valueplusbackend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.Instant;
import lombok.Data;

@Data
public class ReportChartRequestDto {
  @NotNull @PastOrPresent private Instant startDate;
  @NotNull @PastOrPresent private Instant endDate;
  @NotNull private String chartType;
}
