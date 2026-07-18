package com.nitax.valueplusbackend.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportingChartDto {
  private Instant day;
  @JsonIgnore private int dbDay;
  private Long count;
}
