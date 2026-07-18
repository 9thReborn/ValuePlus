package com.nitax.valueplusbackend.repository;

import com.nitax.valueplusbackend.domain.BulkSMSCampaignTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BulkSMSCampaignTargetRepository extends JpaRepository<BulkSMSCampaignTarget,Long> {
}
