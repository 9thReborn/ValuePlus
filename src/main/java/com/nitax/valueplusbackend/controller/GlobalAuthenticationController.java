package com.nitax.valueplusbackend.controller;

import com.nitax.valueplusbackend.dto.request.AdvertiserSignInDTO;
import com.nitax.valueplusbackend.dto.request.SignInDTO;
import com.nitax.valueplusbackend.dto.response.ApiResponse;
import com.nitax.valueplusbackend.dto.response.LoginResponse;
import com.nitax.valueplusbackend.service.GlobalAuthenticationService;
import com.nitax.valueplusbackend.service.PublisherAuthenticationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class GlobalAuthenticationController {
    private final GlobalAuthenticationService globalAuthenticationService;

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody SignInDTO signInDTO) {
        ApiResponse<LoginResponse> response = globalAuthenticationService.login(signInDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/verify/{token}")
    public ResponseEntity<ApiResponse<String>> verifySignupToken(@PathVariable String token) {
        String authToken = globalAuthenticationService.verifySignupToken(token);

        ApiResponse<String> response =
                ApiResponse.<String>builder().success(true).data(authToken).build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
