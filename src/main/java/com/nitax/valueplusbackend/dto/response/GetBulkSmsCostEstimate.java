package com.nitax.valueplusbackend.dto.response;

import lombok.Data;

@Data
public class GetBulkSmsCostEstimate {
    private double pointsRequired;
    private double contactCount;
    private double availablePoints;
    private String status;
}
