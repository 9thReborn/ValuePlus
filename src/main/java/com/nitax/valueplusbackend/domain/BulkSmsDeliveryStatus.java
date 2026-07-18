package com.nitax.valueplusbackend.domain;

public enum BulkSmsDeliveryStatus {
    DND("DND"),DELIVERED("DELIVERED"),FAILED("FAILED")
    ;

    private String name;
    BulkSmsDeliveryStatus(String name) {
        this.name = name;
    }
}
