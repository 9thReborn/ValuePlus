package com.nitax.valueplusbackend.utils;

import java.util.List;

public class GeminiSendBulkSmsRequest {
    private List<String> destinations;
    private String text;
    private String source;
    private String receiptURL;

    // Getters and Setters
    public List<String> getDestinations() { return destinations; }
    public void setDestinations(List<String> destinations) { this.destinations = destinations; }
    public String getText() { return text; }
    public void setContent(String content) { this.text = content; }
    public String getSource() { return source; }
    public void setSource(String senderId) { this.source = senderId; }
    public String getReceiptURL() { return receiptURL; }
    public void setReceiptURL(String receiptURL) { this.receiptURL = receiptURL; }

}
