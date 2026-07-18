package com.nitax.valueplusbackend.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.nitax.valueplusbackend.domain.Admin;
import com.nitax.valueplusbackend.domain.Advertiser;
import com.nitax.valueplusbackend.domain.AdvertiserStatus;
import com.nitax.valueplusbackend.domain.CPASettings;
import com.nitax.valueplusbackend.domain.Campaign;
import com.nitax.valueplusbackend.domain.Publisher;
import com.nitax.valueplusbackend.domain.PublisherCampaign;
import com.nitax.valueplusbackend.domain.SMSLog;
import com.nitax.valueplusbackend.dto.CampignUrlDto;
import com.nitax.valueplusbackend.dto.request.AdminLoginRequest;
import com.nitax.valueplusbackend.dto.request.AdvertiserConversionRequestDTO;
import com.nitax.valueplusbackend.dto.request.AdvertiserSignupDTO;
import com.nitax.valueplusbackend.dto.request.AdvertiserUpdateRequest;
import com.nitax.valueplusbackend.dto.request.CPASettingRequest;
import com.nitax.valueplusbackend.dto.request.CampaignDsableRequest;
import com.nitax.valueplusbackend.dto.request.CampaignFilter;
import com.nitax.valueplusbackend.dto.request.CampaignPauseRequest;
import com.nitax.valueplusbackend.dto.request.ChurnReportRequestDTO;
import com.nitax.valueplusbackend.dto.request.CreateCampaignForAdminDTO;
import com.nitax.valueplusbackend.dto.request.CreatePublisherDTO;
import com.nitax.valueplusbackend.dto.request.DeleteCampaignRequestDto;
import com.nitax.valueplusbackend.dto.request.FundWalletRequest;
import com.nitax.valueplusbackend.dto.request.PublisherCampaignRequest;
import com.nitax.valueplusbackend.dto.request.PublisherConversionRequestDTO;
import com.nitax.valueplusbackend.dto.request.SendSMSByList;
import com.nitax.valueplusbackend.dto.request.SmsPointInfoResponse;
import com.nitax.valueplusbackend.dto.request.UpdateCampaignForAdminDTO;
import com.nitax.valueplusbackend.dto.request.UpdatePublisherDTO;
import com.nitax.valueplusbackend.dto.response.AdminBulkSmsDashboardSummary;
import com.nitax.valueplusbackend.dto.response.AdvertiserConversionCpaBreakdownDTO;
import com.nitax.valueplusbackend.dto.response.AdvertiserConversionDTO;
import com.nitax.valueplusbackend.dto.response.AdvertiserConversionDTOForTop;
import com.nitax.valueplusbackend.dto.response.AdvertiserNameResponse;
import com.nitax.valueplusbackend.dto.response.AutoFillDTO;
import com.nitax.valueplusbackend.dto.response.BulkSMSAdvertiserResponse;
import com.nitax.valueplusbackend.dto.response.ChurnReport;
import com.nitax.valueplusbackend.dto.response.CommonSubscriberStats;
import com.nitax.valueplusbackend.dto.response.LoginResponse;
import com.nitax.valueplusbackend.dto.response.MonthlyConversionCount;
import com.nitax.valueplusbackend.dto.response.PublisherCampaignConversionsDTO;
import com.nitax.valueplusbackend.dto.response.PublisherConversionsDTO;
import com.nitax.valueplusbackend.dto.response.SearchPostbackDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

public interface AdminService {
  public CommonSubscriberStats getCommonSubscribersForCampaigns();

  CommonSubscriberStats getCommonSubscribersForPublishers();

  SMSLog sendSMSByList(SendSMSByList dto);

  LoginResponse adminLogin(AdminLoginRequest dto);

  Long getActiveCampaigns();

  Long getPausedCampaigns();

  Long getDisabledCampaigns();

  List<AdvertiserConversionDTOForTop> getTopFiveCampaigns();

