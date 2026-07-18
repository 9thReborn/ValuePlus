package com.nitax.valueplusbackend.dto;

import lombok.Data;

@Data
public class SecureDNotificationDto {
  private String msisdn;
  private String activation;
  private String productID;
  private String description;
  private String timestamp;
  private String trxId; // format is campaignid_publisherid_clickidSRCIDsourceid
  private String advertiserId;
  private String campaignId;
}
