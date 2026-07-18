package com.nitax.valueplusbackend.controller;

import com.nitax.valueplusbackend.domain.*;
import com.nitax.valueplusbackend.dto.request.*;
import com.nitax.valueplusbackend.dto.response.*;
import com.nitax.valueplusbackend.service.*;
import com.nitax.valueplusbackend.service.impl.UserDetailsImpl;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/bulkSms")
@CrossOrigin
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class BulkSmsAdminController {
    private final AdminService adminService;
    private final BulkSmsCampaignService bulkSmsCampaignService;
    private final TransactionService transactionService;
    private final ProhibitedWordService prohibitedWordService;
    private final SystemExcludedNumberService systemExcludedNumberService;
    private final SmsProviderService smsProviderService;

    @GetMapping("/dashboard-summary")
    public ResponseEntity<ApiResponse<AdminBulkSmsDashboardSummary>>getBulkSmsDashboardSummary() {
        AdminBulkSmsDashboardSummary summary = adminService.getBulkSmsDashboardSummary();
        ApiResponse<AdminBulkSmsDashboardSummary> response = new ApiResponse<>(
                true,
                summary
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/advertisers")
    public ResponseEntity<ApiResponse<List<?>>> getBulkSMSAdvertisers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        List<BulkSMSAdvertiserResponse> advertisers =  adminService.getBulkSMSAdvertisers(page, size);

        ApiResponse<List<?>> response = new ApiResponse<>(
                true,
                advertisers
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-bulkSMS-campaign")
    public ResponseEntity<ApiResponse<CreateBulkSmsCampaignResponse>> createBulkSmsCampaign(@ModelAttribute @Valid CreateBulkSmsCampaignRequest request, @RequestPart(value = "csv",required = false) MultipartFile csv) throws IOException {
        ApiResponse<CreateBulkSmsCampaignResponse> response = new ApiResponse<>(
                true,
                bulkSmsCampaignService.createBulkSms(request,csv)
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate/{advertiserId}")
    public  ResponseEntity<?> validateAndCalculateCostFromCsv(@PathVariable String advertiserId,@RequestPart(value = "csv",required = false) MultipartFile csv) throws IOException {
        GetBulkSmsCostEstimate res =  bulkSmsCampaignService.getSmsPointCostEstimateAndValidate(csv,advertiserId);
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .success(true)
                .data(res)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/calculate-cost/{targetNumber}")
    public ResponseEntity<?> calculateSmsCost(@PathVariable long targetNumber,@RequestParam String advertiserId){
        GetBulkSmsCostEstimate res =  bulkSmsCampaignService.getSmsPointCostEstimate(targetNumber,advertiserId );
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .success(true)
                .data(res)
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

    @GetMapping("points-info")
    public ResponseEntity<ApiResponse<?>> getPointsInfo(@RequestParam String advertiserId, @RequestParam BigDecimal amountPaid) {
        SmsPointInfoResponse response = adminService.getSmsPointInfo(advertiserId, amountPaid);
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .success(true)
                .data(response)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/assign-points")
    public ResponseEntity<?> fundWallet(@RequestBody FundWalletRequest request) {
        adminService.fundWallet(request);
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .success(true)
                .data("Wallet funded successfully")
                .build();
        return ResponseEntity.ok().body(apiResponse);
    }

    @GetMapping("/transactions/{advertiserId}")
    public ResponseEntity<ApiResponse<List<AdvertiserTransactionResponse>>> getAdvertiserTransactions(
            @PathVariable String advertiserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        List<AdvertiserTransactionResponse> transactions = transactionService.getAdvertiserTransactions(advertiserId, page, pageSize);
        ApiResponse<List<AdvertiserTransactionResponse>> response = new ApiResponse<>(
                true,
                transactions
        );

        return ResponseEntity.ok(response);

    }

    @GetMapping("wallet-info/{advertiserId}")
    public ResponseEntity<ApiResponse<?>> getWalletBalance(@PathVariable String advertiserId) {
        SmsPointInfoResponse response = adminService.getAdvertiserSmsPointInfo(advertiserId);
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .success(true)
                .data(response)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/update-advertiser/{advertiserId}")
    public ResponseEntity<ApiResponse<Advertiser>> updateAdvertiser(@PathVariable String advertiserId, @RequestBody @Valid AdvertiserUpdateRequest request) {
        Advertiser updatedAdvertiser = adminService.updateAdvertiser(advertiserId, request);
        ApiResponse<Advertiser> response = new ApiResponse<>(
                true,
                updatedAdvertiser
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/campaigns-summary")
    public ResponseEntity<ApiResponse<?>> getAdminCampaignSummary(
            @RequestParam(value = "startDate", required = false) String startDateString,
            @RequestParam(value = "endDate", required = false) String endDateString) {
        AdminCampaignSummaryResponse response = bulkSmsCampaignService.getAdminCampaignSummary(startDateString, endDateString);
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .success(true)
                .data(response)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/bulkSMS-campaigns")
    public ResponseEntity<ApiResponse<List<AdminAdvertiserCampaignResponse>>> getAdminAdvertiserCampaigns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "startDate", required = false) String startDateString,
            @RequestParam(value = "endDate", required = false) String endDateString) {
        List<AdminAdvertiserCampaignResponse> campaigns = bulkSmsCampaignService.getAdminAdvertiserCampaigns(page, size, startDateString, endDateString);
        ApiResponse<List<AdminAdvertiserCampaignResponse>> response = new ApiResponse<>(
                true,
                campaigns
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/bulkSMS-campaigns/{campaignId}")
    public ResponseEntity<ApiResponse<?>> getBulkSmsCampaignDetails(@PathVariable String campaignId) {
        BulkSmsCampaignResponse response = bulkSmsCampaignService.getBulkSmsCampaignDetailsByCampaignId(campaignId);
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .success(true)
                .data(response)
                .build();
        return ResponseEntity.ok(apiResponse);

    }

    @PostMapping("/prohibited-words/add")
    public ResponseEntity<ApiResponse<?>> addProhibitedWord(@RequestParam String word) {
        ProhibitedWord prohibitedWord = prohibitedWordService.addNewKeyWord(word);
        ApiResponse<?> response = new ApiResponse<>(
                true,
                prohibitedWord
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/prohibited-words")
    public ResponseEntity<ApiResponse<List<ProhibitedWord>>> getAllProhibitedWords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<ProhibitedWord> prohibitedWords = prohibitedWordService.getAllProhibitedWord(page, size);
        ApiResponse<List<ProhibitedWord>> response = new ApiResponse<>(
                true,
                prohibitedWords);
        return ResponseEntity.ok(response);

    }

    @DeleteMapping("/prohibited-words/{id}")
    public ResponseEntity<ApiResponse<?>> deleteProhibitedWord(@PathVariable long id) {
        prohibitedWordService.deleteProhibitedWordById(id);
        ApiResponse<?> response = new ApiResponse<>(
                true,
                "Prohibited word deleted successfully"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/bulkSMSCampaigns")
    public ResponseEntity<ApiResponse<?>> getBulkSMSCampaigns(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        List<BulkSmsCampaignResponse> bulkSmsCampaigns =  bulkSmsCampaignService.findAll(page, size);

        ApiResponse<?> response = new ApiResponse<>(
                true,
                bulkSmsCampaigns
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/optimal-deliveryTimes")
    public  ResponseEntity<?> getHighestDeliveryRateByHour() {
        List<HourlyDeliveryRate> deliveryRates =  bulkSmsCampaignService.getTop3DeliveryRatesByHour();
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .success(true)
                .data(deliveryRates)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/optimalPerformance-rates")
    public ResponseEntity<?> getCampaignDeliveryRates() {
        List<CampaignDeliveryRate> deliveryRates =  bulkSmsCampaignService.getCampaignDeliveryRates();
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .success(true)
                .data(deliveryRates)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/live-delivery-status")
    public ResponseEntity<ApiResponse<List<SMSDeliveryStatusRes>>> getLiveDeliveryStatus(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        List<SMSDeliveryStatusRes> deliveryStatus = bulkSmsCampaignService.getLiveDeliveryStatus(page, size, startDate, endDate);
        ApiResponse<List<SMSDeliveryStatusRes>> response = new ApiResponse<>(
                true,
                deliveryStatus
        );
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @GetMapping("/generate-reports")
    public ResponseEntity<ApiResponse<List<?>>> generateReports(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        List<BulkSMSReportResponse> reports = bulkSmsCampaignService.generateReports(startDate, endDate);
        ApiResponse<List<?>> response = new ApiResponse<>(
                true,
                reports
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/excluded-numbers/add")
    public ResponseEntity<ApiResponse<?>> addExcludedNumbers(@ModelAttribute AddExcludedNumbersRequest request,@RequestPart(value = "csv",required = false) MultipartFile file) throws IOException {
        List<SystemExcludedNumber> excludedNumbers = systemExcludedNumberService.addExcludedNumbers(request.getNumbers(), file,getCurrentAdvertiser());
        ApiResponse<?> response = new ApiResponse<>(
                true,
                excludedNumbers
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/excluded-numbers/remove/{id}")
    public ResponseEntity<ApiResponse<?>> addExcludedNumbers(@PathVariable long id) {
        systemExcludedNumberService.removeExcludedNumber(id);
        ApiResponse<?> response = new ApiResponse<>(
                true,
                "Number removed from excluded list successfully"
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/excluded-numbers")
    public  ResponseEntity<ApiResponse<List<?>>> getExcludedNumbers() {
        List<SystemExcludedNumber> excludedNumbers = systemExcludedNumberService.findAll();
        ApiResponse<List<?>> response = new ApiResponse<>(
                true,
                excludedNumbers
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/providers")
    public ResponseEntity<ApiResponse<List<?>>> getProviders() {
        List<SmsProvider> providers = smsProviderService.findAll();
        ApiResponse<List<?>> response = new ApiResponse<>(
                true,
                providers
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/provider")
    public ResponseEntity<ApiResponse<?>> getCurrentProvider() {
        SmsProvider provider = smsProviderService.getCurrentProvider();

        ApiResponse<?> response = new ApiResponse<>(
                true,
                provider
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/provider/{id}")
    public ResponseEntity<ApiResponse<?>> addProvider(@PathVariable long id){
        SmsProvider smsProvider = smsProviderService.setCurrentProvider(id);
        ApiResponse<?> response = new ApiResponse<>(
                true,
                smsProvider
        );

        return ResponseEntity.ok(response);
    }


    private Admin getCurrentAdvertiser(){
        String adminEmail = "";
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            adminEmail =  userDetails.getEmail();
        }
        return adminService.getAdminByEmail(adminEmail);
    }
}
