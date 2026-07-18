package com.nitax.valueplusbackend.dto;

import com.univocity.parsers.annotations.Parsed;
import lombok.Data;

@Data
public class PhoneNumberTemplateDto {
  @Parsed(field = "originating_lga")
  private String originatingLga;

  @Parsed(field = "originating_city")
  private String originatingCity;

  @Parsed(field = "sub_msisdn")
  private String subMsisdn;
}
