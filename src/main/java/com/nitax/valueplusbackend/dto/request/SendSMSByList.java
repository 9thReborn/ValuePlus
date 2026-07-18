package com.nitax.valueplusbackend.dto.request;

import java.util.List;
import lombok.Data;

@Data
public class SendSMSByList {

  private List<String> msisdnList;
  private String message;
}
