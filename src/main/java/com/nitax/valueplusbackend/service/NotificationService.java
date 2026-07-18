package com.nitax.valueplusbackend.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.nitax.valueplusbackend.domain.Advertiser;
import com.nitax.valueplusbackend.domain.Notification;
import com.nitax.valueplusbackend.dto.CampaignMetricsDTO;
import com.nitax.valueplusbackend.dto.ChurnType;
import com.nitax.valueplusbackend.dto.NotificationDto;
import com.nitax.valueplusbackend.dto.PubChurnReportDto;
import com.nitax.valueplusbackend.dto.PublisherConversionReportDto;
import com.nitax.valueplusbackend.dto.ReportingChartDto;
import com.nitax.valueplusbackend.dto.RetentionReportDto;
import com.nitax.valueplusbackend.dto.request.AdvertiserChurnReportRequestDTO;
import com.nitax.valueplusbackend.dto.request.AdvertiserConversionReportRequestDTO;
import com.nitax.valueplusbackend.dto.request.AdvertiserConversionRequestDTO;
import com.nitax.valueplusbackend.dto.response.PublisherChurnRecordDTO;

import com.nitax.valueplusbackend.dto.request.PublisherConversionRequestDTO;
import com.nitax.valueplusbackend.dto.request.ReportChartRequestDto;
import com.nitax.valueplusbackend.dto.request.ReportSummaryRequestDto;
import com.nitax.valueplusbackend.dto.response.AdvertiserChurnReportDTO;
import com.nitax.valueplusbackend.dto.response.AdvertiserConversionCpaBreakdownDTO;
import com.nitax.valueplusbackend.dto.response.AdvertiserConversionDTO;
import com.nitax.valueplusbackend.dto.response.ChurnReport;
import com.nitax.valueplusbackend.dto.response.MonthlyConversionCount;
import com.nitax.valueplusbackend.dto.response.PublisherCampaignConversionsDTO;
import com.nitax.valueplusbackend.dto.response.PublisherConversionsDTO;
import com.nitax.valueplusbackend.dto.response.ReportingSummaryDto;
import com.nitax.valueplusbackend.dto.response.SearchPostbackDto;

public interface NotificationService {


  Notification saveNotification(Notification notification);

  Optional<Notification> findClickByShortTrxId(String shortTrxId);

  boolean existsByTransactionId(String transactionId);

  boolean existsByMsisdn(String msisdn);

  boolean existsUnsubscriptionByTransactionIdAndMsisdn(String transactionId, String msisdn);

  Notification findByTrxIdAndProductId(String trxId, String productId);

  Notification findTopByMsisdnOrderByCreatedAtDesc(String transactionId);

  long getTotalSpendForPreviousMonth(String campaignId);

  List<Notification> findAllMonthly();

  List<NotificationDto> getWeeklyUnsubscribersForPublisher(String id);

  List<NotificationDto> getDailyUnsubscribersForPublisher(String id);

  List<NotificationDto> getMonthlyNotificationsForCampaign(String campaignId);

  List<PublisherConversionReportDto> getMonthlyNotificationsForPublishers(String pubId);

  Long getCountOfDailyNotificationsForAdvertisers(Advertiser advertiser);

  List<NotificationDto> getDailyNotificationsForCampaign(String campaignId);

  Long getCountOfMonthlyNotificationsForAdvertisers(Advertiser advertiser);

  List<NotificationDto> getWeeklyNotificationsForCampaign(String campaignId);

  long getCountOfCommonSubscribersForCampaigns();

  long getCountOfUniqueSubscribersForCampaigns();


  long getCountOfCommonSubscribersForPublishers();

  long getCountOfUniqueSubscribersForPublishers();

  long getCountOfUnsubscribersWithDateRangeAndOptionalCampaignId(
      Instant startDate, Instant endDate, String campaignId);

  long getCountOfTotalSubscribersWithDateRangeAndOptionalCampaignId(
      Instant startDate, Instant endDate, String campaignId);


