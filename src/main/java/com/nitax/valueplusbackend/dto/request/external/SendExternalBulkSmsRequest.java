package com.nitax.valueplusbackend.dto.request.external;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class SendExternalBulkSmsRequest {
    private List<String> destinations;
    private String name;
    private String content;
    private String source;
    private boolean systemUser;
    private String country;
    private int numberOfTarget;
    private String lga;
    private String city;
    private String state;
    private String sector;
    private List<String> excludedNumbers;
}
