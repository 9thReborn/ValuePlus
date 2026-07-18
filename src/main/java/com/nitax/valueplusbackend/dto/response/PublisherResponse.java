package com.nitax.valueplusbackend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PublisherResponse {
    private String pubId;
    private String name;
    private String pubUrl;
    private String email;
    private String website;
    private String identifier;
    private String postbackUrl;
    private String clickIdParameter;
    private String sourceIdParameter;
    private Double totalDueAmount;
    private String apiKey;
    private String status;
    private String createdAt;
    private String modifiedAt;
}
