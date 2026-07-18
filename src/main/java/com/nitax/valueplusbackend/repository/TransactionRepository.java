package com.nitax.valueplusbackend.repository;

import com.nitax.valueplusbackend.domain.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction,Long> {

    boolean existsByTransactionId(String string);

    @Query(
            """
        select t from Transaction t
        where t.advertiser.advertiserId = :advertiserId
"""
    )
    Page<Transaction> findAllAdvertiserTransactions(@Param("advertiserId") String advertiserId, Pageable pageable);
}
