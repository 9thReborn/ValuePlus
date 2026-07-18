package com.nitax.valueplusbackend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nitax.valueplusbackend.domain.PublisherCampaign;

@Repository
public interface PublisherCampaignRepository extends JpaRepository<PublisherCampaign, Long> {
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE FALSE END " +
           "FROM PublisherCampaign p WHERE p.publisher.pubId = :publisherId and p.campaign.campaignId = :campaignId")
    boolean existsByPublisherIdAndCampaignId(String publisherId, String campaignId);

    boolean existsByPubCampId(String pubCampId);

    @Query("SELECT p FROM PublisherCampaign p WHERE p.publisher.pubId = :publisherId and p.campaign.campaignId = :campaignId")
    Optional<PublisherCampaign> getByPublisherIdAndCampaignId(String publisherId, String campaignId);

    @Query("SELECT pc FROM PublisherCampaign pc WHERE pc.campaign.campaignId=:campaignId")
    List<PublisherCampaign> getByCampaignId(String campaignId);

    @Query("SELECT pc FROM PublisherCampaign pc WHERE pc.publisher.pubId=:publisherId AND pc.campaign.isDeleted = false")
    List<PublisherCampaign> getByPublisherId(String publisherId);

    @Query("SELECT pc FROM PublisherCampaign pc WHERE pc.pubCampId=:campaignId")
    PublisherCampaign getByPubSubId(String campaignId);

    @Modifying
    @Query("DELETE FROM PublisherCampaign pc WHERE pc.pubCampId= :pubCampId")
    void deleteByPubCampId(String pubCampId);

    @Modifying
    @Query("DELETE FROM PublisherCampaign pc WHERE pc.publisher.pubId = :publisherId")
    void deleteByPublisherId(@Param("publisherId") String publisherId);

    @Modifying
    @Query("UPDATE PublisherCampaign pc SET pc.publisherCpa =:pubCpa WHERE pc.pubCampId= :pubCampId")
    void updateCPAByPubCamId(String pubCampId, Double pubCpa);

    @Query("SELECT COUNT(pc) FROM PublisherCampaign pc WHERE pc.publisher.id = :publisher_id")
    Long getAllPublisherCampaignsCountByPublisherId(@Param("publisher_id") long publisherId);


    @Query("SELECT COUNT(pc) FROM PublisherCampaign pc WHERE pc.publisher.id = :publisher_id AND pc.active = true ")
    Long getTotalNumberOfActiveCampaignsByPublisherId(@Param("publisher_id")long publisherId);

    @Query("SELECT COUNT(pc) FROM PublisherCampaign pc WHERE pc.publisher.id = :publisher_id AND  pc.paused = true ")
    Long getTotalNumberOfPausedCampaignsByPublisherId(@Param("publisher_id")long publisherId);

    @Query("SELECT COUNT(pc) FROM PublisherCampaign pc WHERE pc.publisher.id = :publisher_id AND pc.deleted = true ")
    Long getTotalNumberOfDisabledCampaignsByPublisherId(@Param("publisher_id")long publisherId);

    List<PublisherCampaign> findAllByCampaign_CampaignId(String campaignId);




    @Query(
            value =
                    "SELECT "
                            + "c.campaign_id AS campaignId, "
                            + "c.name AS campaignName, "
                            + "c.status AS status, "
                            + "SUM(CASE WHEN n.status  IN('PUBLISHER_HOOK_SENT','ADVERTISER_HOOK_RECEIVED') THEN 1 ELSE 0 END) AS conversions, "
                            + "SUM(CASE WHEN n.status = 'PUBLISHER_HOOK_RECEIVED' THEN 1 ELSE 0 END) AS clicks, "
                            + "SUM(CASE WHEN n.duration < (8*24*60*60) THEN 1 ELSE 0 END) AS churn, "
                            + "pc.publisher_cpa AS cpaCostPerUser, "
                            + "COALESCE(SUM(n.cpa_revenue), 0) AS totalCost "
                            + "FROM hook_notifications_archive_partitioned n "
                            + "INNER JOIN campaigns c ON n.campaign_id = c.campaign_id "
                            + "INNER JOIN publisher_campaign pc ON c.id = pc.campaign_id AND pc.publisher_id = (SELECT  id from publishers where pub_id =:publisherId) "
                            + "WHERE n.publisher_id = :publisherId "
                            + "AND n.created_date >= DATE_TRUNC('month', CURRENT_DATE) "
                            + "GROUP BY c.campaign_id, c.name, c.status, pc.publisher_cpa "
                            + "ORDER BY clicks DESC, conversions DESC "
                            + "LIMIT 3",
            nativeQuery = true)
    List<Object[]> getTopCampaignsForPublisher(
            @Param("publisherId") String publisherId);

    @Query(value = """
    SELECT
        c.campaign_id AS campaignId,
        c.name AS campaignName,
        c.status AS status,
        c.cpa_cost_per_user AS cpa,
        c.carrier_connection AS mno,
        c.click_flow AS clickFlow,
        c.country AS country,
        0 AS conversions,
        0 AS clicks,
        0 AS churn,
        pc.publisher_cpa AS cpaCostPerUser,
        0 AS totalCost
    FROM
        campaigns c
    JOIN
        publisher_campaign pc ON c.id = pc.campaign_id
    JOIN
        publishers p ON p.id = pc.publisher_id
    WHERE
        c.status = 'ACTIVE'
        AND p.pub_id = :publisherId
        AND c.created_date >= DATE_TRUNC('month', CURRENT_DATE)
        AND c.created_date <= CURRENT_DATE
        AND NOT EXISTS (
            SELECT 1
            FROM hook_notifications_archive_partitioned n
            WHERE n.campaign_id = c.campaign_id
              AND n.publisher_id = p.pub_id
              AND n.status IN (
                  'PUBLISHER_HOOK_SENT',
                  'PUBLISHER_HOOK_RECEIVED',
                  'ADVERTISER_HOOK_RECEIVED',
                  'ADVERTISER_HOOK_SENT'
              )
        )
    LIMIT 3
    """, nativeQuery = true)
    List<Object[]> getPublisherTop3AvailableCampaigns(
            @Param("publisherId") String publisherId);

}


