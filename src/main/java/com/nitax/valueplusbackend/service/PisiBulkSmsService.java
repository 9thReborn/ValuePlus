package com.nitax.valueplusbackend.service;

import com.nitax.valueplusbackend.dto.request.PisiCalculateCostRequest;
import com.nitax.valueplusbackend.dto.request.PisiSendBulkSmsRequest;
import com.nitax.valueplusbackend.dto.response.PisiAuthenticationResponse;
import com.nitax.valueplusbackend.dto.response.PisiCalculateCostResponse;
import com.nitax.valueplusbackend.dto.response.PisiGetDeliveryStatusResponse;
import com.nitax.valueplusbackend.dto.response.PisiSendSmsResponse;
import org.springframework.http.ResponseEntity;

public interface PisiBulkSmsService {
    PisiAuthenticationResponse authenticate();

    PisiSendSmsResponse sendSms(PisiSendBulkSmsRequest request);

    PisiCalculateCostResponse calculateCost(PisiCalculateCostRequest request);

    PisiGetDeliveryStatusResponse getDIR(String transactionId);
}
