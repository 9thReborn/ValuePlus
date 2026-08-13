package com.nitax.valueplusbackend.service.impl;

import com.nitax.valueplusbackend.config.FraudRuleProperties;
import com.nitax.valueplusbackend.domain.Blocklist;
import com.nitax.valueplusbackend.domain.ReasonCode;
import com.nitax.valueplusbackend.exception.AppException;
import com.nitax.valueplusbackend.repository.BlocklistRepository;
import com.nitax.valueplusbackend.service.BlocklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlocklistServiceImpl implements BlocklistService {

    private static final String SYSTEM_CREATED_BY = "SYSTEM:GLOBAL_CHURN_BLOCK";

    private final BlocklistRepository blocklistRepository;
    private final FraudRuleProperties fraudRuleProperties;

    @Override
    public Blocklist createOrRefreshGlobalBlock(String msisdn, ReasonCode reasonCode, String createdBy) {
        Instant now = Instant.now();
        Instant newExpiry = now.plus(Duration.ofHours(fraudRuleProperties.getTempBlockDurationHours()));

        List<Blocklist> existing = blocklistRepository.findActiveGlobalBlocks(msisdn, now);
        if (!existing.isEmpty()) {
            Blocklist block = existing.get(0);
            block.setExpiresAt(newExpiry);
            block.setReasonCode(reasonCode);
            log.info(
                    "Refreshed global block for msisdn={}, newExpiresAt={}, reasonCode={}",
                    msisdn,
                    newExpiry,
                    reasonCode);
            return blocklistRepository.save(block);
        }

        Blocklist block = new Blocklist();
        block.setMsisdn(msisdn);
        block.setScope(Blocklist.Scope.GLOBAL);
        block.setReasonCode(reasonCode);
        block.setExpiresAt(newExpiry);
        block.setCreatedBy(createdBy == null ? SYSTEM_CREATED_BY : createdBy);
        Blocklist saved = blocklistRepository.save(block);
        log.info(
                "Created global block for msisdn={}, expiresAt={}, reasonCode={}",
                msisdn,
                newExpiry,
                reasonCode);
        return saved;
    }

    @Override
    public Optional<Blocklist> findActiveGlobalBlock(String msisdn) {
        List<Blocklist> active = blocklistRepository.findActiveGlobalBlocks(msisdn, Instant.now());
        return active.isEmpty() ? Optional.empty() : Optional.of(active.get(0));
    }

    @Override
    public Optional<Blocklist> findActiveServiceBlock(String msisdn, String serviceId) {
        List<Blocklist> active =
                blocklistRepository.findActiveServiceBlocks(msisdn, serviceId, Instant.now());
        return active.isEmpty() ? Optional.empty() : Optional.of(active.get(0));
    }

    @Override
    public Blocklist release(Long blockId, String releasedBy) {
        Blocklist block =
                blocklistRepository
                        .findById(blockId)
                        .orElseThrow(() -> new AppException("Block not found: " + blockId));

        if (block.isReleased()) {
            throw new AppException("Block " + blockId + " is already released");
        }

        block.setReleased(true);
        block.setReleasedAt(Instant.now());
        block.setReleasedBy(releasedBy);
        log.info("Block {} released by {}", blockId, releasedBy);
        return blocklistRepository.save(block);
    }

    @Override
    public Blocklist createManualBlock(
            String msisdn,
            Blocklist.Scope scope,
            String serviceId,
            Integer durationHours,
            String createdBy) {
        if (scope == Blocklist.Scope.SERVICE && (serviceId == null || serviceId.isEmpty())) {
            throw new AppException("serviceId is required for SERVICE-scope blocks");
        }
        Instant expiresAt =
                durationHours == null ? null : Instant.now().plus(Duration.ofHours(durationHours));

        if (scope == Blocklist.Scope.GLOBAL) {
            List<Blocklist> existing = blocklistRepository.findActiveGlobalBlocks(msisdn, Instant.now());
            if (!existing.isEmpty()) {
                Blocklist block = existing.get(0);
                block.setExpiresAt(expiresAt);
                block.setReasonCode(ReasonCode.MANUAL_OVERRIDE);
                block.setCreatedBy(createdBy);
                log.info(
                        "Refreshed existing global block for msisdn={} via manual override, expiresAt={}, createdBy={}",
                        msisdn,
                        expiresAt,
                        createdBy);
                return blocklistRepository.save(block);
            }
        }

        Blocklist block = new Blocklist();
        block.setMsisdn(msisdn);
        block.setScope(scope);
        block.setServiceId(scope == Blocklist.Scope.SERVICE ? serviceId : null);
        block.setReasonCode(ReasonCode.MANUAL_OVERRIDE);
        block.setExpiresAt(expiresAt);
        block.setCreatedBy(createdBy);
        log.info(
                "Manual block created: msisdn={}, scope={}, serviceId={}, expiresAt={}, createdBy={}",
                msisdn,
                scope,
                serviceId,
                block.getExpiresAt(),
                createdBy);
        return blocklistRepository.save(block);
    }

    @Override
    public Page<Blocklist> findByMsisdn(String msisdn, Pageable pageable) {
        return blocklistRepository.findByMsisdnOrderByCreatedDateDesc(msisdn, pageable);
    }

    @Override
    public Page<Blocklist> findAll(Pageable pageable) {
        return blocklistRepository.findAllByOrderByCreatedDateDesc(pageable);
    }
}
