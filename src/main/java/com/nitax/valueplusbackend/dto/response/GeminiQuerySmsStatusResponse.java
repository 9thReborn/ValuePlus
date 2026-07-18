package com.nitax.valueplusbackend.dto.response;

import lombok.Data;

@Data
public class GeminiQuerySmsStatusResponse {
    private String status;
    private String error;
    private String statusText;
    private String ts;
}
