package com.nitax.valueplusbackend.service;

import com.nitax.valueplusbackend.domain.BulkSmsCampaign;
import com.nitax.valueplusbackend.domain.BulkSmsCampaignStatus;
import com.nitax.valueplusbackend.dto.request.CreateBulkSmsCampaignRequest;
import com.nitax.valueplusbackend.dto.response.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

public interface BulkSmsCampaignService {
    CreateBulkSmsCampaignResponse createBulkSms(CreateBulkSmsCampaignRequest request, MultipartFile csv) throws IOException;

    GetBulkSmsCostEstimate getSmsPointCostEstimateAndValidate(MultipartFile csv,String advertiserId) throws IOException;
    GetBulkSmsCostEstimate getSmsPointCostEstimate(long targetNumbers,String advertiserId);

    List<BulkSmsCampaign> findByStatus(BulkSmsCampaignStatus bulkSmsCampaignStatus);
    List<BulkSmsCampaignResponse> findAll(int page, int size);


    List<BulkSmsCampaign> findByStatusAndAdveritserId(BulkSmsCampaignStatus bulkSmsCampaignStatus, long advertiserId);

    BulkSmsCampaign save(BulkSmsCampaign campaign);

    BulkSmsDashboardSummaryDto  getDashboardSummary();

    BulkSmsCampaignResponse getBulkSmsCampaignDetailsByCampaignId(String bulkSmsCampaignId);

    BulkSmsCampaignManagementResponse getAdvertiserCampaignStats(Instant startDate, Instant endDate);

    List<BulkSmsCampaignResponse> getCampaigns(int size, int page, Instant startDate, Instant endDate,String name);

    GeographicResponse getSystemNumbersGeographicDetails();

    void  saveAll(Iterable<BulkSmsCampaign> campaigns);

    BulkSmsCampaign getBulkSmsCampaignById(long id);

    AdminCampaignSummaryResponse getAdminCampaignSummary(String startDateString, String endDateString);

    List<AdminAdvertiserCampaignResponse> getAdminAdvertiserCampaigns(int page, int size, String startDateString, String endDateString);

    List<HourlyDeliveryRate> getTop3DeliveryRatesByHour();

    List<CampaignDeliveryRate> getCampaignDeliveryRates();

    List<SMSDeliveryStatusRes> getLiveDeliveryStatus(int page,int size,String startDate, String endDate);

    List<BulkSMSReportResponse> generateReports(String startDate, String endDate);

}
