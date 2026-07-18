package com.nitax.valueplusbackend.service.impl;

import com.nitax.valueplusbackend.config.JwtUtils;
import com.nitax.valueplusbackend.domain.Advertiser;
import com.nitax.valueplusbackend.domain.AdvertiserStatus;
import com.nitax.valueplusbackend.domain.Publisher;
import com.nitax.valueplusbackend.domain.PublisherStatus;
import com.nitax.valueplusbackend.dto.request.SignInDTO;
import com.nitax.valueplusbackend.dto.response.ApiResponse;
import com.nitax.valueplusbackend.dto.response.LoginResponse;
import com.nitax.valueplusbackend.exception.AppException;
import com.nitax.valueplusbackend.repository.AdvertiserRepository;
import com.nitax.valueplusbackend.repository.PublisherRepository;
import com.nitax.valueplusbackend.service.EmailService;
import com.nitax.valueplusbackend.service.GlobalAuthenticationService;
import com.nitax.valueplusbackend.service.PublisherService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GlobalAuthenticationServiceImpl implements GlobalAuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final PublisherRepository publisherService;
    private final AdvertiserRepository advertiserService;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;

    @Override
    public ApiResponse<LoginResponse> login(SignInDTO loginRequest) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.getEmail(), loginRequest.getPassword()));

        String jwt = jwtUtils.generateJwtToken(authentication);
        String refreshToken =  jwtUtils.generateRefreshToken(authentication);

        LoginResponse loginResponse = new LoginResponse(jwt,refreshToken);
        return ApiResponse.<LoginResponse>builder().success(true).data(loginResponse).build();
    }

    @Override
    public String verifySignupToken(String token) {

        boolean isTokenValid = jwtUtils.validateJwtToken(token);

        if (!isTokenValid) {
            throw new AppException(
                    "Cannot verify signup, please request for a new verification or contact support.");
        }

        String email = jwtUtils.getUserNameFromJwtToken(token);


        Publisher publisher = publisherService.findByEmail(email).orElse(null);
        if (null != publisher ){
            if (publisher.getIsEmailVerified()) {
                throw new AppException("Email already verified");
            }

            publisher.setIsEmailVerified(true);
            publisher.setStatus(PublisherStatus.AWAIT_APPROVAL);
            emailService.sendNotificationToAdmin(publisher);
            emailService.sendNotificationToPublisher(publisher);

            publisherService.save(publisher);

            return token;
        }

        Advertiser advertiser = advertiserService.findByEmail(email).orElse(null);
        if (null != advertiser){
            if (advertiser.getIsEmailVerified()) {
                throw new AppException("Email already verified");
            }

            advertiser.setIsEmailVerified(true);
            advertiser.setStatus(AdvertiserStatus.AWAIT_APPROVAL);
            emailService.sendNotificationToAdmin(advertiser);
            emailService.sendNotificationToAdvertiser(advertiser);

            advertiserService.save(advertiser);

            return token;
        }

        throw new AppException("Can't verify user");

    }
}
