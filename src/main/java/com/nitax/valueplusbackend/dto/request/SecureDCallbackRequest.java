package com.nitax.valueplusbackend.dto.request;

import lombok.Data;

@Data
public class SecureDCallbackRequest {
  private String msisdn;
  private String activation;
  private String productID;
  private String description;
  private String timestamp;
  private String trxId;
  private String renFlag;
  private String sequenceNo;
}
