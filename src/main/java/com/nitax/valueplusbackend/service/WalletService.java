package com.nitax.valueplusbackend.service;

import com.nitax.valueplusbackend.domain.Advertiser;
import com.nitax.valueplusbackend.domain.Wallet;
import com.nitax.valueplusbackend.dto.request.FundWalletRequest;
import com.nitax.valueplusbackend.dto.response.WalletDetailsResponse;
import com.nitax.valueplusbackend.dto.response.external.GetWalletBalanceResponse;

import java.math.BigDecimal;

public interface WalletService {
    double fetchWalletBalanceByAdvertiserId(long advertiserId);
    double fetchWalletPointBalanceByAdvertiserId(long advertiserId);

    double deductWalletPointAndBalanceByAdvertiserId(long advertiserId,double points);

    WalletDetailsResponse getAdvertiserWallet();

    Wallet createAdvertiserWallet(Advertiser advertiser);

    void refundWalletPointAndBalanceByAdvertiserId(Long id, double pointsToRefund);


    Wallet getWalletByAdvertiserId(long advertiserId);
    Wallet getWalletByAdvertiserId(String advertiserId);
    Wallet fundWallet(FundWalletRequest request);
    BigDecimal getTotalWalletPoints();
}
