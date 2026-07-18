package com.nitax.valueplusbackend.service.impl;

import com.nitax.valueplusbackend.domain.ContactRequest;
import com.nitax.valueplusbackend.dto.request.ContactRequestDto;
import com.nitax.valueplusbackend.repository.ContactRequestRepository;
import com.nitax.valueplusbackend.service.ContactRequestService;
import com.nitax.valueplusbackend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactRequestServiceImpl implements ContactRequestService {
    private final ContactRequestRepository contactRequestRepository;
    private final EmailService emailService;

    @Override
    public Boolean createContactAndSendMail(ContactRequestDto request) {
        var contactRequest = new ContactRequest();
        if (request != null) {
            contactRequest.setName(request.getName());
            contactRequest.setEmail(request.getEmail());
            contactRequest.setCompany(request.getCompany());
            contactRequest.setSkypeId(request.getSkypeId());
            contactRequest.setIndustryName(request.getIndustryName());
            contactRequest.setCreatedDate(Instant.now());
            //save to repository
            this.contactRequestRepository.save(contactRequest);
            // send email : we want
            this.emailService.sendContactRequest(request);
            return true;
        } else {
            return false;
        }
    }
}
