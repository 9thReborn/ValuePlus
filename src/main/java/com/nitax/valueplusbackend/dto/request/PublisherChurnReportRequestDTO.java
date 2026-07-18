package com.nitax.valueplusbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "Request body for generating a publisher churn report")
public class PublisherChurnReportRequestDTO implements Serializable {

    @NotBlank
    @Schema(description = "Your publisher API key", example = "a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4", requiredMode = Schema.RequiredMode.REQUIRED)
    private String apiKey;

    @PastOrPresent
    @Schema(description = "Start of the reporting period (inclusive)", example = "2026-04-01T00:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime startDate;

    @PastOrPresent
    @Schema(description = "End of the reporting period (inclusive)", example = "2026-04-30T23:59:59", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime endDate;
}
