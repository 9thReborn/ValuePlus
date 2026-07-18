package com.nitax.valueplusbackend.service;

import com.nitax.valueplusbackend.domain.Admin;
import com.nitax.valueplusbackend.domain.Bank;
import com.nitax.valueplusbackend.dto.request.BankDetailsRequest;

public interface BankService {
    Bank getBankDetails();
    Bank saveBankDetails(BankDetailsRequest request, Admin admin);
}
