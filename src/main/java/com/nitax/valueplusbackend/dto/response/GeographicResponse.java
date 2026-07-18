package com.nitax.valueplusbackend.dto.response;

import lombok.Data;

import java.util.List;
@Data
public class GeographicResponse {
    List<String> sectors;
    List<String> states;
    List<String> cities;
    List<String> lgas;
}
