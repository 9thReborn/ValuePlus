package com.nitax.valueplusbackend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PisiCalculateCostRequest {
    @JsonProperty("Message")
    private String message;

    @JsonProperty("Recipients")
    private String recipients;

    MultipartFile file;
}
