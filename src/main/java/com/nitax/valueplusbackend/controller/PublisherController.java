package com.nitax.valueplusbackend.controller;

import com.nitax.valueplusbackend.dto.PublisherCampaignMetricsDto;
import com.nitax.valueplusbackend.dto.PublisherCampaignUrlDto;
import com.nitax.valueplusbackend.dto.mapper.EntityDtoMapper;
import com.nitax.valueplusbackend.dto.request.AdvertiserChurnReportRequestDTO;
import com.nitax.valueplusbackend.dto.request.ChurnReportRequestDTO;
import com.nitax.valueplusbackend.dto.request.ChurnReportRequestDTO;
import com.nitax.valueplusbackend.dto.request.PublisherConversionRequestDTO;
import com.nitax.valueplusbackend.dto.request.UpdatePublisherDTO;
import com.nitax.valueplusbackend.dto.response.*;
import com.nitax.valueplusbackend.service.PublisherService;
import com.nitax.valueplusbackend.service.impl.UserDetailsImpl;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pubs")
@Validated
@CrossOrigin
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class PublisherController {

  private final PublisherService publisherService;

  @Autowired
  public PublisherController(PublisherService publisherService) {
    this.publisherService = publisherService;
  }

  //  @GetMapping("/{publisherId}/{campaignId}/{trxId}")
  //  public ResponseEntity<ApiResponse<String>> handlePostback(
  //      @PathVariable String publisherId,
  //      @PathVariable String campaignId,
  //      @PathVariable String trxId) {
  //    publisherService.handlePublisherCallBack(publisherId, campaignId, trxId);
  //
  //    ApiResponse<String> apiResponse =
  //        ApiResponse.<String>builder().success(true).data("Postback handled
  // successfully").build();
  //
  //    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  //  }
  //    @PostMapping("/create")
  //    public ResponseEntity<ApiResponse<String>> createPublisher(
  //            @RequestBody @Valid CreatePublisherDTO publisherDTO) {
  //        String response = publisherService.savePublisher(publisherDTO);
  //        ApiResponse<String> apiResponse =
  //                ApiResponse.<String>builder().success(true).data(response).build();
  //        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
  //    }

  @PutMapping("/edit/{pubId}")
  public ResponseEntity<ApiResponse<String>> patchPublisher(
      @RequestBody @Valid UpdatePublisherDTO publisherDTO, @PathVariable String pubId) {
    String response = publisherService.updatePublisher(pubId, publisherDTO);
    ApiResponse<String> apiResponse =
        ApiResponse.<String>builder().success(true).data(response).build();
    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @GetMapping("all")
  public ResponseEntity<ApiResponse<List<PublisherResponse>>> getAllPublishers() {
    List<PublisherResponse> publishers =
        publisherService.getAllPublishers().stream().map(EntityDtoMapper::toResponse).toList();
    ApiResponse<List<PublisherResponse>> apiResponse =
        ApiResponse.<List<PublisherResponse>>builder().success(true).data(publishers).build();
    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @GetMapping("/get/{pubId}")
  public ResponseEntity<ApiResponse<PublisherResponse>> getPublisher(@PathVariable String pubId) {
    PublisherResponse publisher = EntityDtoMapper.toResponse(publisherService.findByPubId(pubId));
    ApiResponse<PublisherResponse> apiResponse =
        ApiResponse.<PublisherResponse>builder().success(true).data(publisher).build();
    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @GetMapping("/load-publisher")
  public ResponseEntity<ApiResponse<PublisherResponse>> loadPublisher() {
    String username = getUsernameFromSecurityContext();
    PublisherResponse publisher = EntityDtoMapper.toResponse(publisherService.findByEmail(username));
    ApiResponse<PublisherResponse> apiResponse =
            ApiResponse.<PublisherResponse>builder().success(true).data(publisher).build();
    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @PostMapping("/update-cpa-cost")
  public ResponseEntity<ApiResponse<String>> calculateTotalDueCost() {
    publisherService.calculateTotalDueCost();
    ApiResponse<String> response = new ApiResponse<>(true, "cost updated");

    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @GetMapping("/publishers-urls")
  public ResponseEntity<ApiResponse<List<PublisherCampaignUrlDto>>> getPublisherUrls() {
    List<PublisherCampaignUrlDto> urls = publisherService.getPublisherUrls();
    ApiResponse<List<PublisherCampaignUrlDto>> response = new ApiResponse<>(true, urls);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @GetMapping("/dashboard-Campaign-stats")
  public ResponseEntity<ApiResponse<PublisherCampaignDashboardResponse>> getPublisherCampaignsStats() {
    String username = getUsernameFromSecurityContext();
    PublisherCampaignDashboardResponse response = publisherService.getTotalCampaignStats(username);
    ApiResponse<PublisherCampaignDashboardResponse> apiResponse = new ApiResponse<>(true, response);
    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @GetMapping("/get-all_publisher-campaigns/{pubId}")
  public ResponseEntity<ApiResponse< List<PublisherCampaignDto> >>getAllPublisherCampaignsByPubId(@PathVariable String pubId){
    List<PublisherCampaignDto> publisherCampaigns = publisherService.getPublisherCampaigns(pubId);
    ApiResponse< List<PublisherCampaignDto> > apiResponse = new ApiResponse<>(true, publisherCampaigns);
    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @GetMapping("/pause-publisher-campaign/{campaignId}")
  public ResponseEntity<ApiResponse<String>> pausePublisherCampaign(@PathVariable long campaignId, @RequestParam String reason){
    String response = publisherService.pausePublisherCampaign(campaignId,reason);
    ApiResponse<String> apiResponse = new ApiResponse<>(true, response);
    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @GetMapping("/activate-publisher-campaign/{campaignId}")
  public ResponseEntity<ApiResponse<String>> activatePublisherCampaign(@PathVariable long campaignId, @RequestParam String reason){
    String response = publisherService.activatePublisherCampaign(campaignId, reason);
    ApiResponse<String> apiResponse = new ApiResponse<>(true, response);
    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @DeleteMapping("/delete-publisher-campaign/{campaignId}")
  public ResponseEntity<ApiResponse<String>> deletePublisherCampaign(@PathVariable long campaignId, @RequestParam String reason){
    String response = publisherService.deletePublisherCampaign(campaignId,reason);
    ApiResponse<String> apiResponse = new ApiResponse<>(true, response);
    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }


  @PostMapping("/get-publisher-campaigns-stats")
  public ResponseEntity<ApiResponse<List<PublisherCampaignConversionsDTO>> > getPublisherCampaignsStats(@RequestBody @Valid PublisherConversionRequestDTO dto){
    List<PublisherCampaignConversionsDTO> publisherCampaigns = publisherService.getCampaignPublisherStats(dto);
    ApiResponse<List<PublisherCampaignConversionsDTO>> apiResponse = new ApiResponse<>(true, publisherCampaigns);
    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @PostMapping("/get-publisher-report-metrics")
  public ResponseEntity<ApiResponse<List<PublisherCampaignMetricsDto>> > getPublisherReportMetrics(@RequestBody @Valid PublisherConversionRequestDTO dto){
    List<PublisherCampaignMetricsDto> publisherCampaigns = publisherService.fetchPublisherCampaignMetrics(dto);
    ApiResponse<List<PublisherCampaignMetricsDto>> apiResponse = new ApiResponse<>(true, publisherCampaigns);
    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }


//  @GetMapping("/get-publisher-campaigns-stats")
//  public ResponseEntity<ApiResponse<List<PublisherCampaignConversionsDTO>>> getPublishersCampaignConversions(PublisherConversionRequestDTO dto) {
//
//    List<PublisherCampaignConversionsDTO> response =
//            adminService.getPublishersCampaignConversions(dto);
//
//    ApiResponse<List<PublisherCampaignConversionsDTO>> apiResponse =
//            ApiResponse.<List<PublisherCampaignConversionsDTO>>builder()
//                    .success(true)
//                    .data(response)
//                    .build();
//
//    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
//  }

  @GetMapping("/get-publisher-top3-campaigns/{pub_id}")
    public ResponseEntity<ApiResponse<?>> getTopThreePublisherCampaigns(@PathVariable String pub_id) {
        ApiResponse<?> topThreePublisherCampaigns = publisherService.getTopThreePublisherCampaigns(pub_id);
        return new ResponseEntity<>(topThreePublisherCampaigns, HttpStatus.OK);
    }

    @GetMapping("/hot3-campaigns")
    public ResponseEntity<ApiResponse<?>> getTopThreeNewCampaigns() {
        ApiResponse<?> topThreePublisherCampaigns = publisherService.getTopThreeNewCampaign();
        return new ResponseEntity<>(topThreePublisherCampaigns, HttpStatus.OK);
    }

    @GetMapping("/new-available-campaigns/{publisherId}")
    public ResponseEntity<ApiResponse<?>> getNewAvailableCampaigns(@PathVariable String publisherId,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "10") int size) {
        ApiResponse<?> newAvailableCampaigns = publisherService.getNewAvaialableCampaign(page, size, publisherId);
        return new ResponseEntity<>(newAvailableCampaigns, HttpStatus.OK);
    }

    @PostMapping("/start-campaign/{campaignId}")
    public ResponseEntity<ApiResponse<?>> startCampaign(@PathVariable String campaignId) {
        ApiResponse<?> startCampaign = publisherService.startCampaign(campaignId,getUsernameFromSecurityContext());
        return new ResponseEntity<>(startCampaign, HttpStatus.OK);
    }

  @GetMapping("/churn/report")
  public ResponseEntity<ApiResponse<List<? extends ChurnReport>>> generateChurnReport(
          ChurnReportRequestDTO reportRequestDTO) {
    List<? extends ChurnReport> reports = publisherService.generateReport(reportRequestDTO);
    ApiResponse<List<? extends ChurnReport>> apiResponse =
            ApiResponse.<List<? extends ChurnReport>>builder().success(true).data(reports).build();
    return ResponseEntity.ok(apiResponse);
  }

  @PostMapping("/generate-api-key/{pubId}")
  public ResponseEntity<ApiResponse<String>> generateApiKey(@PathVariable String pubId) {
    String apiKey = publisherService.generateApiKey(pubId);
    ApiResponse<String> apiResponse =
            ApiResponse.<String>builder().success(true).data(apiKey).build();
    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @GetMapping("/churn/auto-suggestions/campaigns")
  public ResponseEntity<ApiResponse<List<AutoFillDTO>>> autoFillCampaigns(
          @RequestParam String keys) {
    List<AutoFillDTO> response = publisherService.autoFillCampaigns(keys);
    ApiResponse<List<AutoFillDTO>> apiResponse =
            ApiResponse.<List<AutoFillDTO>>builder().success(true).data(response).build();
    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }
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
}
