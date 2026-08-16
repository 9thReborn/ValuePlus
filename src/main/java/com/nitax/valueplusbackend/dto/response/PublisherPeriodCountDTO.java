package com.nitax.valueplusbackend.dto.response;

import lombok.Data;

@Data
public class PublisherPeriodCountDTO {

    private final String publisherId;
    private final long count;

    public PublisherPeriodCountDTO(String publisherId, long count) {
        this.publisherId = publisherId;
        this.count = count;
    }
}
