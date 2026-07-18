package com.nitax.valueplusbackend.dto.response.external;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class StatusQueryResponse {
    private String status;
    private String message;
    private String error;
}
