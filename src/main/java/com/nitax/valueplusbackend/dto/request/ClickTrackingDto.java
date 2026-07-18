package com.nitax.valueplusbackend.dto.request;

import lombok.Data;

@Data
public class ClickTrackingDto {

  private String trxId;
  private String trfsrc;
  private String sourceId;
}
