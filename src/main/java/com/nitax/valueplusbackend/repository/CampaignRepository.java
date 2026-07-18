package com.nitax.valueplusbackend.repository;

import com.nitax.valueplusbackend.domain.Advertiser;
import com.nitax.valueplusbackend.domain.BulkSmsCampaign;
import com.nitax.valueplusbackend.domain.Campaign;
import com.nitax.valueplusbackend.dto.response.AdvertiserConversionDTOForTop;
import com.nitax.valueplusbackend.utils.enums.CampaignTypes;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
  boolean existsByCampaignId(String toString);

  @EntityGraph(attributePaths = {"advertiser"})
  Optional<Campaign> findByCampaignId(String campaignId);

  List<Campaign> findByAdvertiserId(Long advertiserId);

  @Query("SELECT COUNT(c) FROM Campaign c WHERE c.advertiser = :advertiser AND c.status = 'ACTIVE'")
  Long getNumberOfActiveCampaigns(Advertiser advertiser);

  @Query("SELECT COUNT(c) FROM Campaign c WHERE c.advertiser = :advertiser")
  Long getNumberOfCampaigns(Advertiser advertiser);

  @Query("SELECT c FROM Campaign c WHERE c.advertiser = :advertiser AND c.status = 'ACTIVE'")
  List<Campaign> getActiveCampaigns(Advertiser advertiser);

  @Query(
      value =
          "SELECT COUNT(*) FROM campaigns "
              + "WHERE advertiser_id = :advertiserId "
              + "  AND created_date >= CURRENT_DATE - INTERVAL '7 day' "
              + "GROUP BY DATE(created_date)",
      nativeQuery = true)
  long[] getTotalCampaignsStats(Long advertiserId);

  @Query(
      value =
          "SELECT COUNT(*) FROM Campaign c "
              + "WHERE c.advertiser = :advertiser AND c.createdDate < :today")
  long getNumberOfCampaignsPreviousDays(Advertiser advertiser, Instant today);

  @Query(
      value =
          "SELECT COUNT(*) FROM Campaign c "
              + "WHERE c.advertiser = :advertiser AND c.status = 'ACTIVE' AND c.createdDate < :today")
  long getNumberOfActiveCampaignsPreviousDays(Advertiser advertiser, Instant today);

  @Query(
      value =
          "SELECT COUNT(*) FROM campaigns "
              + "WHERE advertiser_id = :advertiserId "
              + "  AND created_date >= CURRENT_DATE - INTERVAL '7 day' "
              + " AND status = 'ACTIVE' "
              + "GROUP BY DATE(created_date)",
      nativeQuery = true)
  long[] getActiveCampaignsStats(Long advertiserId);

  @Query(
      value =
          "SELECT SUM(n.vpRevenue) AS totalConversionCost "
              + "FROM Notification n "
              + "INNER JOIN Campaign c "
              + "ON n.campaignId = c.campaignId "
              + "WHERE c.advertiser.id = :advertiserId "
              + "AND n.status IN ('PUBLISHER_HOOK_SENT', 'ADVERTISER_HOOK_RECEIVED') "
              + "AND n.createdDate BETWEEN :startDate AND :endDate "
              + "AND n.year = :currentYear "
              + "GROUP BY n.month, n.year "
              + "ORDER BY n.month ASC")
  double[] getTotalCampaignsSpendStats(
      @Param("advertiserId") Long advertiserId,
      @Param("currentYear") Integer currentYear,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query(
      value =
          "SELECT COUNT(*) "
              + "FROM hook_notifications_archive_partitioned n "
              + "INNER JOIN campaigns c ON n.campaign_id = c.campaign_id "
              + "WHERE c.advertiser_id = :advertiserId "
              + "AND n.status IN ('PUBLISHER_HOOK_SENT','ADVERTISER_HOOK_RECEIVED') AND year = :year "
              + "GROUP BY n.year, n.month "
              + "ORDER BY n.month ASC",
      nativeQuery = true)
  long[] getMonthlyConversionData(Long advertiserId, Integer year);

  @Query(
      value =
          "SELECT n.month AS conversion_month "
              + "FROM hook_notifications_archive_partitioned n "
              + "INNER JOIN campaigns c ON n.campaign_id = c.campaign_id "
              + "WHERE c.advertiser_id = :advertiserId AND n.year = :year "
              + "GROUP BY n.month, n.year "
              + "ORDER BY n.month ASC",
      nativeQuery = true)
  int[] getMonthlyConversionMonths(Long advertiserId, Integer year);

  @Query(
      "SELECT c FROM Campaign c WHERE c.advertiser = ?1 "
          + "AND (?2 IS NULL OR LOWER(CAST(c.name AS text)) LIKE LOWER(CONCAT('%', CAST(?2 AS text), '%'))) "
          + "AND (?3 IS NULL OR LOWER(CAST(c.country AS text)) LIKE LOWER(CONCAT('%', CAST(?3 AS text), '%'))) "
          + "AND (?4 IS NULL OR c.type = ?4)"
          + "AND (?5 IS NULL OR c.status = ?5)")
  Page<Campaign> findAllByAdvertiserWithFilters(
      Advertiser advertiser,
      String name,
      String country,
      CampaignTypes campaignType,
      String status,
      Pageable pageable);

  @Query(
      "SELECT c FROM Campaign c WHERE c.advertiser = :advertiser"
          + " AND (:name IS NULL OR c.name LIKE :name)")
  Page<Campaign> findAllByAdvertiserAndNameContaining(
      Advertiser advertiser, String name, Pageable pageable);

  @Query(
      value =
          "SELECT COUNT(*) "
              + "FROM hook_notifications_archive_partitioned n "
              + "INNER JOIN campaigns c ON n.campaign_id = c.campaign_id "
              + "WHERE c.advertiser_id = :advertiserId AND n.status = 'PUBLISHER_HOOK_RECEIVED' AND n.year = :year "
              + "GROUP BY n.month, n.year "
              + "ORDER BY n.month ASC",
      nativeQuery = true)
  long[] getClickData(Long advertiserId, Integer year);

  @Transactional
  @Modifying
  @Query("UPDATE Campaign c SET c.status = 'INACTIVE' WHERE c.campaignId = :campaignId")
  void deactivateCampaign(@Param("campaignId") String campaignId);

  @Transactional
  @Modifying
  @Query("UPDATE Campaign c SET c.status = 'ACTIVE' WHERE c.campaignId = :campaignId")
  void activateCampaign(String campaignId);

  @Override
  @EntityGraph(attributePaths = {"advertiser"})
  List<Campaign> findAll();

  @Query("SELECT COUNT(c) FROM Campaign c WHERE c.status = 'ACTIVE'")
  Long getActiveCampaignsForAdmin();

  @Query("SELECT COUNT(c) FROM Campaign c WHERE c.status = 'INACTIVE'")
  Long getPausedCampaignsForAdmin();

  @Query("SELECT COUNT(c) FROM Campaign c WHERE c.isDisabled = true")
  Long getDisabledCampaignsForAdmin();

  @Query("SELECT c FROM Campaign c WHERE c.status = 'ACTIVE' ORDER BY c.acquisition DESC")
  @EntityGraph(attributePaths = {"advertiser"})
  List<Campaign> findTop5ByAcquisition(Pageable pageable);

  @Query(
      "SELECT new com.nitax.valueplusbackend.dto.response.AdvertiserConversionDTOForTop(c.status, c.advertiser.businessName, c.name,c.acquisition,c.reach, null, c.budget, c.costPerUser, c.campaignId, c.campaignCost) "
          + "FROM Campaign c "
          + "WHERE c.status = 'ACTIVE' "
          + "GROUP BY c.advertiser.businessName, c.name, c.acquisition, c.reach, c.budget, c.costPerUser, c.status, c.campaignId, c.campaignCost "
          + "ORDER BY c.acquisition DESC")
  @EntityGraph(attributePaths = {"advertiser"})
  List<AdvertiserConversionDTOForTop> findTop5ByAcquisitionTest(Pageable pageable);

  @Query(
      "SELECT c FROM Campaign c WHERE c.id NOT IN :topCampaignIds AND c.status = 'ACTIVE' ORDER BY c.acquisition ASC")
  @EntityGraph(attributePaths = {"advertiser"})
  List<Campaign> findLeast5ByAcquisition(
      @Param("topCampaignIds") List<Long> topCampaignIds, Pageable pageable);

  @Query(
      "SELECT new com.nitax.valueplusbackend.dto.response.AdvertiserConversionDTOForTop(c.status, c.advertiser.businessName, c.name,c.acquisition,c.reach, null, c.budget, c.costPerUser, c.campaignId, c.campaignCost)"
          + "FROM Campaign c "
          + "WHERE c.campaignId NOT IN :top5CampaignNames AND c.status = 'ACTIVE' "
          + "GROUP BY c.advertiser.businessName, c.name, c.acquisition, c.reach, c.budget, c.costPerUser, c.status, c.campaignId, c.campaignCost "
          + "ORDER BY SUM(c.acquisition) DESC")
  List<AdvertiserConversionDTOForTop> findLeast5ByAcquisitionTest(
      @Param("top5CampaignNames") List<String> top5CampaignNames, Pageable pageable);

  @Query(
      "SELECT c FROM Campaign c "
          + "WHERE (:name IS NULL OR LOWER(CAST(c.name AS text)) LIKE LOWER(CONCAT('%', CAST(:name AS text), '%'))) "
          + "AND (:countryName IS NULL OR LOWER(CAST(c.country AS text)) LIKE LOWER(CONCAT('%', CAST(:countryName AS text), '%'))) "
          + "AND (:campaignType IS NULL OR c.type = :campaignType) "
          + "AND ((:status IS NOT NULL AND c.status = :status) OR (:status IS NULL AND c.status <> 'DELETED')) ")
  @EntityGraph(attributePaths = {"advertiser"})
  Page<Campaign> getAllCampaignsWithFiltersForAdmin(
      @Param("name") String name,
      @Param("countryName") String countryName,
      @Param("campaignType") CampaignTypes campaignType,
      @Param("status") String status,
      Pageable pageable);

  @Query(
      """
SELECT cp FROM Campaign cp WHERE cp.id NOT IN (SELECT DISTINCT pb.campaign.id FROM PublisherCampaign pb)
""")
  List<Campaign> getAllUnMappedCampaigns();

  //  @Query("SELECT c FROM Campaign c JOIN FETCH c.product WHERE c.campaignId = :campaignId")
  //  Optional<Campaign> findByCampaignIdWithProduct(@Param("campaignId") String campaignId);

  // Autosuggestion
  @Query(
      value =
          "SELECT c.name,c.campaign_id FROM campaigns c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))",
      nativeQuery = true)
  List<Object[]> findCampaignByQuery(@Param("query") String query);

  @Query("SELECT c FROM Campaign c where c.status = 'ACTIVE' ORDER BY c.createdDate DESC LIMIT 3")
  List<Campaign> findTopThreeNewCampaigns();

  @Query("""
    SELECT c FROM Campaign c 
    WHERE c.id NOT IN (
        SELECT pc.campaign.id FROM PublisherCampaign pc WHERE pc.publisher.pubId = :publisherId
    )and c.status = 'ACTIVE'
    ORDER BY c.createdDate DESC
""")
  Page<Campaign> findNewAvailableCampaignForPublisher(@Param("publisherId") String publisherId, Pageable pageable);
  Optional<Campaign> findByName(String campaignName);
}
