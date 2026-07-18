package com.nitax.valueplusbackend.service.impl;

import com.nitax.valueplusbackend.config.DoConfig;
import com.nitax.valueplusbackend.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class ImageStorageServiceImpl implements ImageStorageService {

    private final S3Client s3Client;
    private final DoConfig doConfig;
    @Value("${aws.s3.bucketName}")
    private String awsBucketName;
    @Value("${aws.s3.folder}")
    private String folderName;

    @Override
    public String saveFile(MultipartFile multipartFile) throws IOException {
        String extension = FilenameUtils.getExtension(multipartFile.getOriginalFilename());
        String imgName = FilenameUtils.removeExtension(multipartFile.getOriginalFilename());
        String key = folderName + "/" + new Date().getTime() + "-" + imgName + "." + extension;
        return saveImageToServer(multipartFile, key);
    }

    private String saveImageToServer(MultipartFile multipartFile, String key) throws IOException {

        if (multipartFile.getContentType() != null && !"".equals(multipartFile.getContentType())) {
            if (!bucketExists(awsBucketName)) {
                createBucket(awsBucketName);
            }

            // Prepare PutObjectRequest
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(awsBucketName)
                    .key(key)
                    .contentType(multipartFile.getContentType())
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            // Upload the file
            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize()));
            return doConfig.getAwsBucketUrl() + "/" + key;
        } else {
            return "https://fastly.picsum.photos/id/612/200/200.jpg?hmac=HbIkwJ0QBqhSlGTi3bnF4JFTp9BntF-teQZUQhpqWyM";
        }


    }

    private void createBucket(String bucketName) {
        s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
    }

    private boolean bucketExists(String bucketName) {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            return true;
        } catch (NoSuchBucketException e) {
            return false;
        }
    }


}
