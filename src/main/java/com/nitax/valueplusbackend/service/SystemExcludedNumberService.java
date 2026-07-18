package com.nitax.valueplusbackend.service;

import com.nitax.valueplusbackend.domain.Admin;
import com.nitax.valueplusbackend.domain.SystemExcludedNumber;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface SystemExcludedNumberService {
    List<SystemExcludedNumber> findAll();
    List<SystemExcludedNumber> addExcludedNumbers(List<String> numbers, MultipartFile file, Admin admin) throws IOException;

    String getAllExcludedNumbers();

    void removeExcludedNumber(Long id);
}
