package com.nitax.valueplusbackend.service;

import com.nitax.valueplusbackend.dto.response.AdvertiserTransactionResponse;

import java.util.List;

public interface TransactionService {

    List<AdvertiserTransactionResponse> getAdvertiserTransactions(String advertiserId, int page, int pageSize);
}
