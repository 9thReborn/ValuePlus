package com.nitax.valueplusbackend.repository;

import com.nitax.valueplusbackend.domain.SmsProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SmsProviderRepository extends JpaRepository<SmsProvider, Long> {


    @Query("SELECT sp FROM SmsProvider sp WHERE sp.isActive = :isActive")
    SmsProvider findSmsProviderByActive(@Param("isActive") boolean isActive);

}
