package com.nitax.valueplusbackend.service.impl;

import com.nitax.valueplusbackend.domain.Advertiser;
import com.nitax.valueplusbackend.domain.Bank;
import com.nitax.valueplusbackend.domain.Transaction;
import com.nitax.valueplusbackend.domain.Wallet;
import com.nitax.valueplusbackend.dto.request.FundWalletRequest;
import com.nitax.valueplusbackend.dto.response.BankDetailsResponse;
import com.nitax.valueplusbackend.dto.response.WalletDetailsResponse;
import com.nitax.valueplusbackend.dto.response.external.GetWalletBalanceResponse;
import com.nitax.valueplusbackend.exception.WalletServiceException;
import com.nitax.valueplusbackend.repository.AdvertiserRepository;
import com.nitax.valueplusbackend.repository.TransactionRepository;
import com.nitax.valueplusbackend.repository.WalletRepository;
import com.nitax.valueplusbackend.service.BankService;
import com.nitax.valueplusbackend.service.EmailService;
import com.nitax.valueplusbackend.service.WalletService;
import com.nitax.valueplusbackend.utils.AppUtils;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import static com.nitax.valueplusbackend.utils.Constants.POINT_VALUE_NAIRA;

@Service
@AllArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final WalletRepository walletRepository;
    private final AdvertiserRepository advertiserService;
    private final BankService bankService;
    private final TransactionRepository transactionRepository;
    private final EmailService emailService;
    private final AppUtils appUtils;
    @Override
    public double fetchWalletBalanceByAdvertiserId(long advertiserId) {
        return walletRepository.fetchWalletBalanceByAdvertiserId(advertiserId);
    }

    @Override
    public double fetchWalletPointBalanceByAdvertiserId(long advertiserId) {
        return walletRepository.fetchWalletPointBalanceByAdvertiserId(advertiserId);
    }

    @Override
    public double deductWalletPointAndBalanceByAdvertiserId(long advertiserId, double points) {
        Optional<Wallet> optionalWallet =  walletRepository.findWalletByAdvertiser_Id(advertiserId);
        if (optionalWallet.isPresent()){
            Wallet wallet =  optionalWallet.get();

            BigDecimal pointsAsBigDecimal = BigDecimal.valueOf(points);
            wallet.setPointsBalance(wallet.getPointsBalance().subtract(pointsAsBigDecimal));

//            wallet.setPointsBalance(wallet.getPointsBalance().subtract(BigDecimal.valueOf(points)));

            BigDecimal monetaryValueToDeduct = pointsAsBigDecimal.multiply(POINT_VALUE_NAIRA);
            wallet.setBalance(wallet.getBalance().subtract(monetaryValueToDeduct));

//            wallet.setBalance(wallet.getBalance().subtract(BigDecimal.valueOf(points * SMS_CPA)));

            walletRepository.save(wallet);
            Transaction transaction  =  new Transaction();
            transaction.setAdvertiser(wallet.getAdvertiser());
            transaction.setTransactionDate(new Date());
            transaction.setTransactionType("DEBIT");
            transaction.setPointDeducted(points);
            transaction.setAmount(monetaryValueToDeduct);
            transaction.setTransactionId(appUtils.generateTransactionId()); // Generate a transaction ID for debit too
            transactionRepository.save(transaction);
            return wallet.getPointsBalance().doubleValue();
        }
        throw new WalletServiceException("Wallet not found for advertiser ID: " + advertiserId);
    }

    @Override
    public WalletDetailsResponse getAdvertiserWallet() {
        Advertiser curentAdvertiser =  getCurrentAdvertiser();
        Optional<Wallet> optionalWallet =  walletRepository.findWalletByAdvertiser_Id(curentAdvertiser.getId());
        if (optionalWallet.isPresent()){
            Wallet wallet =  optionalWallet.get();
            WalletDetailsResponse walletDetailsResponse =  new WalletDetailsResponse();
            BigDecimal pointsBalance = wallet.getPointsBalance();

            BigDecimal roundedPointsBalance = pointsBalance.setScale(0, RoundingMode.DOWN);

            BigDecimal valueToSubtract = roundedPointsBalance.multiply(POINT_VALUE_NAIRA);

            walletDetailsResponse.setWalletBalance(wallet.getBalance().subtract(valueToSubtract));

            walletDetailsResponse.setPointBalance(wallet.getPointsBalance());

            Bank bank = bankService.getBankDetails();
            BankDetailsResponse bankDetailsResponse =  new BankDetailsResponse();
            bankDetailsResponse.setBankName(bank.getBankName());
            bankDetailsResponse.setAccountName(bank.getAccountName());
            bankDetailsResponse.setAccountNumber(bank.getAccountNumber());
            walletDetailsResponse.setBankDetails(bankDetailsResponse);
            return walletDetailsResponse;
        }
        throw new WalletServiceException("Wallet not found for current advertiser");
    }

    @Override
    public Wallet createAdvertiserWallet(Advertiser advertiser) {
        Wallet wallet =  new Wallet();
        wallet.setAdvertiser(advertiser);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setPointsBalance(BigDecimal.ZERO);
        wallet.setTransactions(new ArrayList<>());
        return  walletRepository.save(wallet);
    }

    @Override
    public void refundWalletPointAndBalanceByAdvertiserId(Long advertiserId, double pointsToRefund) {
        Optional<Wallet> optionalWallet = walletRepository.findWalletByAdvertiser_Id(advertiserId);
        if (optionalWallet.isPresent()) {
            Wallet wallet = optionalWallet.get();

            BigDecimal pointsAsBigDecimal = BigDecimal.valueOf(pointsToRefund);

            wallet.setPointsBalance(wallet.getPointsBalance().add(pointsAsBigDecimal));


            BigDecimal monetaryValueToRefund = pointsAsBigDecimal.multiply(POINT_VALUE_NAIRA);
            wallet.setBalance(wallet.getBalance().add(monetaryValueToRefund));

//            wallet.setBalance(wallet.getBalance().add(BigDecimal.valueOf(pointsToRefund * SMS_CPA)));

            walletRepository.save(wallet);

            Transaction transaction  =  new Transaction();
            transaction.setAdvertiser(wallet.getAdvertiser());
            transaction.setTransactionDate(new Date());
            transaction.setTransactionType("REFUND");
            transaction.setPointRefunded(pointsToRefund); // Record points refunded
            transaction.setAmount(monetaryValueToRefund); // Record monetary value refunded
            transaction.setTransactionId(appUtils.generateTransactionId()); // Generate a transaction ID for refund
            transaction.setTransactionId(appUtils.generateTransactionId());
            transactionRepository.save(transaction);
        }
    }
    @Override
    public Wallet getWalletByAdvertiserId(long advertiserId){
//        Advertiser adv = getCurrentAdvertiser();
        Optional<Wallet> optionalWallet = walletRepository.findWalletByAdvertiser_Id(advertiserId);
        if (optionalWallet.isPresent()) {
            return optionalWallet.get();
        }

        throw new WalletServiceException("Wallet not found");
    }

    @Override
    public Wallet getWalletByAdvertiserId(String advertiserId) {
        Optional<Wallet> optionalWallet = walletRepository.findWalletByAdvertiserId(advertiserId);
        if (optionalWallet.isPresent()) {
            return optionalWallet.get();
        }

        throw new WalletServiceException("Wallet not found");
    }

    @Override
    @Transactional
    public Wallet fundWallet(FundWalletRequest request) {
        Advertiser advertiser =  advertiserService.findByAdvertiserId(request.getAdvertiserId()).orElseThrow(()-> new WalletServiceException("Advertiser not found"));

        Wallet wallet = getWalletByAdvertiserId(advertiser.getId());
        wallet.setBalance(wallet.getBalance().add(request.getAmount()));

        BigDecimal pointsToAdd = request.getAmount().divide(POINT_VALUE_NAIRA, MathContext.DECIMAL64);
        wallet.setPointsBalance(wallet.getPointsBalance().add(pointsToAdd));


        Transaction transaction =  createWalletFundingTransaction(request.getAmount(), advertiser);
        wallet.getTransactions().add(transaction);

        //Send Email for wallet funding
//        emailService.sendWalletFundingNotificationToAdvertiser(advertiser,request.getAmount().doubleValue(),transaction.getTransactionId());

        return  walletRepository.save(wallet);
    }

    @Override
    public BigDecimal getTotalWalletPoints() {
        return walletRepository.getTotalWalletPoints();
    }

    private Transaction createWalletFundingTransaction(BigDecimal amount, Advertiser advertiser){
        Transaction transaction = new Transaction();
        transaction.setAdvertiser(advertiser);
        transaction.setTransactionDate(new Date());
        transaction.setTransactionType("CREDIT");
        transaction.setAmount(amount);
//        transaction.setCostPerSms(SMS_CPA);
//        transaction.setPointAssigned(amount.multiply(BigDecimal.valueOf(SMS_CPA)).doubleValue());
        transaction.setTransactionId(appUtils.generateTransactionId());

        BigDecimal pointsAssigned = amount.divide(POINT_VALUE_NAIRA, MathContext.DECIMAL64); // DECIMAL64 for standard precision
        transaction.setPointAssigned(pointsAssigned.doubleValue());
        return transactionRepository.save(transaction);

    }
    private Advertiser getCurrentAdvertiser(){
        String advertiserEmail = "";
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            advertiserEmail =  userDetails.getEmail();
        }
        return advertiserService.findByEmail(advertiserEmail).get();
    }
}
