package com.nitax.valueplusbackend.controller.external;

import com.nitax.valueplusbackend.dto.request.CreateBulkSmsCampaignRequest;
import com.nitax.valueplusbackend.dto.request.external.SendExternalBulkSmsRequest;
import com.nitax.valueplusbackend.dto.response.ApiResponse;
import com.nitax.valueplusbackend.dto.response.CreateBulkSmsCampaignResponse;
import com.nitax.valueplusbackend.dto.response.external.SendSmsResponse;
import com.nitax.valueplusbackend.service.BulkSmsCampaignService;
import com.nitax.valueplusbackend.service.ExternalService;
import com.nitax.valueplusbackend.service.WalletService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/sms")
@AllArgsConstructor
public class ExternalBulkSmsApiController {
    private final ExternalService externalService;


    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(){
        return ResponseEntity.ok().body(externalService.getExternalWalletBalance());
    }

    @PostMapping("/send-bulk")
    public ResponseEntity<?> createBulkSmsCampaign(@ModelAttribute @Valid SendExternalBulkSmsRequest request, @RequestPart(value = "csv",required = false) MultipartFile csc) throws IOException {
        SendSmsResponse response =  externalService.sendExternalBulkSms(request,csc);
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .success(true)
                .data(response)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/status/{messageId}")
    public ResponseEntity<?> querySmsStatus(@PathVariable String messageId) {
        return ResponseEntity.ok().body(externalService.querySmsStatus(messageId));
    }

    @GetMapping("/prohibited-words")
    public ResponseEntity<?> getProhibitedWordsList() {
        return ResponseEntity.ok().body(externalService.getProhibitedWordsList());
    }

    @GetMapping("/available-phone-geography")
    public ResponseEntity<?> getAvailableGeographyList() {
        return ResponseEntity.ok().body(externalService.getAvailableGeograpyhyList());
    }
}
