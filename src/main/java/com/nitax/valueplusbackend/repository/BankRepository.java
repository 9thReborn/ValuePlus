package com.nitax.valueplusbackend.repository;

import com.nitax.valueplusbackend.domain.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankRepository extends JpaRepository<Bank,Long> {

    @Query("select b from Bank b where b.isActive = true")
    Optional<Bank> findCurrentActiveBank();
}
