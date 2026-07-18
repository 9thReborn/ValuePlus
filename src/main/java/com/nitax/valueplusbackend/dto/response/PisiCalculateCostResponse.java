package com.nitax.valueplusbackend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PisiCalculateCostResponse {
    @JsonProperty("Success")
    private boolean success;
    @JsonProperty("StatusCode")
    private String statusCode;
    @JsonProperty("Message")
    private String message;
    @JsonProperty("TotalCost")
    private double totalCost;
    @JsonProperty("CurrentBalance")
    private double currentBalance;

    @JsonProperty("ProviderCounts")
    private PisiCalculateCostPropertiesProperties ProviderCounts;
    @JsonProperty("Summary")
    private String summary;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public double getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(double currentBalance) {
        this.currentBalance = currentBalance;
    }

    public PisiCalculateCostPropertiesProperties getProperties() {
        return ProviderCounts;
    }

    public void setProperties(PisiCalculateCostPropertiesProperties providerCounts) {
        this.ProviderCounts = providerCounts;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    static class PisiCalculateCostProperties{

         private PisiCalculateCostPropertiesProperties properties;

        public PisiCalculateCostPropertiesProperties getProperties() {
            return properties;
        }

        public void setProperties(PisiCalculateCostPropertiesProperties properties) {
            this.properties = properties;
        }
    }

    static class  PisiCalculateCostPropertiesProperties{
        @JsonProperty("MTN")

        private long MTN;
        @JsonProperty("Airtel")

        private long Airtel;
        @JsonProperty("Glo")
        private long Glo;
        @JsonProperty("Unknown")
        private long Unknown;

        public long getMTN() {
            return MTN;
        }

        public void setMTN(long MTN) {
            this.MTN = MTN;
        }

        public long getAirtel() {
            return Airtel;
        }

        public void setAirtel(long airtel) {
            Airtel = airtel;
        }

        public long getGlo() {
            return Glo;
        }

        public void setGlo(long glo) {
            Glo = glo;
        }

        public long getUnknown() {
            return Unknown;
        }

        public void setUnknown(long unknown) {
            Unknown = unknown;
        }
    }

}
