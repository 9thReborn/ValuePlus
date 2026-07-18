package com.nitax.valueplusbackend.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@Data
public class DoConfig {
  private static final String s3BucketPattern = "https://%s.s3.%s.amazonaws.com";

  @Value("${aws.s3.accessKey}")
  private String awsAccessKey;

  @Value("${aws.s3.accessSecret}")
  private String awsSecretKey;

  @Value("${aws.s3.region}")
  private String awsBucketRegion;

  @Value("${aws.s3.bucketName}")
  private String awsBucketName;

  @Bean
  public S3Client getS3(
      @Value("${aws.s3.accessKey}") String accessKey,
      @Value("${aws.s3.accessSecret}") String secretKey,
      @Value("${aws.s3.region}") String region) {
    return S3Client.builder()
        .region(Region.of(region))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)))
        .build();
  }

  public String getAwsBucketUrl() {
    return s3BucketPattern.formatted(awsBucketName, awsBucketRegion);
  }
}
