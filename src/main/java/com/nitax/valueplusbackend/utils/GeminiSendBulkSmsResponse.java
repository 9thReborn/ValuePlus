package com.nitax.valueplusbackend.utils;

import java.util.List;

public class GeminiSendBulkSmsResponse {
    private String transactionID;
    private List<String> messageID;
    // Getters and Setters
    public String getTransactionID() { return transactionID; }
    public void setTransactionID(String transactionID) { this.transactionID = transactionID; }
    public List<String> getMessage() { return messageID; }
    public void setMessage(List<String> messageID) { this.messageID = messageID; }
}
