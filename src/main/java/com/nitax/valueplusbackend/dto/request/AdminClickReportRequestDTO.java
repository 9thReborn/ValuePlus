package com.nitax.valueplusbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import lombok.Data;

@Data
@Schema(description = "Request parameters for the admin click-ID report")
public class AdminClickReportRequestDTO {

  public enum ReportType {
    CHURN,
    CONVERSION
  }

  @NotNull
  @PastOrPresent
  @Schema(description = "Start of date range (inclusive)", example = "2026-05-01")
  private LocalDate dateFrom;

  @NotNull
  @PastOrPresent
  @Schema(description = "End of date range (inclusive)", example = "2026-05-31")
  private LocalDate dateTo;

  @NotNull
  @Schema(description = "CHURN returns unsubscribed rows; CONVERSION returns acquired rows")
  private ReportType reportType;

  @Positive
  @Schema(
      description =
          "Churn window in hours — only used when reportType=CHURN. "
              + "Rows are included only if duration < churnDurationHours * 3600. "
              + "Defaults to 48 when omitted.",
      example = "48")
  private Integer churnDurationHours;
}
