package com.nitax.valueplusbackend.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CsvStorageService {
    String uploadCsvFile(MultipartFile file) throws IOException;
    String uploadCsvBytes(byte[] bytes, String fileName) throws IOException;
}