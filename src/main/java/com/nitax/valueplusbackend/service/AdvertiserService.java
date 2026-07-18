package com.nitax.valueplusbackend.service;

import com.nitax.valueplusbackend.domain.Advertiser;
import com.nitax.valueplusbackend.domain.AdvertiserStatus;
import com.nitax.valueplusbackend.domain.Campaign;
import com.nitax.valueplusbackend.dto.CampaignDetailsDTO;
import com.nitax.valueplusbackend.dto.ReportingChartDto;
import com.nitax.valueplusbackend.dto.SecureDNotificationDto;
import com.nitax.valueplusbackend.dto.request.*;
import com.nitax.valueplusbackend.dto.response.*;
import java.io.IOException;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface AdvertiserService {
  List<Advertiser> getAllUnverifiedUsers();

  List<Advertiser> getAllRejectUsers();

  String createAdvertiser(AdvertiserSignupDTO advertiserSignupDTO);

  ApiResponse<LoginResponse> loginAdvertiser(AdvertiserSignInDTO advertiserSignInDTO);

  void handleAdvertiserCallBack(
      String advertiserId,
      String campaignId,
      String trxId,
      String sourceId,
      String msisdn,
      String message,
      String activation);

  void handleUnsubscription(UnsubscribeRequest unsubscribeRequest);

  CampaignDetailsDTO getTotalAmountOwedByAdvertiser(String advertiserId);

  void handleSecureDWebhook(SecureDNotificationDto secureDNotificationDto);

  List<Advertiser> findAll();

  List<Campaign> getCampaigns(Long advertiserId);

  Long getNumberOfActiveCampaigns(Advertiser advertiser);

  Long getNumberOfCampaigns(Advertiser advertiser);

  List<Campaign> getActiveCampaigns(Advertiser advertiser);

  List<Advertiser> getAllAdvertiser();

  String verifySignupToken(String jwtToken);

  CampaignSummaryDTO getTotalCampaignStats(Object username);

  CampaignSummaryDTO getActiveCampaignStats(String username);

  SpendSummaryDto getSpendStats(String username);

  List<CampaignAnalyticsResponseDto> getAnalyticsCampaignStats(String username, Integer year);

  Page<Campaign> findAllCampaigns(CampaignFilter filter, Pageable pageable, String username);

  Campaign createCampaign(CreateCampaignDTO createCampaignDTO, String username) throws IOException;

  Campaign getCampaignDetails(String campaignId, String username);

  String deactivateCampaign(String campaignId);

  String activateCampaign(String campaignId);

  Advertiser getAdvertiserDetails(String username);

  String uploadCampaignImage(MultipartFile file) throws IOException;

  Campaign editCampaign(UpdateCampaignDTO editCampaignDTO, String campaignId);

  String deleteCampaign(String campaignId);

  List<ReportingChartDto> getAdvertiserReports(String username, ReportChartRequestDto dto);

  List<ReportingSummaryDto> getAdvertiserReportsSummary(
      String username, ReportSummaryRequestDto dto);

  Advertiser findAdvertiserById(Long advertiserId);

  List<AdvertiserNameResponse> getAllAdvertisersName();

  Advertiser createAdvertiserForAdmin(AdvertiserSignupDTO signupDTO);

  Advertiser getAdvertiserDetailsById(String id);

  Page<Advertiser> getAllAdvertisers(
      String businessName, AdvertiserStatus status, Pageable pageable);

  List<AdvertiserConversionDTO> getAdvertiserConversions(
      String username, AdvertiserConversionReportRequestDTO dto);

  void updateAdvertiser(Advertiser advertiser);

  void deleteAdvertiser(Advertiser advertiser);

  List<AdvertiserChurnReportDTO> generateChurnReport(String username, AdvertiserChurnReportRequestDTO reportRequestDTO);

  List<AutoFillDTO> getCampaignsForAdvertiser(String username);

  void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;

    Advertiser save(Advertiser advertiser);

  long totalBulkSMSAdvertisers();
  long totalActiveBulkSMSAdvertisers();

  List<Advertiser> getAllBulkSMSAdvertisers(Pageable pageable);

}

