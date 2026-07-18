package com.nitax.valueplusbackend.repository;

import com.nitax.valueplusbackend.domain.SystemExcludedNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemExcludedNumberRepository extends JpaRepository<SystemExcludedNumber, Long> {

}
