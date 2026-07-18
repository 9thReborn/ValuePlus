package com.nitax.valueplusbackend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.Instant;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReportSummaryRequestDto {
  @NotNull @PastOrPresent private Instant startDate;
  @NotNull @PastOrPresent private Instant endDate;
}
