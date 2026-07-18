package com.nitax.valueplusbackend.service.impl;

import com.nitax.valueplusbackend.domain.Admin;
import com.nitax.valueplusbackend.domain.SystemExcludedNumber;
import com.nitax.valueplusbackend.repository.SystemExcludedNumberRepository;
import com.nitax.valueplusbackend.service.SystemExcludedNumberService;
import com.nitax.valueplusbackend.utils.CsvUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SystemExcludedNumberServiceImpl implements SystemExcludedNumberService {
    private final SystemExcludedNumberRepository repository;

    @Override
    public List<SystemExcludedNumber> findAll() {
        return repository.findAll();
    }

    @Override
    public List<SystemExcludedNumber> addExcludedNumbers(List<String> numbers, MultipartFile file, Admin admin) throws IOException {
        List<SystemExcludedNumber> list = new ArrayList<>();
        if (null != file){
            String pNumbers = CsvUtils.readPhoneNumbersFromCsv(file);
            String[] nums = pNumbers.split(",");
            for (String n : nums){
                SystemExcludedNumber systemExcludedNumber = new SystemExcludedNumber();
                systemExcludedNumber.setNumber(n);
                systemExcludedNumber.setAddedBy(admin.getEmail());
                list.add(systemExcludedNumber);
            }
            return repository.saveAll(list);
        }

        for (String number : numbers) {
            SystemExcludedNumber systemExcludedNumber = new SystemExcludedNumber();
            systemExcludedNumber.setNumber(number);
            systemExcludedNumber.setAddedBy(admin.getEmail());
            list.add(systemExcludedNumber);
        }
        return  repository.saveAll(list);
    }

    @Override
    public String getAllExcludedNumbers() {
        List<SystemExcludedNumber> excludedNumbers = findAll();
        return excludedNumbers.stream()
                .map(SystemExcludedNumber::getNumber)
                .collect(Collectors.joining(","));
    }

    @Override
    public void removeExcludedNumber(Long id) {
        repository.deleteById(id);
    }
}
