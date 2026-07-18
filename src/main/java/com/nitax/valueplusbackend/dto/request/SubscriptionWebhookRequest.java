package com.nitax.valueplusbackend.dto.request;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class SubscriptionWebhookRequest {

  private String msisdn;

  private String serviceId;

  private String eventType;

  private String campaignId;

  private Boolean renewalFlag;

  private String trxId;

  private String timestamp;

  // Revenue fields
  private BigDecimal billingAmount;

  private String currency;

  private String billingCycle;

  /**
   * Captures any additional fields not explicitly defined.
   * This allows us to store the complete raw payload.
   */
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<>();

  @JsonAnySetter
  public void setAdditionalProperty(String key, Object value) {
    additionalProperties.put(key, value);
  }
}
