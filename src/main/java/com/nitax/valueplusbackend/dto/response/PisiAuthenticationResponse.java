package com.nitax.valueplusbackend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PisiAuthenticationResponse {
    @JsonProperty("Success")
    private boolean success;

    @JsonProperty("StatusCode")
    private String statusCode;

    @JsonProperty("Message")
    private String message;

    @JsonProperty("Provider")
    private String provider;

    @JsonProperty("Pisi-authorization-token")
    private String pisiAuthorizationToken;

    @JsonProperty("Pisisid")
    private int pisisid;

    @JsonProperty("Expiration")
    private String expiration;
}