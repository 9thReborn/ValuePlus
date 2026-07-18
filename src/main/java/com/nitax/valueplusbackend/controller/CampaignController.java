package com.nitax.valueplusbackend.controller;

import com.nitax.valueplusbackend.domain.Campaign;
import com.nitax.valueplusbackend.dto.request.CreateCampaignDTO;
import com.nitax.valueplusbackend.dto.request.RetentionStatDto;
import com.nitax.valueplusbackend.dto.response.ApiResponse;
import com.nitax.valueplusbackend.dto.response.PublisherCampaignDetailsDTO;
import com.nitax.valueplusbackend.dto.response.RetentionStatsResponseDto;
import com.nitax.valueplusbackend.service.CampaignService;
import com.nitax.valueplusbackend.service.PublisherCampaignService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/campaigns")
@CrossOrigin
public class CampaignController {

  private final CampaignService campaignService;
  private final PublisherCampaignService publisherCampaignService;

  @Autowired
  public CampaignController(CampaignService campaignService,PublisherCampaignService publisherCampaignService) {
    this.campaignService = campaignService;
    this.publisherCampaignService =  publisherCampaignService;
  }

  @PostMapping("/create")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<Campaign>> createCampaign(
      @Validated @RequestBody CreateCampaignDTO createCampaignDTO) throws IOException {
    Campaign createdCampaign = campaignService.createCampaign(createCampaignDTO);

    ApiResponse<Campaign> response = new ApiResponse<>(true, createdCampaign);

    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @PostMapping("/update-campaign-cost")
  public ResponseEntity<ApiResponse<String>> updateCampaignCost() {
    campaignService.calculateCampaignCostForCurrentMonth();
    ApiResponse<String> response = new ApiResponse<>(true, "cost updated");

    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @GetMapping("/retention")
  public ResponseEntity<ApiResponse<RetentionStatsResponseDto>> getRetentionStatsForDays(
      RetentionStatDto retentionStatDto) {
    RetentionStatsResponseDto response = campaignService.getRetentionStatsForDays(retentionStatDto);

    ApiResponse<RetentionStatsResponseDto> apiResponse =
        ApiResponse.<RetentionStatsResponseDto>builder().success(true).data(response).build();

    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @GetMapping("/{campaignName}")
  public ResponseEntity<ApiResponse<?>> getCampaignByCampaignName(@PathVariable String campaignName){
    PublisherCampaignDetailsDTO response  = publisherCampaignService.fetchPublisherCampaignDetailsByCampaignName(campaignName);
    return ResponseEntity.ok().body(new ApiResponse<>(true,response));
  }
}
