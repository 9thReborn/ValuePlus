package com.nitax.valueplusbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class AutoFillDTO {
    private String name;
    private String Id;
}