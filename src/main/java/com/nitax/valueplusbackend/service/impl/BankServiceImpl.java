package com.nitax.valueplusbackend.service.impl;

import com.nitax.valueplusbackend.domain.Admin;
import com.nitax.valueplusbackend.domain.Bank;
import com.nitax.valueplusbackend.dto.request.BankDetailsRequest;
import com.nitax.valueplusbackend.repository.BankRepository;
import com.nitax.valueplusbackend.service.AdminService;
import com.nitax.valueplusbackend.service.BankService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class BankServiceImpl implements BankService {
    private final BankRepository bankRepository;
//    private final AdminService adminService;
    @Override
    public Bank getBankDetails() {
//        Bank bank = new Bank();
//        bank.setAccountName("Valueplus Agency Ltd");
//        bank.setAccountNumber("1304969512");
//        bank.setBankName("Providus Bank");
        return bankRepository.findCurrentActiveBank().get();
    }

    @Override
    public Bank saveBankDetails(BankDetailsRequest request, Admin admin) {
        Bank bank =  new Bank();
        bank.setBankName(request.getBankName());
        bank.setAccountNumber(request.getAccountNumber());
        bank.setAccountName(request.getAccountHolderName());
        bank.setAddedBy(admin);

        Optional<Bank> currentActiveBank = bankRepository.findCurrentActiveBank();
        if (currentActiveBank.isPresent()){
            currentActiveBank.get().setActive(false);
            bankRepository.save(currentActiveBank.get());
        }



        bank.setActive(true);
        return bankRepository.save(bank);
    }

//    private Admin getCurrentAdmin(){
//        String adminEmail = "";
//        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        if (principal instanceof UserDetailsImpl userDetails) {
//            adminEmail =  userDetails.getEmail();
//        }
//        return adminService.getAdminByEmail(adminEmail);
//    }
}
