package com.nitax.valueplusbackend.repository;

import com.nitax.valueplusbackend.domain.PhoneNumber;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PhoneNumberRepository extends JpaRepository<PhoneNumber, Long> {
  //  List<PhoneNumber> findByDeliveryStatus(PhoneNumber.Status status, PageRequest pageRequest);

  @Query(
      value = "SELECT * FROM phone_numbers WHERE number = :msisdn AND sector = :sector LIMIT 1",
      nativeQuery = true)
  PhoneNumber findByNumberAndSector(@Param("msisdn") String msisdn, @Param("sector") String sector);

  @Query(value = "SELECT * FROM phone_numbers WHERE number = :msisdn LIMIT 1", nativeQuery = true)
  Optional<PhoneNumber> findByNumber(String msisdn);


  @Query(
          value = "SELECT p FROM PhoneNumber p WHERE p.state = :state AND p.originatingCity = :originatingCity AND p.originatingLga = :originatingLga"
  )
  List<PhoneNumber> findByStateAndOriginatingCityAndOriginatingLga(
          @Param("state") String state,
          @Param("originatingCity") String originatingCity,
          @Param("originatingLga") String originatingLga,
          Pageable pageable);

  @Query(
          value = "SELECT p FROM PhoneNumber p " +
                  "WHERE p.state = :state " +
                  "AND p.originatingCity = :originatingCity " +
                  "AND p.originatingLga = :originatingLga " +
                  "AND p.number NOT IN :excludedNumbers"
  )
  List<PhoneNumber> findByStateAndOriginatingCityAndOriginatingLgaExcludingNumbers(
          @Param("state") String state,
          @Param("originatingCity") String originatingCity,
          @Param("originatingLga") String originatingLga,
          @Param("excludedNumbers") List<String> excludedNumbers,
          Pageable pageable
  );


  @Query("select distinct  p.originatingCity from PhoneNumber p")
  List<String > getDistinctCity();

  @Query("select distinct  p.state from PhoneNumber p")
  List<String > getDistinctStates();


  @Query("select distinct  p.originatingLga from PhoneNumber p")
  List<String > getDistinctLgas();


  @Query("select distinct  p.sector from PhoneNumber p")
  List<String > getDistinctSector();

}
