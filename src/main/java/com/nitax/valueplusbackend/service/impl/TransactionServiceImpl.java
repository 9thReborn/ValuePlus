package com.nitax.valueplusbackend.service.impl;

import com.nitax.valueplusbackend.domain.Transaction;
import com.nitax.valueplusbackend.dto.response.AdvertiserTransactionResponse;
import com.nitax.valueplusbackend.repository.TransactionRepository;
import com.nitax.valueplusbackend.service.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@AllArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;

    @Override
    public List<AdvertiserTransactionResponse> getAdvertiserTransactions(String advertiserId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        List<Transaction> transactions = transactionRepository.findAllAdvertiserTransactions(advertiserId, pageable).getContent();
        if (!transactions.isEmpty()) {
            return transactions.stream()
                    .map(transaction -> {
                        AdvertiserTransactionResponse response = new AdvertiserTransactionResponse();
                        response.setTransactionDate(transaction.getTransactionDate());
                        response.setTransactionId(transaction.getTransactionId());
                        if (transaction.getTransactionType().equalsIgnoreCase("CREDIT")){
                            response.setPoint(transaction.getPointAssigned() <= 0 ?transaction.getPointRefunded():transaction.getPointAssigned());
                        }else if (transaction.getTransactionType().equalsIgnoreCase("DEBIT")){
                            response.setPoint(transaction.getPointDeducted());
                        }
                        response.setTransactionType(transaction.getTransactionType());
                        response.setAmount(transaction.getAmount());

                        response.setCPA(transaction.getCostPerSms());
                        return response;
                    })
                    .toList();
        }
        return List.of();
    }
}
