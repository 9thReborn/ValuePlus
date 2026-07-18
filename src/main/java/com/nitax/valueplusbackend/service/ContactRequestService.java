package com.nitax.valueplusbackend.service;

import com.nitax.valueplusbackend.dto.request.ContactRequestDto;

public interface ContactRequestService {
    Boolean createContactAndSendMail(ContactRequestDto request);
}
