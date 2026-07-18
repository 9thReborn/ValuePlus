package com.nitax.valueplusbackend.service.impl;

import com.nitax.valueplusbackend.config.JwtUtils;
import com.nitax.valueplusbackend.domain.AppRoles;
import com.nitax.valueplusbackend.domain.Publisher;
import com.nitax.valueplusbackend.domain.PublisherStatus;
import com.nitax.valueplusbackend.dto.request.SignInDTO;
import com.nitax.valueplusbackend.dto.request.PublisherSignUpRequest;
import com.nitax.valueplusbackend.dto.response.ApiResponse;
import com.nitax.valueplusbackend.dto.response.LoginResponse;
import com.nitax.valueplusbackend.exception.AppException;
import com.nitax.valueplusbackend.repository.RoleRepository;
import com.nitax.valueplusbackend.service.EmailService;
import com.nitax.valueplusbackend.service.PublisherAuthenticationService;
import com.nitax.valueplusbackend.service.PublisherService;
import com.nitax.valueplusbackend.utils.AppUtils;
import com.nitax.valueplusbackend.utils.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublisherAuthenticationServiceImpl implements PublisherAuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final PublisherService publisherService;
    private final PasswordEncoder encoder;
    private final EmailService emailService;
    private final AppUtils appUtils;
    private final RoleRepository roleRepository;


    @Value("${app.frontend-server-url}")
    private String frontendServerUrl;
    @Override
    public ApiResponse<LoginResponse> login(SignInDTO signInDTO) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                signInDTO.getEmail(), signInDTO.getPassword()));

        String jwt = jwtUtils.generateJwtToken(authentication);
        String refreshToken =  jwtUtils.generateRefreshToken(authentication);

        LoginResponse loginResponse = new LoginResponse(jwt,refreshToken);
        return ApiResponse.<LoginResponse>builder().success(true).data(loginResponse).build();
    }

    @Override
    public String registerPublisher(PublisherSignUpRequest publisherSignUpRequest, MultipartFile signedIoForm) {

        publisherService.emailExist(publisherSignUpRequest.getEmail());

        Publisher publisher = new Publisher();
        publisher.setName(publisherSignUpRequest.getName());
        publisher.setBusinessName(publisherSignUpRequest.getBusinessName());
        publisher.setPassword(encoder.encode(publisherSignUpRequest.getPassword()));
        publisher.setEmail(publisherSignUpRequest.getEmail());
        publisher.setSkypeId(publisherSignUpRequest.getSkypeId());
        publisher.setStatus(PublisherStatus.UNVERIFIED);
        publisher.setPubId(appUtils.generatePubId());
        publisher.setPubUrl(publisherSignUpRequest.getPubUrl());
        publisher.setRole(setPublisherRole());
        publisher.setPostbackUrl(appUtils.generatePublisherPostbackUrl(publisher.getPubId()));
        publisherService.savePublisher(publisher);

        String authToken = jwtUtils.generateJwtToken(publisher);
        String verifyUrl = frontendServerUrl + "?token=" + authToken;
        emailService.sendPublisherVerificationMail(publisher, verifyUrl);
        return authToken;
    }

    private AppRoles setPublisherRole(){
        return roleRepository.findByName(Role.PUBLISHER).orElseGet(() -> {
            AppRoles appRoles = new AppRoles();
            appRoles.setName(Role.PUBLISHER);
            return roleRepository.save(appRoles);
        });
    }
    @Override
    public String verifySignupToken(String token) {
        boolean isTokenValid = jwtUtils.validateJwtToken(token);

        if (!isTokenValid) {
            throw new AppException(
                    "Cannot verify signup, please request for a new verification or contact support.");
        }

        String email = jwtUtils.getUserNameFromJwtToken(token);

        Publisher publisher = publisherService.findByEmail(email);

        if (publisher.getIsEmailVerified()) {
            throw new AppException("Email already verified");
        }

        publisher.setIsEmailVerified(true);
        publisher.setStatus(PublisherStatus.AWAIT_APPROVAL);
        emailService.sendNotificationToAdmin(publisher);
        emailService.sendNotificationToPublisher(publisher);

        publisherService.savePublisher(publisher);

        return token;
    }

}
