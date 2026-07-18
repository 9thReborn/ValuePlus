package com.nitax.valueplusbackend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@Schema(description = "A single churned subscriber record")
public class PublisherChurnRecordDTO {

    @Schema(description = "Date the unsubscribe was recorded", example = "2026-04-15")
    private LocalDate reportDate;

    @Schema(description = "Campaign the subscriber was acquired on", example = "MTN Ghana Promo")
    private String campaignName;

    @Schema(description = "Click ID (transaction ID) from the acquisition postback", example = "txn_abc123xyz")
    private String clickId;

    @Schema(description = "Source ID sent during acquisition", example = "src_001")
    private String sourceId;
}
