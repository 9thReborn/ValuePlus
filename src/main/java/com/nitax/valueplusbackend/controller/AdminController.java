package com.nitax.valueplusbackend.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import com.nitax.valueplusbackend.domain.*;
import com.nitax.valueplusbackend.dto.request.*;
import com.nitax.valueplusbackend.service.*;
import org.springframework.cache.CacheManager;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nitax.valueplusbackend.dto.CampignUrlDto;
import com.nitax.valueplusbackend.dto.mapper.EntityDtoMapper;
import com.nitax.valueplusbackend.dto.response.AdvertiserConversionCpaBreakdownDTO;
import com.nitax.valueplusbackend.dto.response.AdvertiserConversionDTO;
import com.nitax.valueplusbackend.dto.response.AdvertiserConversionDTOForTop;
import com.nitax.valueplusbackend.dto.response.AdvertiserNameResponse;
import com.nitax.valueplusbackend.dto.response.ApiResponse;
import com.nitax.valueplusbackend.dto.response.AutoFillDTO;
import com.nitax.valueplusbackend.dto.response.ChurnReport;
import com.nitax.valueplusbackend.dto.response.CommonSubscriberStats;
import com.nitax.valueplusbackend.dto.response.LoginResponse;
import com.nitax.valueplusbackend.dto.response.MonthlyConversionCount;
import com.nitax.valueplusbackend.dto.response.PublisherCampaignConversionsDTO;
import com.nitax.valueplusbackend.dto.response.PublisherConversionsDTO;
import com.nitax.valueplusbackend.dto.response.PublisherResponse;
import com.nitax.valueplusbackend.dto.response.SearchPostbackDto;
import com.nitax.valueplusbackend.dto.response.SubscriberDetailDTO;
import com.nitax.valueplusbackend.dto.AdminChurnReportDto;

