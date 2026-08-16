package com.nitax.valueplusbackend.repository;

import com.nitax.valueplusbackend.domain.ConversionDecision;
import com.nitax.valueplusbackend.domain.ValidationDecision;
import com.nitax.valueplusbackend.dto.response.PublisherPeriodCountDTO;
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

    @Query(
            """
            SELECT DISTINCT cd.publisherId FROM ConversionDecision cd
            WHERE cd.msisdn = :msisdn
              AND cd.replay = false
              AND cd.decisionTime >= :since
              AND cd.publisherId IS NOT NULL
              AND cd.publisherId <> :currentPublisherId
            """)
    List<String> findDistinctOtherPublisherIdsForMsisdnSince(
            @Param("msisdn") String msisdn,
            @Param("since") Instant since,
            @Param("currentPublisherId") String currentPublisherId);

    Page<ConversionDecision> findByDecisionOrderByDecisionTimeDesc(
            ValidationDecision decision, Pageable pageable);

    @Query(
            "SELECT new com.nitax.valueplusbackend.dto.response.PublisherPeriodCountDTO("
                    + "cd.publisherId, COUNT(cd)) "
                    + "FROM ConversionDecision cd "
                    + "WHERE cd.replay = false "
                    + "  AND cd.decision = :decision "
                    + "  AND cd.decisionTime BETWEEN :start AND :end "
                    + "  AND cd.publisherId IS NOT NULL "
                    + "GROUP BY cd.publisherId")
    List<PublisherPeriodCountDTO> countByPublisherForDecisionInPeriod(
            @Param("decision") ValidationDecision decision,
            @Param("start") Instant start,
            @Param("end") Instant end);
}

