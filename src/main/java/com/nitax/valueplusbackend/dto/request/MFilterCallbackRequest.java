package com.nitax.valueplusbackend.dto.request;

import lombok.Data;

@Data
public class MFilterCallbackRequest {
  private String trxId;
  private String clickId;
  private String trfSrc;
  private String msisdn;
  private String subscriptionId;
  private String subscriptionDescription;
  private String autoRenew;
  private String sdp_success;
  private String sourceId;
}
