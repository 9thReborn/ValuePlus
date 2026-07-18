package com.nitax.valueplusbackend.repository;

import com.nitax.valueplusbackend.domain.Blocklist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface BlocklistRepository extends JpaRepository<Blocklist, Long> {

    /**
     * Rule B enforcement — the single active GLOBAL block for this MSISDN, if any. "Active" means
     * not manually released and either permanent ({@code expiresAt IS NULL}) or not yet expired.
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
    Optional<Blocklist> findActiveGlobalBlock(@Param("msisdn") String msisdn, @Param("now") Instant now);

    Page<Blocklist> findByMsisdnOrderByCreatedDateDesc(String msisdn, Pageable pageable);

    Page<Blocklist> findAllByOrderByCreatedDateDesc(Pageable pageable);
}
