package com.nitax.valueplusbackend.dto.request;

import lombok.Data;

@Data
public class RetentionStatDto {
  private int numberOfDays;
  private String campaignId;
}
