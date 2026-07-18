package com.nitax.valueplusbackend.service;

import com.nitax.valueplusbackend.dto.request.SignInDTO;
import com.nitax.valueplusbackend.dto.response.ApiResponse;
import com.nitax.valueplusbackend.dto.response.LoginResponse;

public interface GlobalAuthenticationService {
    ApiResponse<LoginResponse> login(SignInDTO loginRequest);
    String verifySignupToken(String token);
}
