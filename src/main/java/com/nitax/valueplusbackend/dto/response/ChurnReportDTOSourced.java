package com.nitax.valueplusbackend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;


@Builder
@Data
@AllArgsConstructor
@Schema(description = "Daily churn and acquisition totals for a campaign and source ID")
public final class ChurnReportDTOSourced implements ChurnReport {

    @Schema(description = "Date of the report row", example = "2026-04-15")
    private LocalDate reportDate;

    @Schema(description = "Publisher name", example = "Acme Publishers")
    private String publisherName;

    @Schema(description = "Campaign name", example = "MTN Ghana Promo")
    private String campaignName;

    @Schema(description = "Traffic source ID", example = "src_001")
    private String sourceId;

    @Schema(description = "Number of successful acquisitions on this day", example = "120")
    private int acquisition;

    @Schema(description = "Number of subscribers who churned within 8 days of acquisition", example = "14")
    private int churned;
}
