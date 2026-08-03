package com.nitax.valueplusbackend.repository;

import com.nitax.valueplusbackend.domain.ConversionDecision;
import com.nitax.valueplusbackend.domain.ValidationDecision;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ConversionDecisionRepository extends JpaRepository<ConversionDecision, Long> {

    List<ConversionDecision> findBySubscriberEventId(Long subscriberEventId);

    Page<ConversionDecision> findByMsisdnOrderByDecisionTimeDesc(String msisdn, Pageable pageable);

    /**
     * most recent non-replay ALLOW/decision for this msisdn+service inside a
     * window, used to check whether a cooldown-period duplicate subscription already exists.
     */
    @Query(
            """
            SELECT cd FROM ConversionDecision cd
            WHERE cd.msisdn = :msisdn
              AND cd.serviceId = :serviceId
              AND cd.replay = false
              AND cd.decisionTime >= :since
            ORDER BY cd.decisionTime DESC
            """)
    List<ConversionDecision> findRecentByMsisdnAndService(
            @Param("msisdn") String msisdn,
            @Param("serviceId") String serviceId,
            @Param("since") Instant since);

    /** how many distinct affiliates(publishers) have claimed this msisdn recently. */
    @Query(
            """
            SELECT COUNT(DISTINCT cd.publisherId) FROM ConversionDecision cd
            WHERE cd.msisdn = :msisdn
              AND cd.replay = false
              AND cd.decisionTime >= :since
            """)
    long countDistinctAffiliatesForMsisdnSince(
            @Param("msisdn") String msisdn, @Param("since") Instant since);

    Page<ConversionDecision> findByDecisionOrderByDecisionTimeDesc(
            ValidationDecision decision, Pageable pageable);
}

