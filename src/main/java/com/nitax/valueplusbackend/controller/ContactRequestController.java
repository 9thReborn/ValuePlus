package com.nitax.valueplusbackend.controller;

import com.nitax.valueplusbackend.dto.request.ContactRequestDto;
import com.nitax.valueplusbackend.dto.response.ApiResponse;
import com.nitax.valueplusbackend.service.AdvertiserService;
import com.nitax.valueplusbackend.service.ContactRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/contactRequest")
@CrossOrigin
@Slf4j
public class ContactRequestController {
    private final ContactRequestService service;

    // method for advertisers to contact us
    @PostMapping("/contactUs")
    public ResponseEntity<ApiResponse<String>> contactUs(
            @Valid @RequestBody ContactRequestDto contactRequest) {
        boolean isCompleted = service.createContactAndSendMail(contactRequest);
        ApiResponse<String> response =
                ApiResponse.<String>builder().success(true).data(Boolean.TRUE.equals(isCompleted) ? "Request Sent" : "Failed").build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
