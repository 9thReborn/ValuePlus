package com.nitax.valueplusbackend.controller.external;

import com.nitax.valueplusbackend.dto.request.SignInDTO;
import com.nitax.valueplusbackend.dto.response.ApiResponse;
import com.nitax.valueplusbackend.dto.response.LoginResponse;
import com.nitax.valueplusbackend.service.GlobalAuthenticationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class ExternalAuthApiController {
    private final GlobalAuthenticationService globalAuthenticationService;

    @PostMapping("/authenticate")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody SignInDTO signInDTO) {
        ApiResponse<LoginResponse> response = globalAuthenticationService.login(signInDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
