package com.nitax.valueplusbackend.repository;

import com.nitax.valueplusbackend.domain.Blocklist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface BlocklistRepository extends JpaRepository<Blocklist, Long> {

    /**
     * Global Churn enforcement — the single active GLOBAL block for this MSISDN
     */
    @Query(
            """
            SELECT b FROM Blocklist b
            WHERE b.msisdn = :msisdn
              AND b.scope = 'GLOBAL'
              AND b.released = false
              AND (b.expiresAt IS NULL OR b.expiresAt > :now)
            ORDER BY b.expiresAt DESC NULLS FIRST
            """)
    List<Blocklist> findActiveGlobalBlocks(@Param("msisdn") String msisdn, @Param("now") Instant now);

    @Query(
            """
            SELECT b FROM Blocklist b
            WHERE b.msisdn = :msisdn
              AND b.scope = 'SERVICE'
              AND b.serviceId = :serviceId
              AND b.released = false
              AND (b.expiresAt IS NULL OR b.expiresAt > :now)
            ORDER BY b.expiresAt DESC NULLS FIRST
            """)
    List<Blocklist> findActiveServiceBlocks(
            @Param("msisdn") String msisdn, @Param("serviceId") String serviceId, @Param("now") Instant now);

    Page<Blocklist> findByMsisdnOrderByCreatedDateDesc(String msisdn, Pageable pageable);

    Page<Blocklist> findAllByOrderByCreatedDateDesc(Pageable pageable);
}
