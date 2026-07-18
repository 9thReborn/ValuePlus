package com.nitax.valueplusbackend.controller;

import com.nitax.valueplusbackend.domain.Advertiser;
import com.nitax.valueplusbackend.domain.Campaign;
import com.nitax.valueplusbackend.dto.CampaignDetailsDTO;
import com.nitax.valueplusbackend.dto.ReportingChartDto;
import com.nitax.valueplusbackend.dto.SecureDNotificationDto;
import com.nitax.valueplusbackend.dto.request.*;
import com.nitax.valueplusbackend.dto.response.*;
import com.nitax.valueplusbackend.exception.AppException;
import com.nitax.valueplusbackend.service.AdvertiserService;
import com.nitax.valueplusbackend.service.ClicksConversionsService;
import com.nitax.valueplusbackend.service.impl.UserDetailsImpl;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/advertisers")
@CrossOrigin
@Slf4j
public class AdvertiserController {

  private final AdvertiserService advertiserService;
  private final ClicksConversionsService clicksConversionsService;

  private String getUsernameFromSecurityContext() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new InsufficientAuthenticationException(
          "Authentication object is missing or not authenticated");
    }

    Object principal = authentication.getPrincipal();
    if (principal instanceof UserDetailsImpl) {
      return ((UserDetailsImpl) principal).getEmail();
    } else {
      throw new InsufficientAuthenticationException(
          "Principal is not an instance of UserDetailsImpl");
    }
  }

  @PostMapping("/signup")
  public ResponseEntity<ApiResponse<String>> signup(
      @Valid @RequestBody AdvertiserSignupDTO signupDTO) {
    String authToken = advertiserService.createAdvertiser(signupDTO);
    ApiResponse<String> response =
        ApiResponse.<String>builder().success(true).data(authToken).build();

    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @GetMapping("/verify/{token}")
  public ResponseEntity<ApiResponse<String>> verifySignupToken(@PathVariable String token) {
    String authToken = advertiserService.verifySignupToken(token);

    ApiResponse<String> response =
        ApiResponse.<String>builder().success(true).data(authToken).build();
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @PostMapping("/signin")
  public ResponseEntity<ApiResponse<LoginResponse>> login(
      @Valid @RequestBody AdvertiserSignInDTO advertiserSignInDTO) {
    ApiResponse<LoginResponse> response = advertiserService.loginAdvertiser(advertiserSignInDTO);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @SecurityRequirement(name = "bearerAuth")
  @GetMapping("/me")
  public ResponseEntity<ApiResponse<Advertiser>> getAdvertiserDetails() {
    String username = getUsernameFromSecurityContext();
    Advertiser advertiser = advertiserService.getAdvertiserDetails(username);
    ApiResponse<Advertiser> apiResponse =
        ApiResponse.<Advertiser>builder().success(true).data(advertiser).build();
    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @SecurityRequirement(name = "bearerAuth")
  @GetMapping("stats/campaign/total")
  public ResponseEntity<ApiResponse<CampaignSummaryDTO>> getTotalCampaignStats() {
    String username = getUsernameFromSecurityContext();

    CampaignSummaryDTO response = advertiserService.getTotalCampaignStats(username);
    ApiResponse<CampaignSummaryDTO> apiResponse =
        ApiResponse.<CampaignSummaryDTO>builder().success(true).data(response).build();
    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @SecurityRequirement(name = "bearerAuth")
  @GetMapping("stats/campaign/active")
  public ResponseEntity<ApiResponse<CampaignSummaryDTO>> getActiveCampaignStats() {
    String username = getUsernameFromSecurityContext();

    CampaignSummaryDTO response = advertiserService.getActiveCampaignStats(username);
    ApiResponse<CampaignSummaryDTO> apiResponse =
        ApiResponse.<CampaignSummaryDTO>builder().success(true).data(response).build();
    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @SecurityRequirement(name = "bearerAuth")
  @GetMapping("stats/campaign/analytics")
  public ResponseEntity<ApiResponse<List<CampaignAnalyticsResponseDto>>> getAnalyticsCampaignStats(
      Integer year) {
    String username = getUsernameFromSecurityContext();

    year = year == null ? LocalDate.now().getYear() : year;

    List<CampaignAnalyticsResponseDto> response =
        advertiserService.getAnalyticsCampaignStats(username, year);
    ApiResponse<List<CampaignAnalyticsResponseDto>> apiResponse =
        ApiResponse.<List<CampaignAnalyticsResponseDto>>builder()
            .success(true)
            .data(response)
            .build();
    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @SecurityRequirement(name = "bearerAuth")
  @GetMapping("stats/spend")
  public ResponseEntity<ApiResponse<SpendSummaryDto>> getSpendStats() {
    String username = getUsernameFromSecurityContext();

    SpendSummaryDto response = advertiserService.getSpendStats(username);
    ApiResponse<SpendSummaryDto> apiResponse =
        ApiResponse.<SpendSummaryDto>builder().success(true).data(response).build();
    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @SecurityRequirement(name = "bearerAuth")
  @GetMapping("campaigns")
  public Page<Campaign> findAllCampaigns(CampaignFilter filter, Pageable pageable) {
    String username = getUsernameFromSecurityContext();

    return advertiserService.findAllCampaigns(filter, pageable, username);
  }

  @GetMapping("campaigns/{campaignId}")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<Campaign>> getCampaignDetails(@PathVariable String campaignId) {

    String username = getUsernameFromSecurityContext();

    Campaign campaignDetailsDTO = advertiserService.getCampaignDetails(campaignId, username);

    ApiResponse<Campaign> response =
        ApiResponse.<Campaign>builder().success(true).data(campaignDetailsDTO).build();

    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @SecurityRequirement(name = "bearerAuth")
  @PostMapping(
      value = "campaigns/upload-campaign-image",
      consumes = {"multipart/form-data"})
  public ResponseEntity<ApiResponse<String>> uploadCampaignImage(
      @RequestPart("file") MultipartFile file) throws IOException {
    long MAX_FILE_SIZE = 1024 * 1024 * 5; // 5MB maximum size
    List<String> ALLOWED_CONTENT_TYPES = Arrays.asList("image/png", "image/jpeg", "image/jpg");

    if (file.isEmpty()) {
      throw new AppException("Please select a file to upload.");
    }

    if (file.getSize() > MAX_FILE_SIZE) {
      throw new AppException("File size exceeds the maximum limit of 5MB.");
    }

    if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
      throw new AppException("Only PNG, JPG and JPEG files are allowed.");
    }
    String uploadedImage = advertiserService.uploadCampaignImage(file);
    /*String uploadedImage =
    "https://fastly.picsum.photos/id/612/200/200.jpg?hmac=HbIkwJ0QBqhSlGTi3bnF4JFTp9BntF-teQZUQhpqWyM";*/
    ApiResponse<String> response = new ApiResponse<>(true, uploadedImage);

    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @SecurityRequirement(name = "bearerAuth")
  @PostMapping(value = "campaigns")
  public ResponseEntity<ApiResponse<Campaign>> createCampaign(
      @Valid @RequestBody CreateCampaignDTO createCampaignDTO) throws IOException {
    String username = getUsernameFromSecurityContext();
    Campaign createdCampaign = advertiserService.createCampaign(createCampaignDTO, username);

    ApiResponse<Campaign> response = new ApiResponse<>(true, createdCampaign);

    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @SecurityRequirement(name = "bearerAuth")
  @PutMapping(value = "campaigns/{campaignId}")
  public ResponseEntity<ApiResponse<Campaign>> editCampaign(
      @Valid @RequestBody UpdateCampaignDTO editCampaignDTO, @PathVariable String campaignId)
      throws IOException {

    Campaign updatedCampaign = advertiserService.editCampaign(editCampaignDTO, campaignId);

    ApiResponse<Campaign> response = new ApiResponse<>(true, updatedCampaign);

    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @SecurityRequirement(name = "bearerAuth")
  @PostMapping("/campaigns/deactivate/{campaignId}")
  public ResponseEntity<ApiResponse<String>> deactivateCampaign(@PathVariable String campaignId) {

    String response = advertiserService.deactivateCampaign(campaignId);

    ApiResponse<String> apiResponse =
        ApiResponse.<String>builder().success(true).data(response).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @SecurityRequirement(name = "bearerAuth")
  @PostMapping("/campaigns/activate/{campaignId}")
  public ResponseEntity<ApiResponse<String>> activateCampaign(@PathVariable String campaignId) {

    String response = advertiserService.activateCampaign(campaignId);

    ApiResponse<String> apiResponse =
        ApiResponse.<String>builder().success(true).data(response).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @SecurityRequirement(name = "bearerAuth")
  @DeleteMapping("/campaigns/{campaignId}")
  public ResponseEntity<ApiResponse<String>> deleteCampaign(@PathVariable String campaignId) {

    String response = advertiserService.deleteCampaign(campaignId);

    ApiResponse<String> apiResponse =
        ApiResponse.<String>builder().success(true).data(response).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @SecurityRequirement(name = "bearerAuth")
  @GetMapping("conversions/advertisers")
  public ResponseEntity<ApiResponse<List<AdvertiserConversionDTO>>> getAdvertiserConversions(
      AdvertiserConversionReportRequestDTO dto) {

    String username = getUsernameFromSecurityContext();
    List<AdvertiserConversionDTO> response =
        advertiserService.getAdvertiserConversions(username, dto);

    ApiResponse<List<AdvertiserConversionDTO>> apiResponse =
        ApiResponse.<List<AdvertiserConversionDTO>>builder().success(true).data(response).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @SecurityRequirement(name = "bearerAuth")
  @GetMapping("reports")
  public ResponseEntity<ApiResponse<List<ReportingChartDto>>> getAdvertiserReports(
      @Valid ReportChartRequestDto dto) {

    String username = getUsernameFromSecurityContext();

    List<ReportingChartDto> response = advertiserService.getAdvertiserReports(username, dto);
    ApiResponse<List<ReportingChartDto>> apiResponse =
        ApiResponse.<List<ReportingChartDto>>builder().success(true).data(response).build();
    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @SecurityRequirement(name = "bearerAuth")
  @GetMapping("reports/summary")
  public ResponseEntity<ApiResponse<List<ReportingSummaryDto>>> getAdvertiserReportsSummary(
      @Valid ReportSummaryRequestDto dto) {

    String username = getUsernameFromSecurityContext();

    List<ReportingSummaryDto> response =
        advertiserService.getAdvertiserReportsSummary(username, dto);
    ApiResponse<List<ReportingSummaryDto>> apiResponse =
        ApiResponse.<List<ReportingSummaryDto>>builder().success(true).data(response).build();
    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  //  ================ NO ENDPOINT BEYOND THIS
  // LINE=======================================================
  @GetMapping("/conv")
  public ResponseEntity<ApiResponse<String>> handleAdvertiserPostback2(
          @RequestParam String trxId,
          @RequestParam(required = false) String sourceId,
          @RequestParam(required = false) String msisdn,
          @RequestParam(required = false) String status) {

    // A null-safe way to check the status.
    if ("ACTIVE".equalsIgnoreCase(status)){
      clicksConversionsService.handleAdvertiserPostbackByGET2(
              trxId, sourceId, msisdn, "Success", "1");

      ApiResponse<String> apiResponse =
              ApiResponse.<String>builder().success(true).data("Postback handled successfully").build();

      return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    } else {
      clicksConversionsService.handleAdvertiserPostbackByGET2(
              trxId, sourceId, msisdn, "Success", "1");
      ApiResponse<String> apiResponse =
              ApiResponse.<String>builder().success(true).data("Postback handled successfully").build();
      return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
  }

  @GetMapping("/{advertiserId}/{campaignId}")
  public ResponseEntity<ApiResponse<String>> handleAdvertiserPostback(
      @PathVariable String advertiserId,
      @PathVariable String campaignId,
      @RequestParam String trxId,
      @RequestParam(required = false) String sourceId,
      @RequestParam(required = false) String msisdn) {

    clicksConversionsService.handleAdvertiserPostbackByGET(
        advertiserId, campaignId, trxId, sourceId, msisdn, "Success", "1");

    ApiResponse<String> apiResponse =
        ApiResponse.<String>builder().success(true).data("Postback handled successfully").build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @PostMapping("/conv/{advertiserId}")
  public ResponseEntity<ApiResponse<String>> handleAdvertiserPostback(
      @RequestBody SecureDNotificationDto secureDNotificationDto,
      @PathVariable String advertiserId) {

    log.info("secureDNotificationDto: {}", secureDNotificationDto);

    secureDNotificationDto.setAdvertiserId(advertiserId);

    String realCampaignId = secureDNotificationDto.getTrxId().split("_")[0];
    secureDNotificationDto.setCampaignId(realCampaignId);
    clicksConversionsService.handleSecureDWebhook(secureDNotificationDto);

    ApiResponse<String> apiResponse =
        ApiResponse.<String>builder().success(true).data("Postback handled successfully").build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @PostMapping("/{advertiserId}/{campaignId}")
  public ResponseEntity<ApiResponse<String>> handleAdvertiserPostback(
      @RequestBody SecureDNotificationDto secureDNotificationDto,
      @PathVariable String advertiserId,
      @PathVariable String campaignId) {

    log.info("secureDNotificationDto: {}", secureDNotificationDto);

    secureDNotificationDto.setAdvertiserId(advertiserId);
    secureDNotificationDto.setCampaignId(
        campaignId); // redundant now since advertiser is using one postback for all their services,
    // we'll not append the different campaign id in the transaction id. will
    // improve in the future as it's impossible to modify this url now.

    String realCampaignId = secureDNotificationDto.getTrxId().split("_")[0];
    secureDNotificationDto.setCampaignId(realCampaignId);
    clicksConversionsService.handleSecureDWebhook(secureDNotificationDto);

    ApiResponse<String> apiResponse =
        ApiResponse.<String>builder().success(true).data("Postback handled successfully").build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @PostMapping("/unsubscribe-notification")
  public ResponseEntity<ApiResponse<String>> handleUnsubscribeEvent(
      @RequestBody UnsubscribeRequest unsubscribeRequest) {
    advertiserService.handleUnsubscription(unsubscribeRequest);
    ApiResponse<String> apiResponse =
        ApiResponse.<String>builder().success(true).data("churn notification received").build();
    return new ResponseEntity<ApiResponse<String>>(apiResponse, HttpStatus.OK);
  }

  @GetMapping("/total-amount-owed")
  public ResponseEntity<ApiResponse<CampaignDetailsDTO>> getTotalAmountOwedByAdvertiser(
      String advertiserId) {
    CampaignDetailsDTO response = advertiserService.getTotalAmountOwedByAdvertiser(advertiserId);

    ApiResponse<CampaignDetailsDTO> apiResponse =
        ApiResponse.<CampaignDetailsDTO>builder().success(true).data(response).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @GetMapping("/churn/report")
  public ResponseEntity<ApiResponse<List<AdvertiserChurnReportDTO>>> generateChurnReport(
          AdvertiserChurnReportRequestDTO reportRequestDTO) {
    String username = getUsernameFromSecurityContext();
    List<AdvertiserChurnReportDTO> reports = advertiserService.generateChurnReport(username,reportRequestDTO);
    ApiResponse<List<AdvertiserChurnReportDTO>> apiResponse =
            ApiResponse.<List<AdvertiserChurnReportDTO>>builder().success(true).data(reports).build();
    return ResponseEntity.ok(apiResponse);
  }

  @GetMapping("/churn/campaigns")
  public ResponseEntity<ApiResponse<List<AutoFillDTO>>> autoFillPublisher() {
    String username = getUsernameFromSecurityContext();
    List<AutoFillDTO> response = advertiserService.getCampaignsForAdvertiser(username);
    ApiResponse<List<AutoFillDTO>> apiResponse =
            ApiResponse.<List<AutoFillDTO>>builder().success(true).data(response).build();
    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @PostMapping({"/refresh-token"})
  public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
    advertiserService.refreshToken(request, response);
  }
}
