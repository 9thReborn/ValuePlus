package com.nitax.valueplusbackend.service;

import com.nitax.valueplusbackend.dto.request.external.SendExternalBulkSmsRequest;
import com.nitax.valueplusbackend.dto.response.external.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ExternalService {
    GetWalletBalanceResponse getExternalWalletBalance();
     SendSmsResponse sendExternalBulkSms(SendExternalBulkSmsRequest request, MultipartFile csvFile) throws IOException;

     StatusQueryResponse querySmsStatus(String messageId);

     GetProhibitedWordsListResponse getProhibitedWordsList();

     AvailableNumbersGeographyResponse getAvailableGeograpyhyList();
}