  void recordClickEvent(String campaignId, String trxId, String shortTrxId, String sourceId, Double cpaRevenue, Double vpRevenue);


  List<ReportingChartDto> getAdvertiserClicksReport(String advertiserId, ReportChartRequestDto dto);

  List<ReportingChartDto> getAdvertiserConversionsReport(
      String advertiserId, ReportChartRequestDto dto);

  List<ReportingChartDto> getAdvertiserChurnReport(String advertiserId, ReportChartRequestDto dto);

  List<ReportingChartDto> getAdvertiserRetentionReport(
      String advertiserId, ReportChartRequestDto dto);

  List<ReportingSummaryDto> getAdvertiserReportsSummary(
      String username, ReportSummaryRequestDto dto);

  List<MonthlyConversionCount> getCampaignPerformanceOverview();

  List<MonthlyConversionCount> getCampaignPerformanceOverviewClicks();

  List<AdvertiserConversionDTO> getAdvertiserConversionsForAdmin(
      AdvertiserConversionRequestDTO dto);

  List<PublisherCampaignConversionsDTO> getPublishersCampaignConversions(
      PublisherConversionRequestDTO dto);

  List<Notification> getPendingNotifications();

  List<PublisherConversionsDTO> getPublishersConversions(PublisherConversionRequestDTO dto);

  List<SearchPostbackDto> getPostbacks(String transactionId, Pageable pageable);

  List<AdvertiserConversionDTO> getAdvertiserConversionsForAdvertiser(
      AdvertiserConversionReportRequestDTO dto);

  CampaignMetricsDTO calculateCampaignCostForCurrentMonth(String campaignId);

  Notification getLastConversionForPublisher(String pubId);

  void deleteOldRecords();

  List<NotificationDto> getDailyNotificationsForPublisher(String pubId);

  List<NotificationDto> getWeeklyNotificationsForPublisher(String pubId);

  List<RetentionReportDto> getWeeklyRetentionForPublisher(String pubId);

  List<PubChurnReportDto> getWeeklyChurnForValueplus();

  List<PubChurnReportDto> getDailyChurnForPublisher();

  List<Notification> findByMsisdn(String subMsisdn);

  public List<? extends ChurnReport> fetchReports(List<String> campaigns,
                                                  List<String> publishers,
                                                  LocalDateTime startDate,
                                                  LocalDateTime endDate,
                                                  ChurnType churnType,
                                                  boolean includeSourceId);

  List<com.nitax.valueplusbackend.dto.AdminChurnReportDto> fetchAdminChurnReport(Instant startDate, Instant endDate);

  List<com.nitax.valueplusbackend.dto.AdminChurnReportDto> fetchAdminChurnReport(Instant startDate, Instant endDate, int churnDurationHours);


  List<PublisherChurnRecordDTO> fetchPublisherApiReport(String publisherId,
                                                      LocalDateTime startDate,
                                                      LocalDateTime endDate);

  List<PublisherChurnRecordDTO> fetchPublisherApiReport48hrs(String publisherId,
                                                             LocalDateTime startDate,
                                                             LocalDateTime endDate);

  List<PublisherChurnRecordDTO> fetchPublisherConversions(String publisherId,
                                                      LocalDateTime startDate,
                                                      LocalDateTime endDate);

  long getTotalSpendForPreviousMonthForAdvertiser(Advertiser advertiser);

  List<AdvertiserChurnReportDTO> getAdvertiserChurnAndAcquisition(Advertiser advertiser, AdvertiserChurnReportRequestDTO reportRequestDTO);

  List<Object[]> fetchPublisherMetrics(String publisherId,Instant startDate, Instant endDate, int churnPeriod, boolean sourceId);

  List<AdvertiserConversionCpaBreakdownDTO> getAdvertiserConversionsCpaBreakdown(
      AdvertiserConversionRequestDTO dto);
}
