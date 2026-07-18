package com.nitax.valueplusbackend.repository;

import com.nitax.valueplusbackend.domain.Advertiser;
import com.nitax.valueplusbackend.domain.Notification;
import com.nitax.valueplusbackend.dto.NotificationDto;
import com.nitax.valueplusbackend.dto.PubChurnReportDto;
import com.nitax.valueplusbackend.dto.PublisherConversionReportDto;
import com.nitax.valueplusbackend.dto.ReportingChartDto;
import com.nitax.valueplusbackend.dto.RetentionReportDto;
import com.nitax.valueplusbackend.dto.response.SearchPostbackDto;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
  Optional<Notification> findBytransactionIdAndProductId(String trxId, String productId);

  boolean existsByShortTrxId(String shortTrxId);

  Optional<Notification> findFirstByShortTrxIdAndStatusOrderByCreatedDateDesc(
      String shortTrxId, Notification.NotificationStatus status);

  boolean existsByTransactionIdAndStatusIn(
      String transactionId, List<Notification.NotificationStatus> statuses);

  boolean existsByMsisdnAndStatusIn(String msisdn, List<Notification.NotificationStatus> statuses);

  boolean existsByTransactionIdAndMsisdnAndStatus(
      String transactionId, String msisdn, Notification.NotificationStatus status);

  List<Notification> findByMsisdnOrderByCreatedDateDesc(String msisdn);

  @Query(
      value =
          """
        SELECT *
        FROM hook_notifications_archive_partitioned n
        WHERE n.duration IS NULL
          AND n.msisdn = :msisdn
          AND n.status NOT IN ('UNSUBSCRIBED', 'INVALID')
        ORDER BY n.created_date DESC
        LIMIT 1
    """,
      nativeQuery = true)
  Notification findTopByMsisdnAndDurationIsNullAndTransactionIdOrderByCreatedDateDescFromArchive(
      @Param("msisdn") String msisdn);

  @Query(
      "SELECT COUNT(n) "
          + "FROM Notification n "
          + "WHERE n.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED') "
          + "  AND n.campaignId = :campaignId "
          + "  AND n.createdDate BETWEEN :startOfDay AND :endOfDay")
  long countConversionsByCampaignIdWithDateRange(
      @Param("campaignId") String campaignId,
      @Param("startOfDay") Instant startOfDay,
      @Param("endOfDay") Instant endOfDay);

  @Query(
      "SELECT SUM(n.vpRevenue) "
          + "FROM Notification n "
          + "JOIN Campaign c ON n.campaignId = c.campaignId "
          + "WHERE n.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED') "
          + "AND c.advertiser = :advertiserId "
          + "AND n.createdDate BETWEEN :startOfDay AND :endOfDay")
  long countConversionsByCampaignIdWithDateRangeForAdvertiser(
      @Param("advertiserId") Advertiser advertiserId,
      @Param("startOfDay") Instant startOfDay,
      @Param("endOfDay") Instant endOfDay);

  @Query(
      "SELECT n "
          + "FROM Notification n "
          + "WHERE n.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED') "
          + "AND n.createdDate BETWEEN :startOfDay AND :endOfDay")
  List<Notification> findByStatusWithDateRange(
      @Param("startOfDay") Instant startOfDay, @Param("endOfDay") Instant endOfDay);

  @Query(
      "SELECT new com.nitax.valueplusbackend.dto.NotificationDto("
          + "n.createdDate, "
          + "n.status, "
          + "(CAST(REGEXP_REPLACE(n.transactionId, '^^[^_]+_[^_]+_', '') AS string)), "
          + "n.sourceId,"
          + "n.duration, "
          + "c.name, "
          + "n.cpaRevenue,"
          + "n.vpRevenue)"
          + "FROM Notification n "
          + "INNER JOIN Campaign c ON n.campaignId = c.campaignId "
          + "WHERE n.status = 'UNSUBSCRIBED' AND n.publisherId = :publisherId AND n.campaignId IN ('vtJLLI7ukY', 'TbHLVKWKny', '9yyDUlsXTB', 'vly9DZ1Qv0') "
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
          + "(CAST(REGEXP_REPLACE(n.transactionId, '^^[^_]+_[^_]+_', '') AS string)), "
          + "n.sourceId,"
          + "n.duration, "
          + "c.name, "
          + "CAST(COALESCE(n.cpaRevenue, 0.0) AS double), "
          + "CAST(COALESCE(n.vpRevenue, 0.0) AS double)) "
          + "FROM Notification n "
          + "INNER JOIN Campaign c ON n.campaignId = c.campaignId "
          + "WHERE n.campaignId = :campaignId "
          + "AND n.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED') "
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
          + "c.name, "
          + "SUM(CASE WHEN n.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED') THEN 1 ELSE 0 END), " // totalGoodAcquisition
          + "SUM(CASE WHEN n.status = 'UNSUBSCRIBED' AND n.duration <= (7*24*60*60) THEN 1 ELSE 0 END), " // totalBadAcquisition
          + "COALESCE(SUM(n.cpaRevenue), 0), " // totalCost
          + "COALESCE(SUM(CASE WHEN n.status = 'UNSUBSCRIBED' AND n.duration <= (7*24*60*60) THEN n.cpaRevenue ELSE 0 END), 0)" // totalBadAcquisitionCost
          + ") "
          + "FROM Notification n "
          + "JOIN Campaign c ON n.campaignId = c.campaignId "
          + "WHERE n.publisherId = :pubId "
          + "AND n.createdDate BETWEEN :startDate AND :endDate "
          + "GROUP BY c.name, c.cpaCostPerUser")
  List<PublisherConversionReportDto> findNotificationsForPublishersWithDateRange(
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate,
      @Param("pubId") String pubId);

  @Query(
      "SELECT COUNT(n) "
          + "FROM Notification n "
          + "JOIN Campaign c ON n.campaignId = c.campaignId "
          + "WHERE c.advertiser = :advertiser "
          + "AND n.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED') "
          + "AND n.createdDate BETWEEN :startDate AND :endDate")
  Long getCountOfNotificationsForAdvertisersWithDateRange(
      @Param("advertiser") Advertiser advertiser,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query(
      value =
          "SELECT COUNT(DISTINCT msisdn) "
              + "FROM ( "
              + "    SELECT hn.msisdn "
              + "    FROM hook_notifications_archive_partitioned hn "
              + " WHERE  hn.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED') "
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
              + "    FROM hook_notifications_archive_partitioned hn "
              + " WHERE  hn.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED') "
              + "    GROUP BY hn.msisdn "
              + "    HAVING COUNT(DISTINCT hn.publisher_id) > 1 "
              + ") AS common_subscribers",
      nativeQuery = true)
  long getCountOfCommonSubscribersForPublishers();

  @Query(
      "SELECT COUNT(n) FROM Notification n "
          + "WHERE n.createdDate BETWEEN :startDate AND :endDate "
          + "AND n.status = 'UNSUBSCRIBED' "
          + "AND (:campaignId IS NULL OR n.campaignId = :campaignId)")
  long getCountOfUnsubscribersWithDateRangeAndOptionalCampginId(
      Instant startDate, Instant endDate, String campaignId);

  @Query(
      "SELECT COUNT(n) FROM Notification n "
          + "WHERE n.createdDate BETWEEN :startDate AND :endDate "
          + "AND n.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED') "
          + "AND (:campaignId IS NULL OR n.campaignId = :campaignId)")
  long getCountOfTotalSubscribersWithDateRangeAndOptionalCampaignId(
      Instant startDate, Instant endDate, String campaignId);

  @Query(
      value =
          "SELECT COUNT(n) "
              + "FROM Notification n "
              + "WHERE n.campaignId = :campaignId "
              + "AND n.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED')")
  long getAllTimeConversionCountByCampaignId(String campaignId);

  @Query(
      value =
          "SELECT COUNT(n) FROM Notification n "
              + "WHERE n.campaignId = :campaignId AND n.status = 'PUBLISHER_HOOK_RECEIVED'")
  long getAllTimeClickCountByCampaignId(String campaignId);

  @Query("SELECT n FROM Notification n WHERE n.transactionId = :trxId ORDER BY n.createdDate DESC")
  List<Notification> findByTransactionIdContaining(@Param("trxId") String trxId);

  @Query(
      "SELECT new com.nitax.valueplusbackend.dto.ReportingChartDto(n.day AS dbDay, count(n) AS count) "
          + "FROM Notification n "
          + "JOIN Campaign c ON n.campaignId = c.campaignId "
          + "JOIN Advertiser a ON c.advertiser.id = a.id "
          + "WHERE n.status = 'PUBLISHER_HOOK_RECEIVED' AND n.createdDate BETWEEN :startDate AND :endDate "
          + "AND a.advertiserId = :advertiserId "
          + "GROUP BY n.day "
          + "ORDER BY day ASC")
  List<ReportingChartDto> getAdvertiserClicksReport(
      String advertiserId, Instant startDate, Instant endDate);

  @Query(
      value =
          """
            SELECT n.day AS dbDay, count(n) AS clickCount
            FROM hook_notifications_archive_partitioned n
            JOIN campaigns c ON n.campaign_id = c.campaign_id
            JOIN advertisers a ON c.advertiser_id = a.id
            WHERE n.status = 'PUBLISHER_HOOK_RECEIVED'
            AND n.created_date BETWEEN :startDate AND :endDate
            AND a.advertiser_id = :advertiserId
            GROUP BY n.day
            ORDER BY dbDay ASC
            """,
      nativeQuery = true)
  List<Object[]> getAdvertiserClicksReportNative(
      @Param("advertiserId") String advertiserId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query(
      value =
          """
            SELECT n.day AS dbDay, count(n) AS ConversionCount
            FROM hook_notifications_archive_partitioned n
            JOIN campaigns c ON n.campaign_id = c.campaign_id
            JOIN advertisers a ON c.advertiser_id = a.id
            WHERE n.created_date BETWEEN :startDate AND :endDate
            AND a.advertiser_id = :advertiserId
            AND n.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED')
            GROUP BY dbDay
            ORDER BY dbDay ASC
            """,
      nativeQuery = true)
  List<Object[]> getAdvertiserConversionsReportArchiveNative(
      @Param("advertiserId") String advertiserId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query(
      value =
          """
            SELECT n.day AS dbDay, count(n) AS ChurnCount
            FROM hook_notifications_archive_partitioned n
            JOIN campaigns c ON n.campaign_id = c.campaign_id
            JOIN advertisers a ON c.advertiser_id = a.id
            WHERE n.created_date BETWEEN :startDate AND :endDate
            AND a.advertiser_id = :advertiserId
            AND n.status = 'UNSUBSCRIBED'
            GROUP BY dbDay
            ORDER BY dbDay ASC
            """,
      nativeQuery = true)
  List<Object[]> getAdvertiserChurnReportArchiveNative(
      @Param("advertiserId") String advertiserId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query(
      value =
          """

                      SELECT n.year AS year,
                      n.month AS month,
                      SUM (n.vp_revenue) AS campaignCost,
                      SUM(CASE WHEN n.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED') THEN 1 ELSE 0 END) AS conversionCount,
                      SUM(CASE WHEN n.status = 'PUBLISHER_HOOK_RECEIVED' THEN 1 ELSE 0 END) AS clickCount,
                      c.name AS campaignName,
                      c.budget AS budget
                      FROM hook_notifications_archive_partitioned n
                      JOIN campaigns c ON n.campaign_id = c.campaign_id
                      JOIN advertisers a ON c.advertiser_id = a.id
                      WHERE a.advertiser_id = :advertiserId AND n.created_date BETWEEN :startDate AND :endDate
                      GROUP BY n.year, n.month, c.cost_per_user, c.name, c.budget
                      ORDER BY year DESC, month DESC, c.name ASC, conversionCount DESC;
                    """,
      nativeQuery = true)
  List<Object[]> getAdvertiserReportsSummaryArchiveNative(
      @Param("advertiserId") String advertiserId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query(
      "SELECT COUNT(n) FROM Notification n "
          + "WHERE n.campaignId = :campaignId AND n.status = 'PUBLISHER_HOOK_RECEIVED' AND n.createdDate BETWEEN :startOfDay AND :endOfDay")
  long countClicksByCampaignIdWithDateRange(
      String campaignId, Instant startOfDay, Instant endOfDay);

  @Query(
      value =
          """
        SELECT
            n.year AS year,
            n.month AS month,
            COUNT(*) AS count
        FROM
            hook_notifications_archive_partitioned n
        WHERE
            n.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED')
        GROUP BY
            n.year,
            n.month
        """,
      nativeQuery = true)
  List getCampaignPerformanceOverview();

  @Query(
      value =
          """
        SELECT
            n.year AS year,
            n.month AS month,
            COUNT(*) AS count
        FROM
            hook_notifications_archive_partitioned n
        WHERE
            n.status = 'PUBLISHER_HOOK_RECEIVED'
        GROUP BY
            n.year, n.month
        """,
      nativeQuery = true)
  List<Object[]> getCampaignPerformanceOverviewClicks();

  @Query(
      value =
          "SELECT "
              + "c.status, "
              + "a.business_name AS businessName, "
              + "c.name, "
              + "SUM(CASE "
              + "WHEN n.status IN ('PUBLISHER_HOOK_SENT' ,'ADVERTISER_HOOK_RECEIVED') "
              + "THEN 1 ELSE 0 END) AS conversions, "
              + "SUM(CASE WHEN n.status = 'PUBLISHER_HOOK_RECEIVED' THEN 1 ELSE 0 END) AS clicks, "
              + "SUM(CASE WHEN n.status = 'UNSUBSCRIBED' AND n.duration < (72*60*60) THEN 1 ELSE 0 END) AS churn, "
              + "c.budget, "
              + "c.country as campaignCountry, "
              + "c.cost_per_user AS costPerUser, "
              + "COALESCE(SUM(CASE WHEN n.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED') THEN n.vp_revenue ELSE 0 END), 0) AS totalCost "
              + "FROM  hook_notifications_archive_partitioned n "
              + "INNER JOIN campaigns c ON n.campaign_id = c.campaign_id "
              + "INNER JOIN advertisers a ON c.advertiser_id = a.id "
              + "WHERE n.created_date BETWEEN :startDate AND :endDate "
              + "GROUP BY c.status, a.business_name, c.name, c.budget, c.cost_per_user, c.country",
      nativeQuery = true)
  List<Object[]> getAdvertiserConversionsForAdmin(
      @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

  @Query(
      value =
          "SELECT "
              + "c.status, "
              + "a.business_name AS businessName, "
              + "c.name, "
              + "SUM(CASE "
              + "WHEN n.status IN ('PUBLISHER_HOOK_SENT' ,'ADVERTISER_HOOK_RECEIVED') "
              + "THEN 1 ELSE 0 END) AS conversions, "
              + "SUM(CASE WHEN n.status = 'PUBLISHER_HOOK_RECEIVED' THEN 1 ELSE 0 END) AS clicks, "
              + "SUM(CASE WHEN n.status = 'UNSUBSCRIBED' AND n.duration < (72*60*60) THEN 1 ELSE 0 END) AS churn, "
              + "c.budget, "
              + "c.country as campaignCountry, "
              + "c.cost_per_user AS costPerUser, "
              + "COALESCE(SUM(CASE WHEN n.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED') THEN n.vp_revenue ELSE 0 END), 0) AS totalCost "
              + "FROM hook_notifications_archive_partitioned n "
              + "INNER JOIN campaigns c ON n.campaign_id = c.campaign_id "
              + "INNER JOIN advertisers a ON c.advertiser_id = a.id "
              + "WHERE n.created_date BETWEEN :startDate AND :endDate AND a.business_name = :advertiserName "
              + "GROUP BY c.status, a.business_name, c.name, c.budget, c.cost_per_user, c.country",
      nativeQuery = true)
  List<Object[]> getAdvertiserConversionsForAdminArchiveWithAdvertiser(
      @Param("advertiserName") String advertiserName,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  //  @Query(
  //      value =
  //          "SELECT "
  //              + "p.name AS publisherName, "
  //              + "c.name AS campaignName, "
  //              + "SUM(CASE "
  //              + "WHEN n.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED') THEN 1
  // ELSE 0 END) AS conversions, "
  //              + "SUM(CASE WHEN n.status = 'PUBLISHER_HOOK_RECEIVED' THEN 1 ELSE 0 END) AS
  // clicks, "
  //              + "SUM(CASE WHEN n.duration < (8*24*60*60) THEN 1 ELSE 0 END) AS churn, "
  //              + "pc.publisher_cpa AS cpaCostPerUser, "
  //              + "c.country as campaignCountry, "
  //              + "COALESCE(SUM(n.cpa_revenue), 0) AS totalCost "
  //              + "FROM  hook_notifications_archive_partitioned  n "
  //              + "INNER JOIN campaigns c ON n.campaign_id = c.campaign_id "
  //              + "INNER JOIN publishers p ON n.publisher_id = p.pub_id "
  //              + "INNER JOIN publisher_campaign pc ON p.id = pc.publisher_id AND c.id =
  // pc.campaign_id "
  //              + "WHERE n.created_date BETWEEN :startDate AND :endDate AND (:publisherName IS
  // NULL OR p.name = :publisherName) "
  //              + "GROUP BY p.name, c.name, c.cpa_cost_per_user, c.country, pc.publisher_cpa",
  //      nativeQuery = true)
  //  List<Object[]> getPublisherConversionsForAdminWithPublisher(
  //      @Param("publisherName") String publisherName,
  //      @Param("startDate") Instant startDate,
  //      @Param("endDate") Instant endDate);

  //  @Query(
  //      value =
  //          "SELECT "
  //              + "p.name AS publisherName, "
  //              + "c.name AS campaignName, "
  //              + "SUM(CASE "
  //              + "WHEN n.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED') THEN 1
  // ELSE 0 END) AS conversions, "
  //              + "SUM(CASE WHEN n.status = 'PUBLISHER_HOOK_RECEIVED' THEN 1 ELSE 0 END) AS
  // clicks, "
  //              + "SUM(CASE WHEN n.duration < (8*24*60*60) THEN 1 ELSE 0 END) AS churn, "
  //              + "pc.publisher_cpa AS cpaCostPerUser, "
  //              + "c.country as campaignCountry, "
  //              + "COALESCE(SUM(n.cpa_revenue), 0) AS totalCost "
  //              + "FROM  hook_notifications_archive_partitioned  n "
  //              + "INNER JOIN campaigns c ON n.campaign_id = c.campaign_id "
  //              + "INNER JOIN publishers p ON n.publisher_id = p.pub_id "
  //              + "INNER JOIN publisher_campaign pc ON p.id = pc.publisher_id AND c.id =
  // pc.campaign_id "
  //              + "WHERE n.created_date BETWEEN :startDate AND :endDate "
  //              + "GROUP BY p.name, c.name, c.cpa_cost_per_user, c.country, pc.publisher_cpa",
  //      nativeQuery = true)
  //  List<Object[]> getPublisherConversionsForAdmin(
  //      @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

  @Query(
      value =
          "SELECT "
              + "p.name AS publisherName, "
              + "c.name AS campaignName, "
              + "SUM(CASE "
              + "WHEN n.status = 'PUBLISHER_HOOK_SENT' THEN 1 ELSE 0 END) AS conversions, "
              + "SUM(CASE WHEN n.status  = 'PUBLISHER_HOOK_RECEIVED' THEN 1 ELSE 0 END) AS clicks, "
              + "SUM(CASE WHEN n.duration < (72*60*60) THEN 1 ELSE 0 END) AS churn, "
              + "pc.publisher_cpa AS cpaCostPerUser, "
              + "c.country as campaignCountry, "
              + "COALESCE(SUM(CASE WHEN n.status = 'PUBLISHER_HOOK_SENT' THEN n.cpa_revenue ELSE 0 END), 0) AS totalCost "
              + "FROM  hook_notifications_archive_partitioned  n "
              + "INNER JOIN campaigns c ON n.campaign_id = c.campaign_id "
              + "INNER JOIN publishers p ON n.publisher_id = p.pub_id "
              + "INNER JOIN publisher_campaign pc ON p.id = pc.publisher_id AND c.id = pc.campaign_id "
              + "WHERE n.created_date BETWEEN :startDate AND :endDate "
              + "GROUP BY p.name, c.name, c.cpa_cost_per_user, c.country, pc.publisher_cpa",
      nativeQuery = true)
  List<Object[]> getPublisherConversionsForAdminArchive(
      @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

  @Query(
      value =
          "SELECT "
              + "p.name AS publisherName, "
              + "c.name AS campaignName, "
              + "SUM(CASE "
              + "WHEN n.status = 'PUBLISHER_HOOK_SENT' THEN 1 ELSE 0 END) AS conversions, "
              + "SUM(CASE WHEN n.status = 'PUBLISHER_HOOK_RECEIVED' THEN 1 ELSE 0 END) AS clicks, "
              + "SUM(CASE WHEN n.duration < (72*60*60) THEN 1 ELSE 0 END) AS churn, "
              + "pc.publisher_cpa AS cpaCostPerUser, "
              + "c.country as campaignCountry, "
              + "COALESCE(SUM(CASE WHEN n.status = 'PUBLISHER_HOOK_SENT' THEN n.cpa_revenue ELSE 0 END), 0) AS totalCost "
              + "FROM  hook_notifications_archive_partitioned  n "
              + "INNER JOIN campaigns c ON n.campaign_id = c.campaign_id "
              + "INNER JOIN publishers p ON n.publisher_id = p.pub_id "
              + "INNER JOIN publisher_campaign pc ON p.id = pc.publisher_id AND c.id = pc.campaign_id "
              + "WHERE n.created_date BETWEEN :startDate AND :endDate AND p.name = :publisherName "
              + "GROUP BY p.name, c.name, c.cpa_cost_per_user, c.country, pc.publisher_cpa",
      nativeQuery = true)
  List<Object[]> getPublisherConversionsForAdminArchiveWithPublisher(
      @Param("publisherName") String advertiserName,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query("SELECT n FROM Notification n WHERE n.status = 'ADVERTISER_HOOK_RECEIVED'")
  List<Notification> findUnsentConversions(Pageable pageable);

  //  @Query(
  //      value =
  //          "SELECT "
  //              + "p.name AS publisherName, "
  //              + "SUM(CASE WHEN n.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED')
  // THEN 1 ELSE 0 END) AS acquisition, "
  //              + "SUM(CASE WHEN n.status = 'PUBLISHER_HOOK_RECEIVED' THEN 1 ELSE 0 END) AS
  // clicks, "
  //              + "SUM(CASE WHEN n.duration < (8*24*60*60) THEN 1 ELSE 0 END) AS churn, "
  //              + "c.cpa_cost_per_user AS cpaCostPerUser, "
  //              + " c.country as campaignCountry "
  //              + "FROM hook_notifications_archive_partitioned n "
  //              + "INNER JOIN campaigns c ON n.campaign_id = c.campaign_id "
  //              + "INNER JOIN publishers p ON n.publisher_id = p.pub_id "
  //              + "WHERE n.created_date BETWEEN :startDate AND :endDate "
  //              + "GROUP BY p.name, c.cpa_cost_per_user, c.country",
  //      nativeQuery = true)
  //  List<Object[]> getPublishersConversionsForAdmin(Instant startDate, Instant endDate);

  //  @Query(
  //      value =
  //          "SELECT "
  //              + "p.name AS publisherName, "
  //              + "SUM(CASE WHEN n.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED')
  // THEN 1 ELSE 0 END) AS conversion, "
  //              + "SUM(CASE WHEN n.status = 'PUBLISHER_HOOK_RECEIVED' THEN 1 ELSE 0 END) AS
  // clicks, "
  //              + "SUM(CASE WHEN n.duration < (8*24*60*60) THEN 1 ELSE 0 END) AS churn, "
  //              + "c.cpa_cost_per_user AS cpaCostPerUser, "
  //              + "c.country as campaignCountry "
  //              + "FROM hook_notifications_archive_partitioned n "
  //              + "INNER JOIN campaigns c ON n.campaign_id = c.campaign_id "
  //              + "INNER JOIN publishers p ON n.publisher_id = p.pub_id "
  //              + "WHERE n.created_date BETWEEN :startDate AND :endDate "
  //              + "AND p.name = :publisherName "
  //              + "GROUP BY p.name, c.cpa_cost_per_user, c.country",
  //      nativeQuery = true)
  //  List<Object[]> getPublishersConversionsForAdminWithPublisherName(
  //      String publisherName, Instant startDate, Instant endDate);

  @Query(
      value =
          "SELECT "
              + "p.name AS publisherName, "
              + "SUM(CASE WHEN n.status  = 'PUBLISHER_HOOK_SENT' THEN 1 ELSE 0 END) AS conversion, "
              + "SUM(CASE WHEN n.status = 'PUBLISHER_HOOK_RECEIVED' THEN 1 ELSE 0 END) AS clicks, "
              + "SUM(CASE WHEN n.duration < (72*60*60) THEN 1 ELSE 0 END) AS churn, "
              + "pc.publisher_cpa AS cpaCostPerUser, "
              + " c.country as campaignCountry "
              + "FROM hook_notifications_archive_partitioned n "
              + "INNER JOIN campaigns c ON n.campaign_id = c.campaign_id "
              + "INNER JOIN publishers p ON n.publisher_id = p.pub_id "
              + "INNER JOIN publisher_campaign pc ON p.id = pc.publisher_id AND c.id = pc.campaign_id "
              + "WHERE n.created_date BETWEEN :startDate AND :endDate "
              + "GROUP BY p.name, c.cpa_cost_per_user, c.country, pc.publisher_cpa",
      nativeQuery = true)
  List<Object[]> getPublishersConversionsForAdminArchive(Instant startDate, Instant endDate);

  @Query(
      value =
          "SELECT "
              + "p.name AS publisherName, "
              + "SUM(CASE WHEN n.status = 'PUBLISHER_HOOK_SENT' THEN 1 ELSE 0 END) AS conversion, "
              + "SUM(CASE WHEN n.status = 'PUBLISHER_HOOK_RECEIVED' THEN 1 ELSE 0 END) AS clicks, "
              + "SUM(CASE WHEN n.duration < (72*60*60) THEN 1 ELSE 0 END) AS churn, "
              + "pc.publisher_cpa AS cpaCostPerUser, "
              + " c.country as campaignCountry "
              + "FROM hook_notifications_archive_partitioned n "
              + "INNER JOIN campaigns c ON n.campaign_id = c.campaign_id "
              + "INNER JOIN publishers p ON n.publisher_id = p.pub_id "
              + "INNER JOIN publisher_campaign pc ON p.id = pc.publisher_id AND c.id = pc.campaign_id "
              + "WHERE n.created_date BETWEEN :startDate AND :endDate "
              + "AND p.name = :publisherName "
              + "GROUP BY p.name, c.cpa_cost_per_user, c.country, pc.publisher_cpa",
      nativeQuery = true)
  List<Object[]> getPublishersConversionsForAdminArchiveWithPublisherName(
      String publisherName, Instant startDate, Instant endDate);

  @Query(
      "SELECT new com.nitax.valueplusbackend.dto.response.SearchPostbackDto("
          + "c.name, "
          + "n.msisdn, "
          + "n.transactionId, "
          + "p.name, "
          + "n.status,"
          + "n.createdDate) "
          + "FROM Notification n "
          + "INNER JOIN Campaign c ON n.campaignId = c.campaignId "
          + "INNER JOIN Publisher p ON n.publisherId = p.pubId "
          + "WHERE :transactionId IS NULL OR  n.transactionId = :transactionId")
  List<SearchPostbackDto> searchByTransactionId(
      @Param("transactionId") String transactionId, Pageable pageable);

  @Query(
      value =
          "SELECT "
              + "c.status, "
              + "a.business_name AS businessName, "
              + "c.name, "
              + "SUM(CASE "
              + "WHEN n.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED') "
              + "THEN 1 ELSE 0 END) AS nonPublisherHookCount, "
              + "SUM(CASE WHEN n.status = 'PUBLISHER_HOOK_RECEIVED' THEN 1 ELSE 0 END) AS conversions, "
              + "SUM(CASE WHEN n.status = 'UNSUBSCRIBED' AND n.duration < (72*60*60) THEN 1 ELSE 0 END) AS churn, "
              + "c.budget, "
              + "c.country as campaignCountry, "
              + "c.cost_per_user AS costPerUser, "
              + "SUM(COALESCE(n.vp_revenue, 0.0)) AS revenue "
              + "FROM (SELECT * FROM hook_notifications_archive_partitioned WHERE created_date BETWEEN :startDate AND :endDate) n "
              + "INNER JOIN campaigns c ON n.campaign_id = c.campaign_id "
              + "INNER JOIN advertisers a ON c.advertiser_id = a.id "
              + "WHERE (:advertiserName IS NULL OR a.business_name = :advertiserName) "
              + "AND (:campaignName IS NULL OR c.name LIKE CONCAT('%', cast(:campaignName as text), '%')) "
              + "GROUP BY c.status, a.business_name, c.name, c.budget, c.cost_per_user, c.country",
      nativeQuery = true)
  List<Object[]> getAdvertiserConversionsForAdvertiser(
      String advertiserName, String campaignName, Instant startDate, Instant endDate);

  @Query(
      "SELECT COALESCE(SUM(n.cpaRevenue), 0) FROM Notification n "
          + "WHERE n.campaignId = :campaignId "
          + "AND n.createdDate BETWEEN :startDate AND :endDate")
  Double getCpaCostForCampaign(
      @Param("campaignId") String campaignId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query(
      value =
          """
            SELECT
              COALESCE(SUM(CASE WHEN n.status IN ('PUBLISHER_HOOK_SENT','ADVERTISER_HOOK_RECEIVED') THEN 1 ELSE 0 END), 0),
              COALESCE(SUM(n.vp_revenue), 0.0),
              COALESCE(SUM(CASE WHEN n.status = 'PUBLISHER_HOOK_RECEIVED' THEN 1 ELSE 0 END), 0),
              COALESCE(SUM(n.cpa_revenue), 0.0)
            FROM hook_notifications_archive_partitioned n
            WHERE n.campaign_id = :campaignId
              AND n.created_date BETWEEN :startDate AND :endDate
            """,
      nativeQuery = true)
  List<Object[]> getCampaignMetrics(
      @Param("campaignId") String campaignId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query(
      value =
          "SELECT * FROM hook_notifications_archive_partitioned n "
              + "WHERE n.publisher_id = :pubId "
              + "AND n.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED') "
              + "ORDER BY n.created_date DESC LIMIT 1",
      nativeQuery = true)
  Notification getLastConversionForPublisher(String pubId);

  @Modifying
  @Query(
      value =
          "DELETE FROM hook_notifications_archive_partitioned n WHERE n.created_date < :cutoffDate",
      nativeQuery = true)
  //  @Lock(LockModeType.NONE)
  void deleteBefore(Instant cutoffDate);

  @Query(
      "SELECT new com.nitax.valueplusbackend.dto.NotificationDto("
          + "n.createdDate, "
          + "n.status, "
          + "(CAST(REGEXP_REPLACE(n.transactionId, '^^[^_]+_[^_]+_', '') AS string)), "
          + "n.sourceId, "
          + "n.duration, "
          + "c.name, "
          + "n.cpaRevenue,"
          + "n.vpRevenue)"
          + "FROM Notification n "
          + "INNER JOIN Campaign c ON n.campaignId = c.campaignId "
          + "WHERE n.publisherId = :pubId AND n.campaignId IN ('vtJLLI7ukY', 'TbHLVKWKny', '9yyDUlsXTB', 'vly9DZ1Qv0') "
          + "AND n.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED') "
          + "AND n.createdDate >= :startDate  AND n.createdDate <= :endDate "
          + "ORDER BY n.createdDate DESC")
  List<NotificationDto> findNotificationsForPublisherWithDateRange(
      Instant startDate, Instant endDate, String pubId);

  @Query(
      "SELECT new com.nitax.valueplusbackend.dto.RetentionReportDto("
          + "p.name, "
          + "n.sourceId, "
          + "SUM(CASE WHEN n.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED') THEN 1 ELSE 0 END), "
          + "SUM(CASE WHEN n.status = 'UNSUBSCRIBED' AND n.duration < (72*60*60) THEN 1 ELSE 0 END), "
          + "c.name) "
          + "FROM Notification n "
          + "JOIN Campaign c ON n.campaignId = c.campaignId "
          + "JOIN Publisher  p ON p.pubId = n.publisherId "
          + "WHERE n.publisherId = :pubId AND n.campaignId IN ('vtJLLI7ukY', 'TbHLVKWKny', '9yyDUlsXTB', 'vly9DZ1Qv0') "
          + "AND n.createdDate BETWEEN :startDate AND :endDate "
          + "GROUP BY p.name, n.sourceId, c.name "
          + "ORDER BY c.name DESC")
  List<RetentionReportDto> findPubSourceRetentionCount(
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate,
      @Param("pubId") String pubId);

  @Query(
      "SELECT new com.nitax.valueplusbackend.dto.PubChurnReportDto("
          + "p.name, "
          + "c.name, "
          + "SUM(CASE WHEN n.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED') "
          + "         THEN 1 ELSE 0 END), "
          + "SUM(CASE WHEN n.status = 'UNSUBSCRIBED' AND n.duration < (72*60*60) THEN 1 ELSE 0 END)) "
          + "FROM Notification n "
          + "JOIN Campaign c ON n.campaignId = c.campaignId "
          + "JOIN Publisher p ON p.pubId = n.publisherId "
          + "WHERE n.createdDate BETWEEN :startDate AND :endDate AND n.campaignId IN ('vtJLLI7ukY', 'TbHLVKWKny', '9yyDUlsXTB', 'vly9DZ1Qv0') "
          + "GROUP BY p.name, c.name "
          + "ORDER BY  c.name ASC, p.name ASC")
  List<PubChurnReportDto> getPubChurnReportPerCampaign(
      @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

  @Query("SELECT n FROM Notification n WHERE n.msisdn = :msisdn ORDER BY n.createdDate DESC")
  List<Notification> findTopByMsisdn(@Param("msisdn") String subMsisdn);

  @Query(
      value =
          "SELECT * FROM hook_notifications_archive_partitioned n WHERE n.msisdn = :msisdn ORDER BY n.created_date DESC",
      nativeQuery = true)
  List<Notification> findTopByMsisdnFromArchive(@Param("msisdn") String msisdn);

  // 1. hook_notifications with sourceId: works with discrepancies
  @Query(
      value =
          """
            SELECT DATE(n.created_date) AS reportDate,
                   p.name AS publisherName,
                   c.name AS campaignName,
                   n.source_id AS sourceId,
                   SUM(CASE
                       WHEN n.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED')
                       AND (n.message = 'Success' OR n.message = 'publisher callback sent')
                       THEN 1 ELSE 0
                   END) AS acquisition,
                   SUM(CASE
                       WHEN n.status = 'UNSUBSCRIBED'
                       AND (n.duration / 60) < :duration
                       THEN 1 ELSE 0
                   END) AS churned
            FROM hook_notifications_archive_partitioned n
            JOIN campaigns c ON c.campaign_id = n.campaign_id
            JOIN publishers p ON p.pub_id = n.publisher_id
            WHERE (n.campaign_id IN :campaigns)
                    AND (n.publisher_id IN :publishers)
                AND
                    n.created_date BETWEEN
                        :startDate
                         AND
                         :endDate
            GROUP BY p.name, c.name, DATE(n.created_date), n.source_id
            ORDER BY reportDate, acquisition, churned ASC
            """,
      nativeQuery = true)
  List<Object[]> findWithSourceIdFromCurrent(
      @Param("campaigns") List<String> campaigns,
      @Param("publishers") List<String> publishers,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate,
      @Param("duration") int duration);

  // Admin churn report: for each acquisition day, how many converted and how many of those churned
  // within 48h.
  // Uses conditional aggregation (single table scan) to avoid expensive triple self-joins on the
  // partitioned table.
  @Query(
      value =
          """
            SELECT DATE(n.created_date)                                                          AS acquisitionDay,
                   n.publisher_id,
                   p.name                                                                        AS publisherName,
                   n.source_id,
                   c.name                                                                        AS campaignName,
                   COUNT(CASE WHEN n.status = 'PUBLISHER_HOOK_SENT' THEN 1 END)                 AS totalAcquired,
                   COUNT(CASE WHEN n.status = 'UNSUBSCRIBED' AND n.duration < (72*60*60) THEN 1 END) AS totalChurned,
                   COUNT(CASE WHEN n.status = 'ADVERTISER_HOOK_RECEIVED' THEN 1 END)            AS totalAdvertiserHook,
                   COALESCE(SUM(CASE WHEN n.status = 'PUBLISHER_HOOK_SENT' THEN n.cpa_revenue ELSE 0 END), 0) AS amountSpent,
                   COALESCE(SUM(CASE WHEN n.status = 'UNSUBSCRIBED' AND n.duration < (72*60*60) THEN n.cpa_revenue ELSE 0 END), 0) AS churnedAmount
            FROM hook_notifications_archive_partitioned n
            JOIN publishers p ON p.pub_id = n.publisher_id
            JOIN campaigns c ON c.campaign_id = n.campaign_id
            WHERE n.status IN ('PUBLISHER_HOOK_SENT', 'UNSUBSCRIBED', 'ADVERTISER_HOOK_RECEIVED')
              AND n.created_date BETWEEN :startDate AND :endDate
            GROUP BY DATE(n.created_date), n.publisher_id, p.name, n.source_id, c.name
            ORDER BY acquisitionDay ASC, p.name ASC
            """,
      nativeQuery = true)
  List<Object[]> findAdminChurnReport(
      @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

  // Same report but with a caller-supplied churn window (in hours) instead of the hardcoded 48h
  @Query(
      value =
          """
            SELECT DATE(n.created_date)                                                                               AS acquisitionDay,
                   n.publisher_id,
                   p.name                                                                                             AS publisherName,
                   n.source_id,
                   c.name                                                                                             AS campaignName,
                   COUNT(CASE WHEN n.status = 'PUBLISHER_HOOK_SENT' THEN 1 END)                                      AS totalAcquired,
                   COUNT(CASE WHEN n.status = 'UNSUBSCRIBED' AND n.duration < (:churnDurationHours * 3600) THEN 1 END) AS totalChurned,
                   COUNT(CASE WHEN n.status = 'ADVERTISER_HOOK_RECEIVED' THEN 1 END)                                 AS totalAdvertiserHook,
                   COALESCE(SUM(CASE WHEN n.status = 'PUBLISHER_HOOK_SENT' THEN n.cpa_revenue ELSE 0 END), 0)         AS amountSpent,
                   COALESCE(SUM(CASE WHEN n.status = 'UNSUBSCRIBED' AND n.duration < (:churnDurationHours * 3600) THEN n.cpa_revenue ELSE 0 END), 0) AS churnedAmount
            FROM hook_notifications_archive_partitioned n
            JOIN publishers p ON p.pub_id = n.publisher_id
            JOIN campaigns c ON c.campaign_id = n.campaign_id
            WHERE n.status IN ('PUBLISHER_HOOK_SENT', 'UNSUBSCRIBED', 'ADVERTISER_HOOK_RECEIVED')
              AND n.created_date BETWEEN :startDate AND :endDate
            GROUP BY DATE(n.created_date), n.publisher_id, p.name, n.source_id, c.name
            ORDER BY acquisitionDay ASC, p.name ASC
            """,
      nativeQuery = true)
  List<Object[]> findAdminChurnReportWithDuration(
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate,
      @Param("churnDurationHours") int churnDurationHours);

  // Publisher API: individual conversion rows (PUBLISHER_HOOK_SENT)
  @Query(
      value =
          """
            SELECT DATE(n.created_date) AS reportDate,
                   c.name AS campaignName,
                   n.transaction_id AS clickId,
                   n.source_id AS sourceId
            FROM hook_notifications_archive_partitioned n
            JOIN campaigns c ON c.campaign_id = n.campaign_id
            WHERE n.publisher_id = :publisherId
                AND n.status = 'PUBLISHER_HOOK_SENT'
                AND n.created_date BETWEEN :startDate AND :endDate
            ORDER BY reportDate ASC
            """,
      nativeQuery = true)
  List<Object[]> findPublisherConversions(
      @Param("publisherId") String publisherId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  // Publisher API: individual churned subscriber rows, 72-hour churn window
  @Query(
      value =
          """
            SELECT DATE(n.created_date) AS reportDate,
                   c.name AS campaignName,
                   n.transaction_id AS clickId,
                   n.source_id AS sourceId
            FROM hook_notifications_archive_partitioned n
            JOIN campaigns c ON c.campaign_id = n.campaign_id
            WHERE n.publisher_id = :publisherId
                AND n.status = 'UNSUBSCRIBED'
                AND n.duration < (72 * 60 * 60)
                AND n.created_date BETWEEN :startDate AND :endDate
            ORDER BY reportDate ASC
            """,
      nativeQuery = true)
  List<Object[]> findPublisherChurnReport(
      @Param("publisherId") String publisherId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  // Publisher API: individual churned subscriber rows, 48-hour churn window
  @Query(
      value =
          """
            SELECT DATE(n.created_date) AS reportDate,
                   c.name AS campaignName,
                   n.transaction_id AS clickId,
                   n.source_id AS sourceId
            FROM hook_notifications_archive_partitioned n
            JOIN campaigns c ON c.campaign_id = n.campaign_id
            WHERE n.publisher_id = :publisherId
                AND n.status = 'UNSUBSCRIBED'
                AND n.duration < (72 * 60 * 60)
                AND n.created_date BETWEEN :startDate AND :endDate
            ORDER BY reportDate ASC
            """,
      nativeQuery = true)
  List<Object[]> findPublisherChurnReport48hrs(
      @Param("publisherId") String publisherId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  // 2. hook_notifications without sourceId :  Works with descrepancies
  @Query(
      value =
          """
            SELECT DATE(n.created_date) AS reportDate,
                   p.name AS publisherName,
                    c.name AS campaignName,
                   SUM(CASE
                       WHEN n.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED')
                       AND (n.message = 'Success' OR n.message = 'publisher callback sent')
                       THEN 1 ELSE 0
                   END) AS acquisition,
                   SUM(CASE
                       WHEN n.status = 'UNSUBSCRIBED'
                       AND (n.duration / 60) < :duration
                       THEN 1 ELSE 0
                   END) AS churned
            FROM hook_notifications_archive_partitioned n
            JOIN campaigns c ON c.campaign_id = n.campaign_id
            JOIN publishers p ON p.pub_id = n.publisher_id
            WHERE (n.campaign_id IN :campaigns)
                    AND (n.publisher_id IN :publishers)
                AND
                    n.created_date BETWEEN
                         :startDate
                         AND
                         :endDate
            GROUP BY p.name, c.name, DATE(n.created_date)
            ORDER BY reportDate, acquisition, churned ASC
            """,
      nativeQuery = true)
  List<Object[]> findWithoutSourceIdFromCurrent(
      @Param("campaigns") List<String> campaigns,
      @Param("publishers") List<String> publishers,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate,
      @Param("duration") int duration);

  // 2. hook_notifications without sourceId :  Works with descrepancies
  @Query(
      value =
          """
                    SELECT DATE(n.created_date) AS reportDate,
                            c.name AS campaignName,
                           SUM(CASE
                               WHEN n.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED')
                               AND (n.message = 'Success' OR n.message = 'publisher callback sent')
                               THEN 1 ELSE 0
                           END) AS acquisition,
                           SUM(CASE
                               WHEN n.status = 'UNSUBSCRIBED'
                               AND (n.duration / 60) < :duration
                               THEN 1 ELSE 0
                           END) AS churned
                          FROM hook_notifications_archive_partitioned n
                           JOIN campaigns c ON c.campaign_id = n.campaign_id
                           JOIN advertisers a ON a.id = c.advertiser_id
                           WHERE a.advertiser_id = :advertiserId
                           AND n.campaign_id IN (:campaigns)
                           AND n.created_date BETWEEN :startDate
                           AND :endDate
                           GROUP BY c.name, DATE(n.created_date)
                           ORDER BY reportDate, acquisition, churned ASC;
                    """,
      nativeQuery = true)
  List<Object[]> findAdvertiserChurn(
      @Param("campaigns") List<String> campaigns,
      @Param("advertiserId") String advertiserId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate,
      @Param("duration") int duration);

  @Query(
      value =
"""
    SELECT
        DATE(n.created_date) AS reportDate,
        c.name AS campaignName,
        SUM(CASE
                WHEN n.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED')
                     AND (n.message = 'Success' OR n.message = 'publisher callback sent')
                THEN 1 ELSE 0
            END) AS acquisition,
        SUM(CASE
                WHEN n.status = 'UNSUBSCRIBED'
                     AND (n.duration / 60) < :duration
                THEN 1 ELSE 0
            END) AS churned
    FROM hook_notifications_archive_partitioned n
    JOIN publisher_campaign pc ON pc.campaign_id = n.campaign_id
    JOIN publishers p ON p.id = pc.publisher_id
    JOIN campaigns c ON c.campaign_id = n.campaign_id
    WHERE p.pub_id = :publisherId
      AND n.campaign_id IN (:campaigns)
      AND n.created_date BETWEEN :startDate AND :endDate
    GROUP BY c.name, DATE(n.created_date)
    ORDER BY reportDate ASC, acquisition ASC, churned ASC
""",
      nativeQuery = true)
  List<Object[]> findPublisherChurn(
      @Param("campaigns") List<String> campaigns,
      @Param("publisherId") String publisherId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate,
      @Param("duration") int duration);

  //  @Query(
  //          value = """
  //            WITH campaign_notifications AS (
  //                SELECT
  //                    n.campaign_id,
  //                    c.name AS campaign_name,
  //                    c.country AS campaign_country,
  //                    c.daily_cap AS campaign_cap,
  //                    n.source_id,
  //                    SUM(CASE WHEN n.status = 'PUBLISHER_HOOK_RECEIVED' THEN 1 ELSE 0 END) AS
  // total_clicks,
  //                    SUM(CASE WHEN n.status = 'PUBLISHER_HOOK_SENT' THEN 1 ELSE 0 END) AS
  // total_conversions,
  //                    SUM(CASE WHEN n.status = 'UNSUBSCRIBED' AND (n.duration / 60) < :churnPeriod
  // THEN 1 ELSE 0 END) AS churn_count,
  //                    SUM(n.duration) AS total_duration
  //                FROM
  //                    hook_notifications_archive_partitioned n
  //                JOIN
  //                    campaigns c ON n.campaign_id = c.campaign_id
  //                WHERE
  //                    n.publisher_id = :publisherId
  //                    AND n.created_date >= :startDate AND n.created_date < :endDate
  //                GROUP BY
  //                    n.campaign_id, c.name, c.country, c.daily_cap, n.source_id
  //            )
  //            SELECT
  //                campaign_name,
  //                campaign_country,
  //                source_id,
  //                total_clicks,
  //                total_conversions,
  //                churn_count,
  //                CASE
  //                    WHEN total_clicks > 0 THEN (total_conversions * 100.0) / total_clicks
  //                    ELSE 0
  //                END AS cr,
  //                CASE
  //                    WHEN total_clicks > 0 THEN ((campaign_cap * total_conversions * 100.0) /
  // 100) / total_clicks * 100
  //                    ELSE 0
  //                END AS ecpm,
  //                (campaign_cap * total_conversions * 100.0) AS total_amount_spent,
  //                total_duration / 60.0 AS total_churn_hours
  //            FROM
  //                campaign_notifications
  //            ORDER BY
  //                campaign_name, source_id;
  //        """,
  //          nativeQuery = true
  //  )
  //  List<Object[]> getPublisherCampaignMetrics(
  //          @Param("publisherId") String publisherId,
  //          @Param("startDate") Instant startDate,
  //          @Param("endDate") Instant endDate,
  //          @Param("churnPeriod") int churnPeriod
  //  );

  @Query(
      value =
          """
    WITH campaign_notifications AS (
        SELECT
            n.campaign_id,
            c.name AS campaign_name,
            c.country AS campaign_country,
            c.daily_cap AS campaign_cap,
            n.source_id,
            SUM(CASE WHEN n.status = 'PUBLISHER_HOOK_RECEIVED' THEN 1 ELSE 0 END) AS total_clicks,
            SUM(CASE WHEN n.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED') THEN 1 ELSE 0 END) AS total_conversions,
            SUM(CASE WHEN n.status = 'UNSUBSCRIBED' AND n.duration < (72*60*60) THEN 1 ELSE 0 END) AS churn_count,
            SUM(n.duration) AS total_duration
        FROM
            hook_notifications_archive_partitioned n
        JOIN
            campaigns c ON n.campaign_id = c.campaign_id
        WHERE
            n.publisher_id = :publisherId
            AND n.created_date BETWEEN :startDate AND :endDate
        GROUP BY
            n.campaign_id, c.name, c.country, c.daily_cap, n.source_id
    )
    SELECT
        campaign_name,
        campaign_country,
        source_id,
        total_clicks,
        total_conversions,
        churn_count,
        CASE
            WHEN total_clicks > 0 THEN (total_conversions * 100.0) / total_clicks
            ELSE 0
        END AS cr,
        CASE
            WHEN total_clicks > 0 THEN ((campaign_cap * total_conversions * 100.0) / 100) / total_clicks * 100
            ELSE 0
        END AS ecpm,
        (campaign_cap * total_conversions * 100.0) AS total_amount_spent,
        total_duration / 60.0 AS total_churn_hours
    FROM
        campaign_notifications
    ORDER BY
        campaign_name, source_id
    """,
      nativeQuery = true)
  List<Object[]> getPublisherCampaignMetricsWithSourceId(
      @Param("publisherId") String publisherId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query(
      value =
          """
    WITH campaign_notifications AS (
        SELECT
            n.campaign_id,
            c.name AS campaign_name,
            c.country AS campaign_country,
            c.daily_cap AS campaign_cap,
            SUM(CASE WHEN n.status = 'PUBLISHER_HOOK_RECEIVED' THEN 1 ELSE 0 END) AS total_clicks,
            SUM(CASE WHEN n.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED') THEN 1 ELSE 0 END) AS total_conversions,
            SUM(CASE WHEN n.status = 'UNSUBSCRIBED' AND n.duration < (72*60*60) THEN 1 ELSE 0 END) AS churn_count,
            SUM(n.duration) AS total_duration
        FROM
            hook_notifications_archive_partitioned n
        JOIN
            campaigns c ON n.campaign_id = c.campaign_id
        WHERE
            n.publisher_id = :publisherId
            AND n.created_date BETWEEN :startDate AND :endDate
        GROUP BY
            n.campaign_id, c.name, c.country, c.daily_cap
    )
    SELECT
        campaign_name,
        campaign_country,
        NULL AS source_id,
        total_clicks,
        total_conversions,
        churn_count,
        CASE
            WHEN total_clicks > 0 THEN (total_conversions * 100.0) / total_clicks
            ELSE 0
        END AS cr,
        CASE
            WHEN total_clicks > 0 THEN ((campaign_cap * total_conversions * 100.0) / 100) / total_clicks * 100
            ELSE 0
        END AS ecpm,
        (campaign_cap * total_conversions * 100.0) AS total_amount_spent,
        total_duration / 60.0 AS total_churn_hours
    FROM
        campaign_notifications
    ORDER BY
        campaign_name;
    """,
      nativeQuery = true)
  List<Object[]> getPublisherCampaignMetricsWithoutSourceId(
      @Param("publisherId") String publisherId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  /**
   * Get advertiser conversions grouped by CPA rate. This allows tracking when CPA changes over time
   * for accurate amount calculations.
   */
  @Query(
      value =
          """
          SELECT
              a.business_name AS advertiserName,
              c.name AS campaignName,
              c.campaign_id AS campaignId,
              c.status AS status,
              c.country AS country,
              c.budget AS budget,
              COUNT(CASE WHEN n.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED') THEN 1 END) AS conversions,
              COUNT(CASE WHEN n.status = 'PUBLISHER_HOOK_RECEIVED' THEN 1 END) AS clicks,
              COUNT(CASE WHEN n.status = 'UNSUBSCRIBED' AND n.duration < (72*60*60) THEN 1 END) AS churn,
              COALESCE(SUM(CASE WHEN n.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED') THEN n.vp_revenue ELSE 0 END), 0) AS amountSpent
          FROM hook_notifications_archive_partitioned n
          INNER JOIN campaigns c ON n.campaign_id = c.campaign_id
          INNER JOIN advertisers a ON c.advertiser_id = a.id
          WHERE n.created_date BETWEEN :startDate AND :endDate
            AND c.status = 'ACTIVE'
          GROUP BY a.business_name, c.name, c.campaign_id, c.status, c.country, c.budget
          ORDER BY a.business_name, c.name
          """,
      nativeQuery = true)
  List<Object[]> getAdvertiserConversionsCpaBreakdown(
      @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

  /** Get advertiser conversions grouped by CPA rate for a specific advertiser. */
  @Query(
      value =
          """
          SELECT
              a.business_name AS advertiserName,
              c.name AS campaignName,
              c.campaign_id AS campaignId,
              c.status AS status,
              c.country AS country,
              c.budget AS budget,
              COUNT(CASE WHEN n.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED') THEN 1 END) AS conversions,
              COUNT(CASE WHEN n.status = 'PUBLISHER_HOOK_RECEIVED' THEN 1 END) AS clicks,
              COUNT(CASE WHEN n.status = 'UNSUBSCRIBED' AND n.duration < (72*60*60) THEN 1 END) AS churn,
              COALESCE(SUM(CASE WHEN n.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED') THEN n.vp_revenue ELSE 0 END), 0) AS amountSpent
          FROM hook_notifications_archive_partitioned n
          INNER JOIN campaigns c ON n.campaign_id = c.campaign_id
          INNER JOIN advertisers a ON c.advertiser_id = a.id
          WHERE n.created_date BETWEEN :startDate AND :endDate
            AND a.business_name = :advertiserName
            AND c.status = 'ACTIVE'
          GROUP BY a.business_name, c.name, c.campaign_id, c.status, c.country, c.budget
          ORDER BY a.business_name, c.name
          """,
      nativeQuery = true)
  List<Object[]> getAdvertiserConversionsCpaBreakdownByAdvertiser(
      @Param("advertiserName") String advertiserName,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);
}
