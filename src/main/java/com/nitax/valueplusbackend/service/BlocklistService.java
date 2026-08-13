package com.nitax.valueplusbackend.service;

import com.nitax.valueplusbackend.domain.Blocklist;
import com.nitax.valueplusbackend.domain.ReasonCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BlocklistService {
    Blocklist createOrRefreshGlobalBlock(String msisdn, ReasonCode reasonCode, String createdBy);
    Optional<Blocklist> findActiveGlobalBlock(String msisdn);

    Optional<Blocklist> findActiveServiceBlock(String msisdn, String serviceId);
    Blocklist release(Long blockId, String releasedBy);
    Blocklist createManualBlock(
            String msisdn,
            Blocklist.Scope scope,
            String serviceId,
            Integer durationHours,
            String createdBy);
    Page<Blocklist> findByMsisdn(String msisdn, Pageable pageable);
    Page<Blocklist> findAll(Pageable pageable);
}