import com.nitax.valueplusbackend.exception.AppException;
import com.nitax.valueplusbackend.service.impl.UserDetailsImpl;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@CrossOrigin
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class AdminController {
  private final AdminService adminService;
  private final BankService bankService;
  private final ExcelExportService excelExportService;
  private final CacheManager cacheManager;
  private final SubscriberService subscriberService;
  private final ConversionDecisionService conversionDecisionService;
  private final com.nitax.valueplusbackend.service.ReportService reportService;
  private final NotificationService notificationService;
  private final BlocklistService blocklistService;

  @PostMapping("login")
  public ResponseEntity<ApiResponse<LoginResponse>> adminLogin(@RequestBody AdminLoginRequest dto) {
    LoginResponse response = adminService.adminLogin(dto);

    ApiResponse<LoginResponse> apiResponse =
        ApiResponse.<LoginResponse>builder().success(true).data(response).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @PostMapping("campaigns")
  public ResponseEntity<ApiResponse<Campaign>> createCampaign(
      @RequestBody CreateCampaignForAdminDTO dto) {
    Campaign response = adminService.createCampaign(dto);

    ApiResponse<Campaign> apiResponse =
        ApiResponse.<Campaign>builder().success(true).data(response).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
  }

  @PostMapping("campaigns/pause/{campaignId}")
  public ResponseEntity<ApiResponse<Campaign>> pauseCampaign(
      @RequestBody CampaignPauseRequest dto, @PathVariable String campaignId) {
    Campaign response = adminService.pauseCampaign(dto, campaignId);

    ApiResponse<Campaign> apiResponse =
        ApiResponse.<Campaign>builder().success(true).data(response).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @PostMapping("campaigns/enable/{campaignId}")
  public ResponseEntity<ApiResponse<Campaign>> enableCampaign(@PathVariable String campaignId) {
    Campaign response = adminService.enableCampaign(campaignId);

    ApiResponse<Campaign> apiResponse =
        ApiResponse.<Campaign>builder().success(true).data(response).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @PostMapping("campaigns/disable/{campaignId}")
  public ResponseEntity<ApiResponse<Campaign>> disableCampaign(
      @PathVariable String campaignId, @RequestBody CampaignDsableRequest dto) {
    Campaign response = adminService.disableCampaign(campaignId, dto);

    ApiResponse<Campaign> apiResponse =
        ApiResponse.<Campaign>builder().success(true).data(response).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @PostMapping("campaigns/delete/{campaignId}")
  public ResponseEntity<ApiResponse<String>> deleteCampaign(
      @RequestBody DeleteCampaignRequestDto dto, @PathVariable String campaignId) {
    String response = adminService.deleteCampaign(dto, campaignId);

    ApiResponse<String> apiResponse =
        ApiResponse.<String>builder().success(true).data(response).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @GetMapping("campaigns/active")
  public ResponseEntity<ApiResponse<Long>> getActiveCampaigns() {
    Long response = adminService.getActiveCampaigns();

    ApiResponse<Long> apiResponse =
        ApiResponse.<Long>builder().success(true).data(response).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @GetMapping("campaigns/paused")
  public ResponseEntity<ApiResponse<Long>> getPausedCampaigns() {
    Long response = adminService.getPausedCampaigns();

    ApiResponse<Long> apiResponse =
        ApiResponse.<Long>builder().success(true).data(response).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @GetMapping("campaigns/disabled")
  public ResponseEntity<ApiResponse<Long>> getDisabledCampaigns() {
    Long response = adminService.getDisabledCampaigns();

    ApiResponse<Long> apiResponse =
        ApiResponse.<Long>builder().success(true).data(response).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @GetMapping("campaigns/top-five")
  public ResponseEntity<ApiResponse<List<AdvertiserConversionDTOForTop>>> getTopFiveCampaigns() {
    List<AdvertiserConversionDTOForTop> response = adminService.getTopFiveCampaigns();

    ApiResponse<List<AdvertiserConversionDTOForTop>> apiResponse =
        ApiResponse.<List<AdvertiserConversionDTOForTop>>builder()
            .success(true)
            .data(response)
            .build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @GetMapping("campaigns/least-five")
  public ResponseEntity<ApiResponse<List<AdvertiserConversionDTOForTop>>> getLeastFiveCampaigns() {
    List<AdvertiserConversionDTOForTop> response = adminService.getLeastFiveCampaigns();

    ApiResponse<List<AdvertiserConversionDTOForTop>> apiResponse =
        ApiResponse.<List<AdvertiserConversionDTOForTop>>builder()
            .success(true)
            .data(response)
            .build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @GetMapping("campaigns/all")
  public ResponseEntity<ApiResponse<Page<Campaign>>> getAllCampaigns(
      CampaignFilter filter, Pageable pageable) {
    Page<Campaign> response = adminService.getAllCampaigns(filter, pageable);

    ApiResponse<Page<Campaign>> apiResponse =
        ApiResponse.<Page<Campaign>>builder().success(true).data(response).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @GetMapping("campaigns/urls/{campaignId}")
  public ResponseEntity<ApiResponse<List<CampignUrlDto>>> getCampaignUrls(
      @PathVariable String campaignId) {
    List<CampignUrlDto> response = adminService.getCampaignUrls(campaignId);

    ApiResponse<List<CampignUrlDto>> apiResponse =
        ApiResponse.<List<CampignUrlDto>>builder().success(true).data(response).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @GetMapping("campaigns/{campaignId}")
  public ResponseEntity<ApiResponse<Campaign>> getCampaignById(@PathVariable String campaignId) {
    Campaign response = adminService.getCampaignById(campaignId);

    ApiResponse<Campaign> apiResponse =
        ApiResponse.<Campaign>builder().success(true).data(response).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

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
    String uploadedImage = adminService.uploadCampaignImage(file);
    ApiResponse<String> response = new ApiResponse<>(true, uploadedImage);

    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @PutMapping("campaigns/{campaignId}")
  public ResponseEntity<ApiResponse<Campaign>> updateCampaign(
      @PathVariable String campaignId, @RequestBody UpdateCampaignForAdminDTO dto) {
    Campaign response = adminService.updateCampaign(campaignId, dto);

    ApiResponse<Campaign> apiResponse =
        ApiResponse.<Campaign>builder().success(true).data(response).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @GetMapping("performance-overview/conversions")
  public ResponseEntity<ApiResponse<List<MonthlyConversionCount>>>
      getCampaignPerformanceOverview() {
    List<MonthlyConversionCount> response = adminService.getCampaignPerformanceOverview();

    ApiResponse<List<MonthlyConversionCount>> apiResponse =
        ApiResponse.<List<MonthlyConversionCount>>builder().success(true).data(response).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @GetMapping("conversions/advertisers")
  public ResponseEntity<ApiResponse<List<AdvertiserConversionDTO>>> getAdvertiserConversions(
      AdvertiserConversionRequestDTO dto) {

    List<AdvertiserConversionDTO> response = adminService.getAdvertiserConversions(dto);

    ApiResponse<List<AdvertiserConversionDTO>> apiResponse =
        ApiResponse.<List<AdvertiserConversionDTO>>builder().success(true).data(response).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @GetMapping("conversions/advertisers/export")
  public ResponseEntity<byte[]> exportAdvertiserConversions(AdvertiserConversionRequestDTO dto) {
    List<AdvertiserConversionDTO> response = adminService.getAdvertiserConversions(dto);

    ByteArrayOutputStream excelFile =
        excelExportService.exportAdvertiserConversionsToExcel(response);
    String filename =
        "advertiser_conversions_report_"
            + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            + ".xlsx";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(
        MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    headers.setContentDispositionFormData("attachment", filename);
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

    return new ResponseEntity<>(excelFile.toByteArray(), headers, HttpStatus.OK);
  }

  @GetMapping("conversions/advertisers/export-cpa-breakdown")
  public ResponseEntity<byte[]> exportAdvertiserConversionsCpaBreakdown(
      AdvertiserConversionRequestDTO dto) {
    List<AdvertiserConversionCpaBreakdownDTO> response =
        adminService.getAdvertiserConversionsCpaBreakdown(dto);

    ByteArrayOutputStream excelFile =
        excelExportService.exportAdvertiserConversionsCpaBreakdownToExcel(response);
    String filename =
        "advertiser_conversions_cpa_breakdown_"
            + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            + ".xlsx";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(
        MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    headers.setContentDispositionFormData("attachment", filename);
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

    return new ResponseEntity<>(excelFile.toByteArray(), headers, HttpStatus.OK);
  }

  @GetMapping("conversions/publishers-campaigns")
  public ResponseEntity<ApiResponse<List<PublisherCampaignConversionsDTO>>>
      getPublishersCampaignConversions(PublisherConversionRequestDTO dto) {

    List<PublisherCampaignConversionsDTO> response =
        adminService.getPublishersCampaignConversions(dto);

    ApiResponse<List<PublisherCampaignConversionsDTO>> apiResponse =
        ApiResponse.<List<PublisherCampaignConversionsDTO>>builder()
            .success(true)
            .data(response)
            .build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @GetMapping("conversions/publishers-campaigns/export")
  public ResponseEntity<byte[]> exportPublishersCampaignConversions(
      PublisherConversionRequestDTO dto) {
    List<PublisherCampaignConversionsDTO> response =
        adminService.getPublishersCampaignConversions(dto);

    ByteArrayOutputStream excelFile =
        excelExportService.exportPublisherCampaignConversionsToExcel(response);
    String filename =
        "publisher_campaign_conversions_report_"
            + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            + ".xlsx";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(
        MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    headers.setContentDispositionFormData("attachment", filename);
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

    return new ResponseEntity<>(excelFile.toByteArray(), headers, HttpStatus.OK);
  }

  @GetMapping("conversions/publishers")
  public ResponseEntity<ApiResponse<List<PublisherConversionsDTO>>> getPublishersConversions(
      PublisherConversionRequestDTO dto) {
    List<PublisherConversionsDTO> response = adminService.getPublishersConversions(dto);

    ApiResponse<List<PublisherConversionsDTO>> apiResponse =
        ApiResponse.<List<PublisherConversionsDTO>>builder().success(true).data(response).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @GetMapping("conversions/publishers/export")
  public ResponseEntity<byte[]> exportPublishersConversions(PublisherConversionRequestDTO dto) {
    List<PublisherConversionsDTO> response = adminService.getPublishersConversions(dto);

    ByteArrayOutputStream excelFile =
        excelExportService.exportPublisherConversionsToExcel(response);
    String filename =
        "publisher_conversions_report_"
            + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            + ".xlsx";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(
        MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    headers.setContentDispositionFormData("attachment", filename);
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

    return new ResponseEntity<>(excelFile.toByteArray(), headers, HttpStatus.OK);
  }

  @GetMapping("performance-overview/clicks")
  public ResponseEntity<ApiResponse<List<MonthlyConversionCount>>>
      getCampaignPerformanceOverviewClicks() {
    List<MonthlyConversionCount> response = adminService.getCampaignPerformanceOverviewClicks();

    ApiResponse<List<MonthlyConversionCount>> apiResponse =
        ApiResponse.<List<MonthlyConversionCount>>builder().success(true).data(response).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @GetMapping("campaigns/common-subscribers")
  public ResponseEntity<ApiResponse<CommonSubscriberStats>> getCommonSubscribersForCampaigns() {
    CommonSubscriberStats response = adminService.getCommonSubscribersForCampaigns();

    ApiResponse<CommonSubscriberStats> apiResponse =
        ApiResponse.<CommonSubscriberStats>builder().success(true).data(response).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @GetMapping("publishers/common-subscribers")
  public ResponseEntity<ApiResponse<CommonSubscriberStats>> getCommonSubscribersForPublishers() {
    CommonSubscriberStats response = adminService.getCommonSubscribersForPublishers();

    ApiResponse<CommonSubscriberStats> apiResponse =
        ApiResponse.<CommonSubscriberStats>builder().success(true).data(response).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @PostMapping("sms/send")
  public ResponseEntity<ApiResponse<SMSLog>> sendSMSByList(@RequestBody SendSMSByList dto) {
    SMSLog response = adminService.sendSMSByList(dto);

    ApiResponse<SMSLog> apiResponse =
        ApiResponse.<SMSLog>builder().success(true).data(response).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
  }

  @GetMapping("advertisers/names")
  public ResponseEntity<ApiResponse<List<AdvertiserNameResponse>>> getAdvertiserNames() {
    List<AdvertiserNameResponse> response = adminService.getAdvertiserNames();

    ApiResponse<List<AdvertiserNameResponse>> apiResponse =
        ApiResponse.<List<AdvertiserNameResponse>>builder().success(true).data(response).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @PostMapping("/advertisers/create")
  public ResponseEntity<ApiResponse<String>> createAdvertiser(
      @Valid @RequestBody AdvertiserSignupDTO signupDTO) {
    String authToken = adminService.createAdvertiser(signupDTO);
    ApiResponse<String> response =
        ApiResponse.<String>builder().success(true).data(authToken).build();

    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @GetMapping("/advertisers/info/{id}")
  public ResponseEntity<ApiResponse<Advertiser>> getAdvertiserInfo(@PathVariable String id) {
    Advertiser response = adminService.getAdvertiserInfo(id);
    ApiResponse<Advertiser> apiResponse =
        ApiResponse.<Advertiser>builder().success(true).data(response).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @PutMapping("/advertisers/approve/{id}")
  public ResponseEntity<ApiResponse<String>> approveAdvertiser(@PathVariable String id) {
    String response = adminService.approveAdvertiser(id);
    ApiResponse<String> apiResponse =
        ApiResponse.<String>builder()
            .success(true)
            .data("ACTION PERFROMED, PLEASE REFERESH")
            .build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @PutMapping("/advertisers/reject/{id}")
  public ResponseEntity<ApiResponse<String>> rejectAdvertiser(@PathVariable String id) {
    String response = adminService.rejectAdvertiser(id);
    ApiResponse<String> apiResponse =
        ApiResponse.<String>builder()
            .success(true)
            .data("ACTION PERFROMED, PLEASE REFERESH")
            .build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @PutMapping("/advertisers/suspend/{id}")
  public ResponseEntity<ApiResponse<String>> suspendAdvertiser(
      @RequestBody SuspendAdvertiserRequestDto reason, @PathVariable String id) {
    String response = adminService.suspendAdvertiser(id, reason.getSuspensionReason());
    ApiResponse<String> apiResponse =
        ApiResponse.<String>builder().success(true).data(response).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @DeleteMapping("/advertisers/delete/{id}")
  public ResponseEntity<ApiResponse<String>> deleteAdvertiser(@PathVariable String id) {
    String response = adminService.deleteAdvertiser(id);
    ApiResponse<String> apiResponse =
        ApiResponse.<String>builder().success(true).data(response).build();
    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @GetMapping("advertisers")
  public ResponseEntity<ApiResponse<Page<Advertiser>>> getAllAdvertisers(
      String businessName,
      @RequestParam(name = "status", required = false) AdvertiserStatus status,
      @PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
    Page<Advertiser> response = adminService.getAllAdvertisers(businessName, status, pageable);
    ApiResponse<Page<Advertiser>> apiResponse =
        ApiResponse.<Page<Advertiser>>builder().success(true).data(response).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @SecurityRequirement(name = "bearerAuth")
  @GetMapping("/postbacks")
  public ResponseEntity<ApiResponse<List<SearchPostbackDto>>> getPostbacks(
      String transactionId,
      @PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
    List<SearchPostbackDto> response = adminService.getPostbacks(transactionId, pageable);
    ApiResponse<List<SearchPostbackDto>> apiResponse =
        ApiResponse.<List<SearchPostbackDto>>builder().success(true).data(response).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  // publisher

  @PostMapping("/pubs/create")
  public ResponseEntity<ApiResponse<String>> createPublisher(
      @RequestBody @Valid CreatePublisherDTO publisherDTO) {
    String response = adminService.savePublisher(publisherDTO);
    ApiResponse<String> apiResponse =
        ApiResponse.<String>builder().success(true).data(response).build();
    return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
  }

  @PutMapping("/pubs/edit/{pubId}")
  public ResponseEntity<ApiResponse<String>> patchPublisher(
      @RequestBody @Valid UpdatePublisherDTO publisherDTO, @PathVariable String pubId) {
    String response = adminService.updatePublisher(pubId, publisherDTO);
    ApiResponse<String> apiResponse =
        ApiResponse.<String>builder().success(true).data(response).build();
    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @GetMapping("/pubs/all")
  public ResponseEntity<ApiResponse<List<PublisherResponse>>> getAllPublishers() {
    List<PublisherResponse> publishers =
        adminService.getAllPublishers().stream().map(EntityDtoMapper::toResponse).toList();
    ApiResponse<List<PublisherResponse>> apiResponse =
        ApiResponse.<List<PublisherResponse>>builder().success(true).data(publishers).build();
    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @GetMapping("/pubs/get/{pubId}")
  public ResponseEntity<ApiResponse<PublisherResponse>> getPublisher(@PathVariable String pubId) {
    PublisherResponse publisher = EntityDtoMapper.toResponse(adminService.findByPubId(pubId));
    ApiResponse<PublisherResponse> apiResponse =
        ApiResponse.<PublisherResponse>builder().success(true).data(publisher).build();
    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @PutMapping("/pubs/approve/{id}")
  public ResponseEntity<ApiResponse<String>> approvePublisher(@PathVariable String id) {
    String response = adminService.approvePublisher(id);
    ApiResponse<String> apiResponse =
        ApiResponse.<String>builder().success(true).data(response).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @PutMapping("/pubs/suspend/{id}")
  public ResponseEntity<ApiResponse<String>> suspendPublisher(@PathVariable String id) {
    String response = adminService.suspendPublisher(id);
    ApiResponse<String> apiResponse =
        ApiResponse.<String>builder().success(true).data(response).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @DeleteMapping("/pubs/delete/{id}")
  public ResponseEntity<ApiResponse<String>> deletePublisher(@PathVariable String id) {
    String response = adminService.deletePublisher(id);
    ApiResponse<String> apiResponse =
        ApiResponse.<String>builder().success(true).data(response).build();
    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  // publisher Campaign: Create, Read, Update, Delete
  @PostMapping("/publisher-campaigns")
  public ResponseEntity<PublisherCampaign> addPublisherCampaign(
      @Valid @RequestBody PublisherCampaignRequest request) {
    PublisherCampaign publisherCampaign = adminService.createPublisherCampaign(request);
    return ResponseEntity.ok(publisherCampaign);
  }

  @PutMapping("/publisher-campaigns/{pubCampId}")
  public ResponseEntity<PublisherCampaign> updatePublisherCampaign(
      @PathVariable String pubCampId, @Valid @RequestBody PublisherCampaignRequest request) {
    PublisherCampaign updatedCampaign = adminService.updatePublisherCampaign(pubCampId, request);
    return ResponseEntity.ok(updatedCampaign);
  }

  @GetMapping("/publisher-campaigns")
  public ResponseEntity<List<PublisherCampaign>> getPublisherCampaigns(
      @RequestParam(required = false) String publisherId,
      @RequestParam(required = false) String campaignId) {
    List<PublisherCampaign> campaigns = adminService.getPublisherCampaigns(publisherId, campaignId);
    return ResponseEntity.ok(campaigns);
  }

  @DeleteMapping("/publisher-campaigns/{pubCampId}")
  public ResponseEntity<Void> deletePublisherCampaign(@PathVariable String pubCampId) {
    adminService.deletePublisherCampaign(pubCampId);
    return ResponseEntity.noContent().build();
  }

  // General CPA Settings..
  @PostMapping("/cpa") // done
  public ResponseEntity<CPASettings> addCPASettings(@Valid @RequestBody CPASettingRequest request) {
    CPASettings cpaSettings = adminService.createCPASetting(request);
    return ResponseEntity.ok(cpaSettings);
  }

  @PutMapping("/cpa/{cpaId}") // fine
  public ResponseEntity<CPASettings> updateCPASettings(
      @PathVariable String cpaId, @RequestBody CPASettingRequest request) {
    CPASettings updatedCampaign = adminService.updateCPASetting(cpaId, request);
    return ResponseEntity.ok(updatedCampaign);
  }

  @GetMapping("/cpa") // done
  public ResponseEntity<List<CPASettings>> getCPASettings(
      @RequestParam(required = false) String cpaId,
      @RequestParam(required = false) String country,
      @RequestParam(required = false) String mno,
      @RequestParam(required = false) String flow,
      @RequestParam(required = false) String flowType) {
    List<CPASettings> cpas = adminService.getCPAList(cpaId);
    if (country != null || mno != null || flow != null || flowType != null) {
      cpas = adminService.getCPAList(country, mno, flow, flowType);
    }
    return ResponseEntity.ok(cpas);
  }

  @DeleteMapping("/cpa/{cpaId}")
  public ResponseEntity<Void> deleteCPASettings(@PathVariable String cpaId) {
    adminService.deleteCPA(cpaId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/reports/churn/download")
  public ResponseEntity<byte[]> downloadAdminChurnReport(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
      @RequestParam(required = false) Integer churnDurationHours,
      @RequestParam(required = false, defaultValue = "false") boolean sendEmail) throws IOException {
    Instant startDate = dateFrom.atStartOfDay().toInstant(ZoneOffset.UTC);
    Instant endDate = dateTo.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
    int duration = churnDurationHours != null ? churnDurationHours : 48;
    List<AdminChurnReportDto> records = notificationService.fetchAdminChurnReport(startDate, endDate, duration);
    if (sendEmail) {
      reportService.sendChurnReportToPublishers(records, dateFrom + " to " + dateTo, duration);
    }
    ByteArrayOutputStream out = excelExportService.exportAdminChurnReportToExcel(records);
    String filename = "churn-report-" + dateFrom + "-to-" + dateTo + "-" + duration + "h.xlsx";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    headers.setContentDispositionFormData("attachment", filename);
    return ResponseEntity.ok().headers(headers).body(out.toByteArray());
  }

  // churnReport Endpoint
  @GetMapping("/churn/report")
  public ResponseEntity<ApiResponse<List<? extends ChurnReport>>> generateChurnReport(
      ChurnReportRequestDTO reportRequestDTO) {
    List<? extends ChurnReport> reports = adminService.generateReport(reportRequestDTO);
    ApiResponse<List<? extends ChurnReport>> apiResponse =
        ApiResponse.<List<? extends ChurnReport>>builder().success(true).data(reports).build();
    return ResponseEntity.ok(apiResponse);
  }

  @GetMapping("/churn/auto-suggestions/publishers")
  public ResponseEntity<ApiResponse<List<AutoFillDTO>>> autoFillPublisher(
      @RequestParam String keys) {
    List<AutoFillDTO> response = adminService.autoFillPublisher(keys);
    ApiResponse<List<AutoFillDTO>> apiResponse =
        ApiResponse.<List<AutoFillDTO>>builder().success(true).data(response).build();
    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @GetMapping("/churn/auto-suggestions/campaigns")
  public ResponseEntity<ApiResponse<List<AutoFillDTO>>> autoFillCampaigns(
      @RequestParam String keys) {
    List<AutoFillDTO> response = adminService.autoFillCampaigns(keys);
    ApiResponse<List<AutoFillDTO>> apiResponse =
        ApiResponse.<List<AutoFillDTO>>builder().success(true).data(response).build();
    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @PostMapping({"/refresh-token"})
  public void refreshToken(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    adminService.refreshToken(request, response);
  }

  @PostMapping("/add-bank")
  public ResponseEntity<?> addBankDetails(@RequestBody BankDetailsRequest request) {
    Bank bank = bankService.saveBankDetails(request, getCurrentAdmin());
    ApiResponse<Bank> apiResponse = ApiResponse.<Bank>builder().success(true).data(bank).build();
    return ResponseEntity.ok(apiResponse);
  }

  @DeleteMapping("/cache/clear")
  public ResponseEntity<ApiResponse<String>> clearAllCaches() {
    cacheManager.getCacheNames().forEach(cacheName -> {
      var cache = cacheManager.getCache(cacheName);
      if (cache != null) {
        cache.clear();
      }
    });
    log.info("All caches cleared by admin: {}", getCurrentAdmin().getEmail());
    ApiResponse<String> apiResponse = ApiResponse.<String>builder()
        .success(true)
        .data("All caches cleared successfully")
        .build();
    return ResponseEntity.ok(apiResponse);
  }

  @DeleteMapping("/cache/clear/{cacheName}")
  public ResponseEntity<ApiResponse<String>> clearCache(@PathVariable String cacheName) {
    var cache = cacheManager.getCache(cacheName);
    if (cache != null) {
      cache.clear();
      log.info("Cache '{}' cleared by admin: {}", cacheName, getCurrentAdmin().getEmail());
      ApiResponse<String> apiResponse = ApiResponse.<String>builder()
          .success(true)
          .data("Cache '" + cacheName + "' cleared successfully")
          .build();
      return ResponseEntity.ok(apiResponse);
    }
    throw new AppException("Cache '" + cacheName + "' not found");
  }

  @GetMapping("/cache/names")
  public ResponseEntity<ApiResponse<List<String>>> getCacheNames() {
    List<String> cacheNames = cacheManager.getCacheNames().stream().toList();
    ApiResponse<List<String>> apiResponse = ApiResponse.<List<String>>builder()
        .success(true)
        .data(cacheNames)
        .build();
    return ResponseEntity.ok(apiResponse);
  }

  @GetMapping("/subscribers/events")
  public ResponseEntity<ApiResponse<Page<SubscriberEvent>>> searchSubscriberEvents(
      SubscriberEventFilter filter,
      @PageableDefault(sort = "eventTimestamp", direction = Sort.Direction.DESC) Pageable pageable) {
    Page<SubscriberEvent> events = subscriberService.searchEvents(filter, pageable);
    ApiResponse<Page<SubscriberEvent>> apiResponse = ApiResponse.<Page<SubscriberEvent>>builder()
        .success(true)
        .data(events)
        .build();
    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @GetMapping("/subscribers/{subscriberId}")
  public ResponseEntity<ApiResponse<SubscriberDetailDTO>> getSubscriberDetail(
      @PathVariable Long subscriberId) {
    SubscriberDetailDTO detail = subscriberService.getSubscriberDetail(subscriberId);
    ApiResponse<SubscriberDetailDTO> apiResponse = ApiResponse.<SubscriberDetailDTO>builder()
        .success(true)
        .data(detail)
        .build();
    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

    @PostMapping("/subscribers/events/{eventId}/replay")
    public ResponseEntity<ApiResponse<ConversionDecision>> replaySubscriberEvent(
            @PathVariable Long eventId) {
        ConversionDecision replayDecision = subscriberService.replayEvent(eventId);
        ApiResponse<ConversionDecision> apiResponse = ApiResponse.<ConversionDecision>builder()
                .success(true)
                .data(replayDecision)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/subscribers/events/{eventId}/decisions")
    public ResponseEntity<ApiResponse<List<ConversionDecision>>> getDecisionsForEvent(
            @PathVariable Long eventId) {
        List<ConversionDecision> decisions = conversionDecisionService.findByEventId(eventId);
        ApiResponse<List<ConversionDecision>> apiResponse = ApiResponse.<List<ConversionDecision>>builder()
                .success(true)
                .data(decisions)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/subscribers/replay")
    public ResponseEntity<ApiResponse<ConversionDecision>> replaySubscriberEventByMsisdn(
            @RequestParam String msisdn) {
        ConversionDecision replayDecision = subscriberService.replayEventByMsisdn(msisdn);
        ApiResponse<ConversionDecision> apiResponse = ApiResponse.<ConversionDecision>builder()
                .success(true)
                .data(replayDecision)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/subscribers/decisions")
    public ResponseEntity<ApiResponse<Page<ConversionDecision>>> getDecisionsForMsisdn(
            @RequestParam String msisdn,
            @PageableDefault(sort = "decisionTime", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ConversionDecision> decisions = conversionDecisionService.findByMsisdn(msisdn, pageable);
        ApiResponse<Page<ConversionDecision>> apiResponse = ApiResponse.<Page<ConversionDecision>>builder()
                .success(true)
                .data(decisions)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/blocklist")
    public ResponseEntity<ApiResponse<Blocklist>> createBlock(
            @Valid @RequestBody CreateBlocklistRequest request) {
        Blocklist block =
                blocklistService.createManualBlock(
                        request.getMsisdn(),
                        request.getScope(),
                        request.getServiceId(),
                        request.getDurationHours(),
                        getCurrentAdmin().getEmail());
        ApiResponse<Blocklist> apiResponse =
                ApiResponse.<Blocklist>builder().success(true).data(block).build();
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PutMapping("/blocklist/{blockId}/release")
    public ResponseEntity<ApiResponse<Blocklist>> releaseBlock(@PathVariable Long blockId) {
        Blocklist block = blocklistService.release(blockId, getCurrentAdmin().getEmail());
        ApiResponse<Blocklist> apiResponse =
                ApiResponse.<Blocklist>builder().success(true).data(block).build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/blocklist")
    public ResponseEntity<ApiResponse<Page<Blocklist>>> listBlocks(
            @RequestParam(required = false) String msisdn,
            @PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Blocklist> blocks =
                (msisdn == null || msisdn.isEmpty())
                        ? blocklistService.findAll(pageable)
                        : blocklistService.findByMsisdn(msisdn, pageable);
        ApiResponse<Page<Blocklist>> apiResponse =
                ApiResponse.<Page<Blocklist>>builder().success(true).data(blocks).build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

  @PostMapping("/reports/churn/weekly")
  public ResponseEntity<ApiResponse<String>> triggerWeeklyChurnReport() {
    try {
      reportService.generateAndSendAdminChurnReport();
      return ResponseEntity.ok(ApiResponse.<String>builder()
          .success(true)
          .data("Weekly churn report generated and sent.")
          .build());
    } catch (IOException e) {
      log.error("Failed to generate weekly churn report", e);
      return ResponseEntity.internalServerError().body(ApiResponse.<String>builder()
          .success(false)
          .data("Failed to generate report: " + e.getMessage())
          .build());
    }
  }

  private Admin getCurrentAdmin() {
    String adminEmail = "";
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (principal instanceof UserDetailsImpl userDetails) {
      adminEmail = userDetails.getEmail();
    }
    return adminService.getAdminByEmail(adminEmail);
  }
}
