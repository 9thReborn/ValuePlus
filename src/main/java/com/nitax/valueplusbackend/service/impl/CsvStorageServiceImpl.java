package com.nitax.valueplusbackend.service.impl;

import com.nitax.valueplusbackend.config.DoConfig;
import com.nitax.valueplusbackend.service.CsvStorageService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class CsvStorageServiceImpl implements CsvStorageService {

    private final S3Client s3Client;
    private final DoConfig doConfig;

    @Value("${aws.s3.bucketName}")
    private String awsBucketName;

    @Value("${aws.s3.csvFolder}")
    private String csvFolderName;

    @Override
    public String uploadCsvFile(MultipartFile file) throws IOException {
        // Validate file type
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (!"csv".equalsIgnoreCase(extension)) {
            throw new IllegalArgumentException("Only CSV files are allowed.");
        }

        // Generate unique file name
        String fileName = FilenameUtils.removeExtension(file.getOriginalFilename());
        String key = csvFolderName + "/" + new Date().getTime() + "-" + fileName + "." + extension;

        // Upload file to S3
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(awsBucketName)
                .key(key)
                .contentType("text/csv")
                .acl(ObjectCannedACL.PUBLIC_READ)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        // Return the public URL of the uploaded file
        return doConfig.getAwsBucketUrl() + "/" + key;
    }

    @Override
    public String uploadCsvBytes(byte[] bytes, String fileName) throws IOException {
        String key = csvFolderName + "/" + new Date().getTime() + "-" + fileName + ".csv";

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(awsBucketName)
                .key(key)
                .contentType("text/csv")
                .acl(ObjectCannedACL.PUBLIC_READ)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));

        return doConfig.getAwsBucketUrl() + "/" + key;
    }
}