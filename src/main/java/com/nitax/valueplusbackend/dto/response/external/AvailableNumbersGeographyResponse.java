package com.nitax.valueplusbackend.dto.response.external;

import lombok.Data;

import java.util.List;

@Data
public class AvailableNumbersGeographyResponse {
    private String status;
    private String message;
    private List<String> city;
    private List<String> country;
    private List<String> lga;
    private List<String> sector;
}
