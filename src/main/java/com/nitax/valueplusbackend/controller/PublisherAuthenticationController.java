package com.nitax.valueplusbackend.controller;

import com.nitax.valueplusbackend.dto.request.SignInDTO;
import com.nitax.valueplusbackend.dto.request.PublisherSignUpRequest;
import com.nitax.valueplusbackend.dto.response.ApiResponse;
import com.nitax.valueplusbackend.dto.response.LoginResponse;
import com.nitax.valueplusbackend.service.PublisherAuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/affiliates")
@CrossOrigin
@Slf4j
public class PublisherAuthenticationController {

    private final PublisherAuthenticationService publisherAuthenticationService;

    @PostMapping(value = "/signup", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<String>> createPublisher(
            @RequestPart("signedIoForm") MultipartFile signedIoForm,
            @ModelAttribute @Valid PublisherSignUpRequest publisherDTO) {
        String response = publisherAuthenticationService.registerPublisher(publisherDTO,signedIoForm);
        ApiResponse<String> apiResponse =
                ApiResponse.<String>builder().success(true).data(response).build();
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping("/verify/{token}")
    public ResponseEntity<ApiResponse<String>> verifySignupToken(@PathVariable String token) {
        String authToken = publisherAuthenticationService.verifySignupToken(token);

        ApiResponse<String> response =
                ApiResponse.<String>builder().success(true).data(authToken).build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody SignInDTO signInDTO) {
        ApiResponse<LoginResponse> response = publisherAuthenticationService.login(signInDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
