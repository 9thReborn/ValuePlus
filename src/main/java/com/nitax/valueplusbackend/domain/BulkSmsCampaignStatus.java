package com.nitax.valueplusbackend.domain;

public enum BulkSmsCampaignStatus {
    SCHEDULED("SCHEDULED"),IN_PROGRESS("IN_PROGRESS"),COMPLETED("COMPLETED"),FAILED("FAILED");

    private String name;
    BulkSmsCampaignStatus(String name) {
        this.name = name;
    }
}