  List<AdvertiserConversionDTOForTop> getLeastFiveCampaigns();

  List<MonthlyConversionCount> getCampaignPerformanceOverview();

  List<MonthlyConversionCount> getCampaignPerformanceOverviewClicks();

  Page<Campaign> getAllCampaigns(CampaignFilter filter, Pageable pageable);

  Campaign getCampaignById(String id);

  List<AdvertiserConversionDTO> getAdvertiserConversions(AdvertiserConversionRequestDTO dto);

  List<PublisherCampaignConversionsDTO> getPublishersCampaignConversions(
      PublisherConversionRequestDTO dto);

  List<PublisherConversionsDTO> getPublishersConversions(PublisherConversionRequestDTO dto);

  List<CampignUrlDto> getCampaignUrls(String campaignId);

  Campaign updateCampaign(String campaignId, UpdateCampaignForAdminDTO dto);

  String uploadCampaignImage(MultipartFile file) throws IOException;

  Campaign createCampaign(CreateCampaignForAdminDTO dto);

  Campaign pauseCampaign(CampaignPauseRequest dto, String campaignId);

  Campaign enableCampaign(String campaignId);

  String deleteCampaign(DeleteCampaignRequestDto dto, String campaignId);

  Campaign disableCampaign(String campaignId, CampaignDsableRequest dto);

  List<AdvertiserNameResponse> getAdvertiserNames();

  String createAdvertiser(AdvertiserSignupDTO signupDTO);

  Advertiser getAdvertiserInfo(String id);

  List<SearchPostbackDto> getPostbacks(String transactionId, Pageable pageable);

  Page<Advertiser> getAllAdvertisers(String businessName, AdvertiserStatus status, Pageable pageable);

  String approveAdvertiser(String id);
  String rejectAdvertiser(String id);
  String suspendAdvertiser(String id, String suspensionReason);
  String deleteAdvertiser(String id);

  String savePublisher(@Valid CreatePublisherDTO publisherDTO);

  String updatePublisher(String pubId, @Valid UpdatePublisherDTO publisherDTO);

  List<Publisher> getAllPublishers();

  Publisher findByPubId(String pubId);

  String approvePublisher(String id);

  String suspendPublisher(String id);

  String deletePublisher(String id);

  PublisherCampaign createPublisherCampaign(@Valid PublisherCampaignRequest request);

  PublisherCampaign updatePublisherCampaign(String pubCampId, @Valid PublisherCampaignRequest request);

  List<PublisherCampaign> getPublisherCampaigns(String publisherId, String campaignId);

  void deletePublisherCampaign(String pubCampId);

  CPASettings createCPASetting(CPASettingRequest request);

  CPASettings updateCPASetting(String cpaId, CPASettingRequest request);

  List<CPASettings> getCPAList(String cpaId);

  void deleteCPA(String cpaId);

  List<CPASettings> getCPAList(String country, String mno, String flow, String flowType);

  List<? extends ChurnReport> generateReport(ChurnReportRequestDTO reportRequestDTO);

  List<AutoFillDTO> autoFillPublisher(String keys);

  List<AutoFillDTO> autoFillCampaigns(String keys);

  Admin getAdminByEmail(String email);

  void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;

  AdminBulkSmsDashboardSummary getBulkSmsDashboardSummary();

  List<BulkSMSAdvertiserResponse> getBulkSMSAdvertisers(int page, int size);

  SmsPointInfoResponse getSmsPointInfo(String advertiserId, BigDecimal amount);

  void fundWallet(FundWalletRequest request);

  SmsPointInfoResponse getAdvertiserSmsPointInfo(String advertiserId);

  Advertiser updateAdvertiser(String advertiserId, AdvertiserUpdateRequest request);

  List<AdvertiserConversionCpaBreakdownDTO> getAdvertiserConversionsCpaBreakdown(
      AdvertiserConversionRequestDTO dto);
}
