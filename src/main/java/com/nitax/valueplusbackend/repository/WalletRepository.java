package com.nitax.valueplusbackend.repository;

import com.nitax.valueplusbackend.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet,Long> {

    @Query("select w.pointsBalance from Wallet w where w.advertiser.id =:advertiserId")
    double fetchWalletPointBalanceByAdvertiserId(@Param("advertiserId") long advertiserId);

    @Query("select w.balance from Wallet w where w.advertiser.id =:advertiserId")
    double fetchWalletBalanceByAdvertiserId(@Param("advertiserId") long advertiserId);

    Optional<Wallet> findWalletByAdvertiser_Id(long advertiserId);

    @Query("select sum(w.pointsBalance) from Wallet w")
    BigDecimal getTotalWalletPoints();

    @Query("select w from Wallet w where w.advertiser.advertiserId = :advertiserId")
    Optional<Wallet> findWalletByAdvertiserId(String advertiserId);
}
