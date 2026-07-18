package com.nitax.valueplusbackend.dto.response;

import lombok.Data;

import java.util.List;
@Data
public class GeminiSendBulkSmsResponse {
    private String transactionID;
    private List<String> messageID;
}
