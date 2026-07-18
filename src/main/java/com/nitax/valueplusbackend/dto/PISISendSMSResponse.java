package com.nitax.valueplusbackend.dto;

import lombok.Data;

@Data
public class PISISendSMSResponse {
  private String trxid;
  private String message;
  private String msisdn;
  private String statusCode;
  private Boolean success;
}
