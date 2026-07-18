package com.nitax.valueplusbackend.repository;

import com.nitax.valueplusbackend.domain.Advertiser;
import com.nitax.valueplusbackend.domain.Notification;
import com.nitax.valueplusbackend.domain.NotificationView;
import com.nitax.valueplusbackend.dto.NotificationDto;
import com.nitax.valueplusbackend.dto.PublisherConversionReportDto;
import com.nitax.valueplusbackend.dto.ReportingChartDto;
import com.nitax.valueplusbackend.dto.response.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface NotificationViewRepository extends JpaRepository<NotificationView, Long> {
  Optional<NotificationView> findBytransactionIdAndProductId(String trxId, String productId);

  @Query(
      "SELECT n FROM NotificationView n WHERE n.msisdn = :msisdn AND n.duration IS NULL AND n.transactionId = :transactionId ORDER BY n.createdDate DESC LIMIT 1")
  NotificationView findTopByMsisdnAndDurationIsNullAndTransactionIdOrderByCreatedDateDesc(
      @Param("msisdn") String msisdn, @Param("transactionId") String transactionId);

  @Query(
      "SELECT COUNT(n) FROM NotificationView n "
          + "WHERE n.status != 'PUBLISHER_HOOK_RECEIVED' and (n.message = 'Success' or n.message = 'publisher callback sent' OR n.duration >= 0) AND n.campaignId = :campaignId AND n.createdDate BETWEEN :startOfDay AND :endOfDay")
  long countConversionsByCampaignIdWithDateRange(
      @Param("campaignId") String campaignId,
      @Param("startOfDay") Instant startOfDay,
      @Param("endOfDay") Instant endOfDay);

  @Query(
      "SELECT n FROM NotificationView n "
          + "WHERE n.status = :status or n.status = 'UNSUBSCRIBED' AND n.createdDate BETWEEN :startOfDay AND :endOfDay")
  List<NotificationView> findByStatusWithDateRange(
      Notification.NotificationStatus status,
      @Param("startOfDay") Instant startOfDay,
      @Param("endOfDay") Instant endOfDay);

  @Query(
      "SELECT new com.nitax.valueplusbackend.dto.NotificationDto("
          + "n.createdDate, "
          + "n.status, "
          + "(CAST(REGEXP_REPLACE(n.transactionId, '^valueplus_[^_]+_', '') AS string)), "
          + "n.sourceId,"
          + "n.duration, "
          + "c.name, "
          + "c.cpaCostPerUser)"
          + "FROM NotificationView n "
          + "INNER JOIN Campaign c ON n.campaignId = c.campaignId "
          + "WHERE n.status = 'UNSUBSCRIBED' AND n.publisherId = :publisherId "
          + "AND n.unsubscribeTimestamp BETWEEN :startDate AND :endDate AND n.createdDate BETWEEN :startDate AND :endDate "
          + "ORDER BY n.createdDate DESC")
  List<NotificationDto> findUnsubscribersForPublisherWithDateRange(
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate,
      @Param("publisherId") String publisherId);

  @Query(
      "SELECT new com.nitax.valueplusbackend.dto.NotificationDto("
          + "n.createdDate, "
          + "n.status, "
          + "(CAST(REGEXP_REPLACE(n.transactionId, '^valueplus_[^_]+_', '') AS string)), "
          + "n.sourceId,"
          + "n.duration, "
          + "c.name, "
          + "c.cpaCostPerUser)"
          + "FROM NotificationView n "
          + "INNER JOIN Campaign c ON n.campaignId = c.campaignId "
          + "WHERE n.campaignId = :campaignId "
          + "AND n.status != 'PUBLISHER_HOOK_RECEIVED' AND (n.message = 'Success' OR n.message = 'publisher callback sent' OR n.duration >= 0) "
          + "AND :status IS NOT NULL "
          + "AND n.createdDate >= :startDate  AND n.createdDate <= :endDate "
          + "ORDER BY n.createdDate DESC")
  List<NotificationDto> findNotificationsForCampaignWithDateRange(
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate,
      @Param("status") Notification.NotificationStatus status,
      @Param("campaignId") String campaignId);

  @Query(
      "SELECT new com.nitax.valueplusbackend.dto.PublisherConversionReportDto("
          + "c.name,"
          + "COUNT(CASE WHEN n.status = 'PUBLISHER_HOOK_SENT' OR n.duration > 7 THEN 1 END) AS totalGoodAcquisition,"
          + "COUNT(CASE WHEN n.status = 'UNSUBSCRIBED' AND n.duration <= 7 THEN 1 END) AS totalBadAcquisition,"
          + "c.cpaCostPerUser as cpa), "
          + "0 as totalCost "
          + "FROM NotificationView n "
          + "JOIN Campaign c ON n.campaignId = c.campaignId "
          + "WHERE n.publisherId = :pubId "
          + "AND  n.createdDate BETWEEN :startDate AND :endDate "
          + "GROUP BY c.name, c.cpaCostPerUser")
  List<PublisherConversionReportDto> findNotificationsForPublishersWithDateRange(
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate,
      @Param("pubId") String pubId);

  @Query(
      "SELECT COUNT(n) FROM NotificationView n "
          + "JOIN Campaign c ON n.campaignId = c.campaignId "
          + "WHERE c.advertiser = :advertiser "
          + "AND n.status <> :status "
          + "AND  n.createdDate BETWEEN :startDate AND :endDate")
  Long getCountOfNotificationsForAdvertisersWithDateRange(
      @Param("advertiser") Advertiser advertiser,
      @Param("status") Notification.NotificationStatus status,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query(
      value =
          "SELECT COUNT(DISTINCT msisdn) "
              + "FROM ( "
              + "    SELECT hn.msisdn "
              + "    FROM hook_notifications_view hn "
              + " WHERE  hn.status <> 'PUBLISHER_HOOK_RECEIVED' "
              + "    GROUP BY hn.msisdn "
              + "    HAVING COUNT(DISTINCT hn.campaign_id) > 1 "
              + ") AS common_subscribers",
      nativeQuery = true)
  long getCountOfCommonSubscribers();

  long countDistinctByMsisdnIsNotNull();

  long countByMsisdnIsNotNull();

  @Query(
      value =
          "SELECT COUNT(DISTINCT msisdn) "
              + "FROM ( "
              + "    SELECT hn.msisdn "
              + "    FROM hook_notifications_view hn "
              + " WHERE  hn.status <> 'PUBLISHER_HOOK_RECEIVED' "
              + "    GROUP BY hn.msisdn "
              + "    HAVING COUNT(DISTINCT hn.publisher_id) > 1 "
              + ") AS common_subscribers",
      nativeQuery = true)
  long getCountOfCommonSubscribersForPublishers();

  @Query(
      "SELECT COUNT(n) FROM NotificationView n "
          + "WHERE n.createdDate BETWEEN :startDate AND :endDate "
          + "AND n.status = 'UNSUBSCRIBED' "
          + "AND (:campaignId IS NULL OR n.campaignId = :campaignId)")
  long getCountOfUnsubscribersWithDateRangeAndOptionalCampginId(
      Instant startDate, Instant endDate, String campaignId);

  @Query(
      "SELECT COUNT(n) FROM NotificationView n "
          + "WHERE n.createdDate BETWEEN :startDate AND :endDate "
          + "AND n.status <> 'PUBLISHER_HOOK_RECEIVED' "
          + "AND (:campaignId IS NULL OR n.campaignId = :campaignId)")
  long getCountOfTotalSubscribersWithDateRangeAndOptionalCampaignId(
      Instant startDate, Instant endDate, String campaignId);

  @Query(
      value =
          "SELECT COUNT(n) FROM NotificationView n "
              + "WHERE n.campaignId = :campaignId AND n.status != 'PUBLISHER_HOOK_RECEIVED' AND (n.message = 'Success' OR n.message = 'publisher callback sent' OR n.duration >= 0)")
  long getAllTimeConversionCountByCampaignId(String campaignId);

  @Query(
      value =
          "SELECT COUNT(n) FROM NotificationView n "
              + "WHERE n.campaignId = :campaignId AND n.status = 'PUBLISHER_HOOK_RECEIVED'")
  long getAllTimeCountByCampaignId(String campaignId);

  @Query("SELECT n FROM NotificationView n WHERE n.transactionId = :trxId")
  List<NotificationView> findByTransactionIdContaining(@Param("trxId") String trxId);

  @Query(
      "SELECT new com.nitax.valueplusbackend.dto.ReportingChartDto(date_trunc('day', n.createdDate) AS day, count(n) AS count) "
          + "FROM NotificationView n "
          + "JOIN Campaign c ON n.campaignId = c.campaignId "
          + "JOIN Advertiser a ON c.advertiser.id = a.id "
          + "WHERE n.status = 'PUBLISHER_HOOK_RECEIVED' AND n.createdDate BETWEEN :startDate AND :endDate "
          + "AND a.advertiserId = :advertiserId "
          + "GROUP BY date_trunc('day', n.createdDate)"
          + "ORDER BY day ASC")
  List<ReportingChartDto> getAdvertiserClicksReport(
      String advertiserId, Instant startDate, Instant endDate);

  @Query(
      "SELECT new com.nitax.valueplusbackend.dto.ReportingChartDto(date_trunc('day', n.createdDate) AS day, count(n) AS count) "
          + "FROM NotificationView n "
          + "JOIN Campaign c ON n.campaignId = c.campaignId "
          + "JOIN Advertiser a ON c.advertiser.id = a.id "
          + "WHERE n.createdDate BETWEEN :startDate AND :endDate "
          + "AND a.advertiserId = :advertiserId "
          + "AND n.status != 'PUBLISHER_HOOK_RECEIVED' "
          + "AND (n.message = 'Success' OR n.message = 'publisher callback sent' OR n.duration >= 0) "
          + "GROUP BY date_trunc('day', n.createdDate)"
          + "ORDER BY day ASC")
  List<ReportingChartDto> getAdvertiserConversionsReport(
      String advertiserId, Instant startDate, Instant endDate);

  @Query(
      "SELECT new com.nitax.valueplusbackend.dto.ReportingChartDto(date_trunc('day', n.createdDate) AS day, count(n) AS count) "
          + "FROM NotificationView n "
          + "JOIN Campaign c ON n.campaignId = c.campaignId "
          + "JOIN Advertiser a ON c.advertiser.id = a.id "
          + "WHERE n.createdDate BETWEEN :startDate AND :endDate "
          + "AND a.advertiserId = :advertiserId "
          + "AND n.status = 'UNSUBSCRIBED' "
          + "GROUP BY date_trunc('day', n.createdDate)"
          + "ORDER BY day ASC")
  List<ReportingChartDto> getAdvertiserChurnReport(
      String advertiserId, Instant startDate, Instant endDate);

  @Query(
      "SELECT new com.nitax.valueplusbackend.dto.response.ReportingSummaryDto("
          + "YEAR(n.createdDate) AS year, "
          + "MONTH(n.createdDate) AS month, "
          + "c.costPerUser AS campaignCost, "
          + "SUM(CASE WHEN n.status != 'PUBLISHER_HOOK_RECEIVED' and (n.message = 'Success' or n.message = 'publisher callback sent' or n.duration >= 0) THEN 1 ELSE 0 END) AS conversionCount, "
          + "SUM(CASE WHEN n.status = 'PUBLISHER_HOOK_RECEIVED' THEN 1 ELSE 0 END) AS clickCount, "
          + "c.name AS campaignName, "
          + "c.budget AS budget) "
          + "FROM NotificationView n "
          + "JOIN Campaign c ON n.campaignId = c.campaignId "
          + "JOIN Advertiser a ON c.advertiser.id = a.id "
          + "WHERE a.advertiserId = :advertiserId "
          + "GROUP BY YEAR(n.createdDate), MONTH(n.createdDate), c.costPerUser, c.name, c.budget "
          + "ORDER BY year DESC, month DESC,  name ASC, conversionCount DESC ")
  List<ReportingSummaryDto> getAdvertiserReportsSummary(String advertiserId);

  @Query(
      "SELECT COUNT(n) FROM NotificationView n "
          + "WHERE n.campaignId = :campaignId AND n.status = 'PUBLISHER_HOOK_RECEIVED' AND n.createdDate BETWEEN :startOfDay AND :endOfDay")
  long countClicksByCampaignIdWithDateRange(
      String campaignId, Instant startOfDay, Instant endOfDay);

  @Query(
      "SELECT new com.nitax.valueplusbackend.dto.response.MonthlyConversionCount("
          + "YEAR(n.createdDate) as year, MONTH( n.createdDate) as month, COUNT(n) as count) "
          + "FROM NotificationView n "
          + "WHERE n.status <> 'PUBLISHER_HOOK_RECEIVED' "
          + "AND (n.message = 'Success' OR n.message = 'publisher callback sent' OR n.duration >= 0) "
          + "GROUP BY YEAR(n.createdDate), MONTH( n.createdDate) "
          + "ORDER BY YEAR(n.createdDate), MONTH( n.createdDate)")
  List<MonthlyConversionCount> getCampaignPerformanceOverview();

  @Query(
      "SELECT new com.nitax.valueplusbackend.dto.response.MonthlyConversionCount("
          + "YEAR(n.createdDate) as year, MONTH( n.createdDate) as month, COUNT(n) as count) "
          + "FROM NotificationView n "
          + "WHERE n.status = 'PUBLISHER_HOOK_RECEIVED' "
          + "GROUP BY YEAR(n.createdDate), MONTH( n.createdDate) "
          + "ORDER BY YEAR(n.createdDate), MONTH( n.createdDate)")
  List<MonthlyConversionCount> getCampaignPerformanceOverviewClicks();

  @Query(
      "SELECT new com.nitax.valueplusbackend.dto.response.AdvertiserConversionDTO("
          + "c.status, "
          + "a.businessName, "
          + "c.name, "
          + "SUM(CASE WHEN n.status <> 'PUBLISHER_HOOK_RECEIVED' AND (n.message = 'Success' OR n.message = 'publisher callback sent' OR n.duration >= 0) THEN 1 ELSE 0 END),"
          + "SUM(CASE WHEN n.status = 'PUBLISHER_HOOK_RECEIVED' THEN 1 ELSE 0 END), "
          + "SUM(CASE WHEN n.status = 'UNSUBSCRIBED' AND n.duration >= 0 THEN 1 ELSE 0 END),"
          + "c.budget, "
          + "c.costPerUser )"
          + "FROM NotificationView n "
          + "INNER JOIN Campaign c ON n.campaignId = c.campaignId "
          + "INNER JOIN Advertiser a ON c.advertiser.id = a.id "
          + "WHERE n.createdDate BETWEEN :startDate AND :endDate "
          + "AND (:advertiserName IS NULL OR a.businessName = :advertiserName) "
          + "GROUP BY c.status, a.businessName, c.name, c.budget, c.costPerUser")
  List<AdvertiserConversionDTO> getAdvertiserConversionsForAdmin(
      @Param("advertiserName") String advertiserName,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query(
      "SELECT new com.nitax.valueplusbackend.dto.response.PublisherCampaignConversionsDTO("
          + "p.name, "
          + "c.name, "
          + "SUM(CASE WHEN n.status = 'PUBLISHER_HOOK_SENT' OR n.duration > 7 THEN 1 ELSE 0 END),"
          + "SUM(CASE WHEN n.status = 'PUBLISHER_HOOK_RECEIVED' THEN 1 ELSE 0 END), "
          + "SUM(CASE WHEN n.duration < 8 THEN 1 ELSE 0 END), "
          + "c.cpaCostPerUser)"
          + "FROM NotificationView n "
          + "INNER JOIN Campaign c ON n.campaignId = c.campaignId "
          + "INNER JOIN Publisher p ON n.publisherId = p.pubId "
          + "WHERE n.createdDate BETWEEN :startDate AND :endDate "
          + "AND (:publisherName IS NULL OR p.name = :publisherName) "
          + "GROUP BY p.name, c.name, c.cpaCostPerUser")
  List<PublisherCampaignConversionsDTO> getPublisherConversionsForAdmin(
      @Param("publisherName") String advertiserName,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query("SELECT n FROM NotificationView n WHERE n.status = 'ADVERTISER_HOOK_RECEIVED'")
  List<NotificationView> findUnsentConversions(Pageable pageable);

  @Query(
      "SELECT new com.nitax.valueplusbackend.dto.response.PublisherConversionsDTO("
          + "p.name, "
          + "SUM(CASE WHEN n.status = 'PUBLISHER_HOOK_SENT' OR n.duration > 7 THEN 1 ELSE 0 END), "
          + "SUM(CASE WHEN n.status = 'PUBLISHER_HOOK_RECEIVED' THEN 1 ELSE 0 END), "
          + "SUM(CASE WHEN n.duration < 8 THEN 1 ELSE 0 END), "
          + "c.cpaCostPerUser) "
          + "FROM NotificationView n "
          + "INNER JOIN Campaign c ON n.campaignId = c.campaignId "
          + "INNER JOIN Publisher p ON n.publisherId = p.pubId "
          + "WHERE n.createdDate BETWEEN :startDate AND :endDate "
          + "AND (:publisherName IS NULL OR p.name = :publisherName) "
          + "GROUP BY p.name, c.cpaCostPerUser")
  List<PublisherConversionsDTO> getPublishersConversionsForAdmin(
      String publisherName, Instant startDate, Instant endDate);

  @Query(
      "SELECT new com.nitax.valueplusbackend.dto.response.SearchPostbackDto("
          + "c.name, "
          + "n.msisdn, "
          + "n.transactionId, "
          + "p.name, "
          + "n.status,"
          + "n.createdDate) "
          + "FROM NotificationView n "
          + "INNER JOIN Campaign c ON n.campaignId = c.campaignId "
          + "INNER JOIN Publisher p ON n.publisherId = p.pubId "
          + "WHERE :transactionId IS NULL OR  n.transactionId = :transactionId")
  List<SearchPostbackDto> searchByTransactionId(
      @Param("transactionId") String transactionId, Pageable pageable);

  @Query(
      "SELECT new com.nitax.valueplusbackend.dto.response.AdvertiserConversionDTO("
          + "c.status, "
          + "a.businessName, "
          + "c.name, "
          + "SUM(CASE WHEN n.status <> 'PUBLISHER_HOOK_RECEIVED' AND (n.message = 'Success' OR n.message = 'publisher callback sent' OR n.duration >= 0) THEN 1 ELSE 0 END),"
          + "SUM(CASE WHEN n.status = 'PUBLISHER_HOOK_RECEIVED' THEN 1 ELSE 0 END), "
          + "SUM(CASE WHEN n.status = 'UNSUBSCRIBED' AND n.duration >= 0 THEN 1 ELSE 0 END),"
          + "c.budget, "
          + "c.costPerUser )"
          + "FROM NotificationView n "
          + "INNER JOIN Campaign c ON n.campaignId = c.campaignId "
          + "INNER JOIN Advertiser a ON c.advertiser.id = a.id "
          + "WHERE n.createdDate BETWEEN :startDate AND :endDate "
          + "AND (:advertiserName IS NULL OR a.businessName = :advertiserName) "
          + "AND (:campaignName IS NULL OR c.name LIKE CONCAT('%', :campaignName, '%')) "
          + "GROUP BY c.status, a.businessName, c.name, c.budget, c.costPerUser")
  List<AdvertiserConversionDTO> getAdvertiserConversionsForAdvertiser(
      String advertiserName, String campaignName, Instant startDate, Instant endDate);

  @Transactional
  @Modifying
  @Query(
      value = "REFRESH MATERIALIZED VIEW CONCURRENTLY hook_notifications_view",
      nativeQuery = true)
  void refreshNotificationView();
}
