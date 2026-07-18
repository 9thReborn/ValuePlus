package com.nitax.valueplusbackend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nitax.valueplusbackend.domain.Subscriber;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {

  Optional<Subscriber> findByMsisdn(String msisdn);

  Optional<Subscriber> findByServiceId(String serviceId);

  Optional<Subscriber> findByMsisdnAndServiceId(String msisdn, String serviceId);

  Optional<Subscriber> findByTrxId(String trxId);

  Optional<Subscriber> findByMsisdnAndTrxId(String msisdn, String trxId);

  List<Subscriber> findByServiceId(String serviceId, org.springframework.data.domain.Pageable pageable);

  List<Subscriber> findByAdvertiserId(String advertiserId);

  List<Subscriber> findByStatus(Subscriber.SubscriberStatus status);

  boolean existsByMsisdnAndTrxId(String msisdn, String trxId);
}
