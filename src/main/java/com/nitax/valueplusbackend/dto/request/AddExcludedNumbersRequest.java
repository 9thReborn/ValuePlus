package com.nitax.valueplusbackend.dto.request;

import lombok.Data;

import java.util.List;
@Data
public class AddExcludedNumbersRequest {
    List<String> numbers;
}
