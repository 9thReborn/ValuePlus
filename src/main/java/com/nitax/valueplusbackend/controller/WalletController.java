package com.nitax.valueplusbackend.controller;

import com.nitax.valueplusbackend.dto.request.FundWalletRequest;
import com.nitax.valueplusbackend.dto.response.ApiResponse;
import com.nitax.valueplusbackend.dto.response.WalletDetailsResponse;
import com.nitax.valueplusbackend.service.WalletService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallet")
@AllArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @GetMapping("/")
    public ResponseEntity<?> getAdvertiserWalletDetails(){
        WalletDetailsResponse response =  walletService.getAdvertiserWallet();
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .success(true)
                .data(response)
                .build();
        return ResponseEntity.ok().body(apiResponse);
    }

    @PostMapping("/fund")
    public ResponseEntity<?> fundWallet(@RequestBody FundWalletRequest request) {
        walletService.fundWallet(request);
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .success(true)
                .data("Wallet funded successfully")
                .build();
        return ResponseEntity.ok().body(apiResponse);
    }
}
