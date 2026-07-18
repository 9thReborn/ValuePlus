package com.nitax.valueplusbackend.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.util.List;

@Data
public class GeminiSendBulkSmsRequest {
    @JsonSerialize(using = StringListSerializer.class)
    private List<String> destinations;
    private String text;
    private String source;
    private String receiptURL;
}
