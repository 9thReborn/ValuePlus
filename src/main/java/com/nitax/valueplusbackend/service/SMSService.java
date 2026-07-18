package com.nitax.valueplusbackend.service;

import com.nitax.valueplusbackend.domain.SMSLog;
import com.nitax.valueplusbackend.dto.PISISendSMSResponse;
import com.nitax.valueplusbackend.dto.request.SendSMSByList;

public interface SMSService {
  SMSLog sendBulkSmsFromList(SendSMSByList sendSMSByList);

  SMSLog logSMS(SMSLog log);

  PISISendSMSResponse sendSMS(String number, String text);
}
