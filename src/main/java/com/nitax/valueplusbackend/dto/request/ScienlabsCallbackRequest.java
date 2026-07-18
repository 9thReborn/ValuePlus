package com.nitax.valueplusbackend.dto.request;

import lombok.Data;

@Data
public class ScienlabsCallbackRequest {
  private String serviceId;
  private String serviceType;
  private String sequenceNo;
  private String resultCode;
  private String operationId;
  private String serviceNode;
  private String chargingMode;
  private String validityType;
  private String validityDays;
  private String processingTime;
  private String chargeAmount;
  private String category;
  private String contentId;
  private String keyword;
  private String renFlag;
  private String requestNo;
  private String callingParty;
  private String requestedPlan;
  private String appliedPlan;
  private String result;
  private String bearerId;
  private String traffic_source;
}
