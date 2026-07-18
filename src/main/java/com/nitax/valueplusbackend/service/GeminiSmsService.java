package com.nitax.valueplusbackend.service;

import com.nitax.valueplusbackend.dto.request.GeminiSendBulkSmsRequest;
import com.nitax.valueplusbackend.dto.response.GeminiQuerySmsStatusResponse;
import com.nitax.valueplusbackend.dto.response.GeminiRetrieveAccountBalanceResponse;
import com.nitax.valueplusbackend.dto.response.GeminiSendBulkSmsResponse;

public interface GeminiSmsService {
    GeminiSendBulkSmsResponse sendBulkSms(GeminiSendBulkSmsRequest sendBulkSmsRequest);
    GeminiQuerySmsStatusResponse querySmsStatus(String messageId);

    GeminiRetrieveAccountBalanceResponse retrieveAccountBalance();

}
