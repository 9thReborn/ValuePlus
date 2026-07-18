package com.nitax.valueplusbackend.dto.response.external;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetProhibitedWordsListResponse {
    private String status;
    private String message;
    private String prohibitedWords;
}
