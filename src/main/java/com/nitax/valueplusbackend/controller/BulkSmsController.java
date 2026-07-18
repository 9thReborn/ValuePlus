package com.nitax.valueplusbackend.controller;

import com.nitax.valueplusbackend.dto.request.CreateBulkSmsCampaignRequest;
import com.nitax.valueplusbackend.dto.request.GeminiSendBulkSmsRequest;
import com.nitax.valueplusbackend.dto.request.PisiCalculateCostRequest;
import com.nitax.valueplusbackend.dto.request.PisiSendBulkSmsRequest;
import com.nitax.valueplusbackend.dto.response.*;
import com.nitax.valueplusbackend.service.BulkSmsCampaignService;
import com.nitax.valueplusbackend.service.GeminiSmsService;
import com.nitax.valueplusbackend.service.PisiBulkSmsService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/bulk-sms")
@AllArgsConstructor
public class BulkSmsController {
    private final PisiBulkSmsService pisiBulkSmsService;
    private final GeminiSmsService geminiSmsService;
    private final BulkSmsCampaignService bulkSmsCampaignService;

    @PostMapping("/pis-send-bulk-sms")
    public ResponseEntity<?> sendPisiBulkSms(@RequestBody @Valid PisiSendBulkSmsRequest request){
        return ResponseEntity.ok().body(pisiBulkSmsService.sendSms(request));
    }

    @PostMapping("/pisi-auth")
    public ResponseEntity<?> authenticatePisi(){
        return ResponseEntity.ok().body(pisiBulkSmsService.authenticate());
    }

    @PostMapping("/pisi-calculate-cost")
    public ResponseEntity<?> calculatePisiCost(@RequestBody @Valid PisiCalculateCostRequest request){
        return ResponseEntity.ok().body(pisiBulkSmsService.calculateCost(request));
    }

    @GetMapping("/pisi-sms-delivery-status/{transactionId}")
    public ResponseEntity<?> getDeliveryStatus(@PathVariable String transactionId){
        return ResponseEntity.ok().body(pisiBulkSmsService.getDIR(transactionId));
    }

    @PostMapping("/gemini-bulk-sms")
    public ResponseEntity<?> sendGeminiBulkSms(@RequestBody @Valid GeminiSendBulkSmsRequest request){
        return ResponseEntity.ok().body(geminiSmsService.sendBulkSms(request));
    }

    @GetMapping("/gemini-sms-delivery-status")
    public  ResponseEntity<?> querySmsStatus(@RequestParam String messageId){
        return ResponseEntity.ok().body(geminiSmsService.querySmsStatus(messageId));
    }

    @GetMapping("/gemini-account-balance")
    public ResponseEntity<?> retrieveAccountBalance(){
        return ResponseEntity.ok().body(geminiSmsService.retrieveAccountBalance());
    }



    @PostMapping("/validate")
    public  ResponseEntity<?> validateAndCalculateCostFromCsv(@RequestPart(value = "csv",required = false) MultipartFile csv) throws IOException {
        GetBulkSmsCostEstimate res =  bulkSmsCampaignService.getSmsPointCostEstimateAndValidate(csv,null );
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .success(true)
                .data(res)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/calculate-cost{targetNumber}")
    public ResponseEntity<?> calculateSmsCost(@PathVariable long targetNumber){
        GetBulkSmsCostEstimate res =  bulkSmsCampaignService.getSmsPointCostEstimate(targetNumber,null );
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .success(true)
                .data(res)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createBulkSmsCampaign(@ModelAttribute @Valid CreateBulkSmsCampaignRequest request, @RequestPart(value = "csv",required = false) MultipartFile csc) throws IOException {
        CreateBulkSmsCampaignResponse response =  bulkSmsCampaignService.createBulkSms(request,csc);
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .success(true)
                .data(response)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/dashboard-summary")
    public ResponseEntity<?> getDashboardSummary(){
        BulkSmsDashboardSummaryDto response =  bulkSmsCampaignService.getDashboardSummary();
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .success(true)
                .data(response)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/campaign-details/{bulkSmsCampaignId}")
    public ResponseEntity<?> getCampaignDetails(@PathVariable String bulkSmsCampaignId){
        BulkSmsCampaignResponse response =  bulkSmsCampaignService.getBulkSmsCampaignDetailsByCampaignId(bulkSmsCampaignId);
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .success(true)
                .data(response)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }


    @GetMapping("/campaign-stats")
    public ResponseEntity<?> getCampaignStats(@RequestParam(value = "startDate",required = false)Instant startDate, @RequestParam(value = "endDate", required = false)Instant endDate){
        BulkSmsCampaignManagementResponse response =  bulkSmsCampaignService.getAdvertiserCampaignStats(startDate,endDate);
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .success(true)
                .data(response)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/campaigns")
    public ResponseEntity<?> getCampaigns(
            @RequestParam("page")int page,
            @RequestParam("size") int size,
            @RequestParam(name = "startDate",required = false) Instant startDate,
            @RequestParam(name = "endDate",required = false)  Instant endDate,
            @RequestParam(name = "name", required = false) String name){
        List<BulkSmsCampaignResponse> response =  bulkSmsCampaignService.getCampaigns(page,size,startDate,endDate,name );
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .success(true)
                .data(response)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/geographic")
    public ResponseEntity<?> getGeographies(){
        GeographicResponse res =  bulkSmsCampaignService.getSystemNumbersGeographicDetails();
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .success(true)
                .data(res)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}
