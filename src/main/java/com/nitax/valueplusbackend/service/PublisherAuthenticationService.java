package com.nitax.valueplusbackend.service;

import com.nitax.valueplusbackend.dto.request.SignInDTO;
import com.nitax.valueplusbackend.dto.request.PublisherSignUpRequest;
import com.nitax.valueplusbackend.dto.response.ApiResponse;
import com.nitax.valueplusbackend.dto.response.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

public interface PublisherAuthenticationService {
    ApiResponse<LoginResponse> login(SignInDTO signInDTO);
    String registerPublisher(@Valid PublisherSignUpRequest signUpRequest, MultipartFile signedIoForm);

    String verifySignupToken(String token);
}
