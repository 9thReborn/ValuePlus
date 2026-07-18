package com.nitax.valueplusbackend.dto.response;

import lombok.Data;

@Data
public class PisiSendSmsResponse {
    private String customStatus;
    private String message;
    private String transactionId;

    public PisiSendSmsResponse(String message){
        this.message = message;
    }
}
