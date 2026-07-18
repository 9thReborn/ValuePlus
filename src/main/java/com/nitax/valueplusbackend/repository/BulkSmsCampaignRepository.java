package com.nitax.valueplusbackend.repository;

import com.nitax.valueplusbackend.domain.BulkSmsCampaign;
import com.nitax.valueplusbackend.domain.BulkSmsCampaignStatus;
import com.nitax.valueplusbackend.dto.response.BulkSmsDashboardSummaryDto;
import com.nitax.valueplusbackend.dto.response.CampaignDeliveryRate;
import com.nitax.valueplusbackend.dto.response.HourlyDeliveryRate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface BulkSmsCampaignRepository extends JpaRepository<BulkSmsCampaign,Long> {
    boolean existsByBulkSmsCampaignId(String string);
    List<BulkSmsCampaign> findByStatus(BulkSmsCampaignStatus status);
    Page<BulkSmsCampaign> findAllByAdvertiser_Id(@Param("advertiserId") long advertiserId , Pageable pageable);
    @Query("""
    SELECT c FROM BulkSmsCampaign c
      WHERE c.advertiser.id = :advertiserId
     AND c.createdDate BETWEEN :startDate AND :endDate
""")
    Page<BulkSmsCampaign> findAllByAdvertiser_IdAndCreatedDateBetween(
            @Param("advertiserId") Long advertiserId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );

    @Query("""
    SELECT c FROM BulkSmsCampaign c
    WHERE (:name IS NULL OR LOWER(CAST(c.name AS text)) LIKE LOWER(CONCAT('%', CAST(:name AS text), '%')))
      AND c.advertiser.id = :advertiserId
""")
    Page<BulkSmsCampaign> findAllByAdvertiser_IdAndFilterName( @Param("advertiserId") Long advertiserId,@Param("name") String name,
                                                               Pageable pageable);

    @Query("""
    SELECT c FROM BulkSmsCampaign c
      where c.advertiser.id = :advertiserId order by createdDate desc 
""")
    Page<BulkSmsCampaign> findAllByAdvertiser_Id( @Param("advertiserId") Long advertiserId, Pageable pageable);

    @Query(value = """
    SELECT
        (SELECT COUNT(*)
         FROM bulk_sms_campaign
         WHERE created_date BETWEEN TO_TIMESTAMP(:startDate, 'YYYY-MM-DD HH24:MI') AND TO_TIMESTAMP(:endDate, 'YYYY-MM-DD HH24:MI')
        ) AS totalCampaign,
    
        (SELECT COUNT(*)
         FROM bulk_sms_campaign
         WHERE status = 'IN_PROGRESS'
           AND created_date BETWEEN TO_TIMESTAMP(:startDate, 'YYYY-MM-DD HH24:MI') AND TO_TIMESTAMP(:endDate, 'YYYY-MM-DD HH24:MI')
        ) AS currentlySending,
    
        (SELECT COUNT(*)
         FROM bulk_sms_campaign
         WHERE status = 'SCHEDULED'
           AND created_date BETWEEN TO_TIMESTAMP(:startDate, 'YYYY-MM-DD HH24:MI') AND TO_TIMESTAMP(:endDate, 'YYYY-MM-DD HH24:MI')
        ) AS scheduled,
    
        -- The following queries use a JOIN to calculate message stats
        (SELECT COALESCE(COUNT(m.id), 0)
         FROM bulk_sms_message m
         JOIN bulk_sms_campaign c ON m.campaign_id = c.id
         WHERE m.status = 'DELIVERED'
           AND c.created_date BETWEEN TO_TIMESTAMP(:startDate, 'YYYY-MM-DD HH24:MI') AND TO_TIMESTAMP(:endDate, 'YYYY-MM-DD HH24:MI')
        ) AS messagesSent,
    
        (SELECT COALESCE(COUNT(m.id), 0)
         FROM bulk_sms_message m
         JOIN bulk_sms_campaign c ON m.campaign_id = c.id
         WHERE c.created_date BETWEEN TO_TIMESTAMP(:startDate, 'YYYY-MM-DD HH24:MI') AND TO_TIMESTAMP(:endDate, 'YYYY-MM-DD HH24:MI')
        ) AS totalMessages,
    
        -- Calculation for delivery rate
        (SELECT COALESCE(
                        (SUM(CASE WHEN m.status = 'DELIVERED' THEN 1 ELSE 0 END) * 100.0) / NULLIF(COUNT(m.id), 0),
                        0)
         FROM bulk_sms_message m
         JOIN bulk_sms_campaign c ON m.campaign_id = c.id
         WHERE c.created_date BETWEEN TO_TIMESTAMP(:startDate, 'YYYY-MM-DD HH24:MI') AND TO_TIMESTAMP(:endDate, 'YYYY-MM-DD HH24:MI')
        ) AS deliveryRate
    """, nativeQuery = true)
    Map<Object, Object> getAdminCampaignSummary(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);


    @Query(value = """
    SELECT
        (SELECT COUNT(*)
         FROM bulk_sms_campaign
        ) AS totalCampaign,
    
        (SELECT COUNT(*)
         FROM bulk_sms_campaign
         WHERE status = 'IN_PROGRESS'
        ) AS currentlySending,
    
        (SELECT COUNT(*)
         FROM bulk_sms_campaign
         WHERE status = 'SCHEDULED'
        ) AS scheduled,
    
        -- The following queries use a JOIN to calculate message stats
        (SELECT COALESCE(COUNT(m.id), 0)
         FROM bulk_sms_message m
         JOIN bulk_sms_campaign c ON m.campaign_id = c.id
         WHERE m.status = 'DELIVERED'
        ) AS messagesSent,
    
        (SELECT COALESCE(COUNT(m.id), 0)
         FROM bulk_sms_message m
         JOIN bulk_sms_campaign c ON m.campaign_id = c.id
        ) AS totalMessages,
    
        -- Calculation for delivery rate
        (SELECT COALESCE(
                        (SUM(CASE WHEN m.status = 'DELIVERED' THEN 1 ELSE 0 END) * 100.0) / NULLIF(COUNT(m.id), 0),
                        0)
         FROM bulk_sms_message m
         JOIN bulk_sms_campaign c ON m.campaign_id = c.id
        ) AS deliveryRate
    """, nativeQuery = true)
    Map<Object, Object> getAdminCampaignSummary();

    @Query(value = """
SELECT
    w.points_balance AS totalPoints,

    (SELECT COUNT(*)
     FROM bulk_sms_campaign bc
     WHERE bc.advertiser_id = :advertiserId
       AND bc.status = 'COMPLETED') AS completedCampaigns,

    (SELECT COUNT(*)
     FROM bulk_sms_campaign bc
     WHERE bc.advertiser_id = :advertiserId
       AND bc.status = 'SCHEDULED'
       AND bc.scheduled_date > CURRENT_DATE) AS upcomingCampaignCount,

    (SELECT STRING_AGG(bc.name, ', ')
     FROM bulk_sms_campaign bc
     WHERE bc.advertiser_id = :advertiserId
       AND bc.status = 'SCHEDULED'
       AND bc.scheduled_date > CURRENT_DATE) AS upcomingCampaignNames,

    -- totalSmsSentForTheMonth: Count of DELIVERED individual SMS messages for the current month
    (SELECT COUNT(bsm.id)
     FROM bulk_sms_message bsm
     JOIN bulk_sms_campaign bsc ON bsm.campaign_id = bsc.id
     WHERE bsc.advertiser_id = :advertiserId
       AND bsm.status = 'DELIVERED'
       AND EXTRACT(MONTH FROM bsm.created_date) = EXTRACT(MONTH FROM CURRENT_DATE)
       AND EXTRACT(YEAR FROM bsm.created_date) = EXTRACT(YEAR FROM CURRENT_DATE)) AS totalSmsSentForTheMonth,

    (SELECT MAX(bc.created_date)
     FROM bulk_sms_campaign bc
     WHERE bc.advertiser_id = :advertiserId
       AND bc.status = 'COMPLETED') AS lastCampaignDay,

    (SELECT SUM(t.point_deducted)
         FROM transaction t
         WHERE t.advertiser_id = :advertiserId AND t.transaction_type = 'DEBIT'
         AND EXTRACT(MONTH FROM t.created_date) = EXTRACT(MONTH FROM CURRENT_DATE)
        AND EXTRACT(YEAR FROM t.created_date) = EXTRACT(YEAR FROM CURRENT_DATE)) AS pointUsedForTheMonth,

    -- NEW: Fetch current month's delivered SMS count separately
    (SELECT COALESCE(COUNT(bsm.id), 0)
     FROM bulk_sms_message bsm
     JOIN bulk_sms_campaign bsc ON bsm.campaign_id = bsc.id
     WHERE bsc.advertiser_id = :advertiserId
       AND bsm.status = 'DELIVERED'
       AND EXTRACT(MONTH FROM bsm.created_date) = EXTRACT(MONTH FROM CURRENT_DATE)
       AND EXTRACT(YEAR FROM bsm.created_date) = EXTRACT(YEAR FROM CURRENT_DATE)
    ) AS currentMonthDeliveredSmsCount,

    -- NEW: Fetch previous month's delivered SMS count separately
    (SELECT COALESCE(COUNT(bsm.id), 0)
     FROM bulk_sms_message bsm
     JOIN bulk_sms_campaign bsc ON bsm.campaign_id = bsc.id
     WHERE bsc.advertiser_id = :advertiserId
       AND bsm.status = 'DELIVERED'
       AND EXTRACT(MONTH FROM bsm.created_date) = EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '1 month')
       AND EXTRACT(YEAR FROM bsm.created_date) = EXTRACT(YEAR FROM CURRENT_DATE - INTERVAL '1 month')
    ) AS previousMonthDeliveredSmsCount

FROM wallet w
WHERE w.advertiser_id = :advertiserId
""", nativeQuery = true)
    List<Object[]> getBulkSmsDashboardSummaryData(@Param("advertiserId") Long advertiserId);
    Optional<BulkSmsCampaign> findByBulkSmsCampaignId(String bulkSmsCampaignId);


    @Query(value = """
    select 
        count(c) as totalCampaigns,
        sum(case when c.status = 'IN_PROGRESS' then 1 else 0 end) as activeCampaigns,
        sum(case when c.status = 'SCHEDULED' then 1 else 0 end) as scheduled,
        sum(c.total_delivered) as totalMessageSent,
        (sum(c.total_delivered) * 1.0 / nullif(sum(c.total_numbers), 0)) * 100.0 as overallSuccessRate
    from bulk_sms_campaign c
    where c.advertiser_id = :advertiserId and
          c.created_date >= :startDate and
          c.created_date <= :endDate
""", nativeQuery = true)
    Object[] getAdvertiserCampaignStats(@Param("advertiserId") long advertiserId, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);


    @Query(value = """
    select 
        count(c) as totalCampaigns,
        sum(case when c.status = 'IN_PROGRESS' then 1 else 0 end) as activeCampaigns,
        sum(case when c.status = 'SCHEDULED' then 1 else 0 end) as scheduled,
        sum(c.total_delivered) as totalMessageSent,
        (sum(c.total_delivered) * 1.0 / nullif(sum(c.total_numbers), 0)) * 100.0 as overallSuccessRate
    from bulk_sms_campaign c
    where c.advertiser_id = :advertiserId
""", nativeQuery = true)
    Object[] getAdvertiserCampaignStatsWithoutDateFilter(@Param("advertiserId") long advertiserId);

    Page<BulkSmsCampaign> findAllByCreatedDateBetween(Instant startDate, Instant endDate, Pageable pageable);


    @Query(value = """
            SELECT
            EXTRACT(HOUR FROM d.created_date) AS deliveryHour,
            COUNT(*) AS totalMessages,
            COUNT(*) FILTER (WHERE d.status = 'DELIVERED') AS deliveredCount,
            (COUNT(*) FILTER (WHERE d.status = 'DELIVERED') * 100.0 / COUNT(*)) AS deliveryRate
        FROM bulk_sms_message d
        GROUP BY EXTRACT(HOUR FROM d.created_date)
        ORDER BY deliveryRate DESC
        LIMIT 3;
        """, nativeQuery = true)
    List<HourlyDeliveryRate> findTop3DeliveryRatesByHour();


    @Query(value = """

            SELECT
            c.processor,
            COUNT(m.id) AS totalMessages,
            SUM(CASE WHEN m.status = 'DELIVERED' THEN 1 ELSE 0 END) AS deliveredCount,
            (SUM(CASE WHEN m.status = 'DELIVERED' THEN 1 ELSE 0 END) * 100.0 / COUNT(m.id)) AS deliveryRate
        FROM bulk_sms_campaign c
                 JOIN bulk_sms_message m ON c.id = m.campaign_id
        GROUP BY c.processor
        ORDER BY deliveryRate DESC
        LIMIT 3;
        """, nativeQuery = true)
    List<CampaignDeliveryRate> findCampaignDeliveryRates();


    List<BulkSmsCampaign> findAllByCreatedDateBetween(Instant startDate, Instant endDate);
}
