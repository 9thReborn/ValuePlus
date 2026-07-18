package com.nitax.valueplusbackend.repository;

import com.nitax.valueplusbackend.domain.Advertiser;

import java.util.List;
import java.util.Optional;

import com.nitax.valueplusbackend.domain.AdvertiserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AdvertiserRepository extends JpaRepository<Advertiser, Long> {
  boolean existsByAdvertiserId(String toString);

  boolean existsByEmail(String email);

  Optional<Advertiser> findByEmail(String email);

  Optional<Advertiser> findByAdvertiserId(String advertiserId);

  @Query(
          "SELECT a FROM Advertiser a WHERE (:businessName IS NULL OR a.businessName LIKE %:businessName%) AND (:status IS NULL OR a.status = :status)")
  Page<Advertiser> getAllAdvertisers(@Param("businessName")String businessName, @Param("status")AdvertiserStatus status, Pageable pageable);

  List<Advertiser> findAllByStatus(AdvertiserStatus advertiserStatus);

  @Query("SELECT count(*) FROM Advertiser a WHERE a.isBulkSmsEnabled = true")
  long countAdvertisersWithBulkSmsEnabled();

  @Query("SELECT count(*) FROM Advertiser a WHERE a.isBulkSmsEnabled = true and a.isAccountActive = true")
  long countAdvertisersWithBulkSmsEnabledAndActive();

  @Query("SELECT a FROM Advertiser a WHERE a.isBulkSmsEnabled = true order by a.createdDate desc")
  Page<Advertiser> findAllByBulkSmsEnabledAdvertiser(Pageable pageable);
}
