package com.nitax.valueplusbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import lombok.Data;

@Data
public class AdvertiserConversionReportRequestDTO {
  @PastOrPresent private LocalDate startDate;
  @PastOrPresent private LocalDate endDate;
  private String campaignName;

  @Schema(hidden = true)
  private String advertiserName;
}
