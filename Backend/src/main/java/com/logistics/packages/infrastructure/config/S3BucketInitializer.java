package com.logistics.packages.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3BucketInitializer implements ApplicationRunner {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.auto-create-bucket:false}")
    private boolean autoCreateBucket;

    @Override
    public void run(ApplicationArguments args) {
        if (!autoCreateBucket) {
            return;
        }
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            log.info("S3 bucket '{}' already exists", bucketName);
        } catch (NoSuchBucketException e) {
            crearBucket(bucketName);
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                crearBucket(bucketName);
            } else {
                log.warn("Could not verify S3 bucket '{}': {} (status {})",
                        bucketName, e.awsErrorDetails().errorMessage(), e.statusCode());
            }
        }
    }

    private void crearBucket(String bucketName) {
        try {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
            log.info("S3 bucket '{}' created successfully", bucketName);
        } catch (S3Exception e) {
            if ("BucketAlreadyOwnedByYou".equals(e.awsErrorDetails().errorCode())
                    || "BucketAlreadyExists".equals(e.awsErrorDetails().errorCode())) {
                log.info("S3 bucket '{}' already exists (created by another process)", bucketName);
            } else {
                log.warn("Could not create S3 bucket '{}': {} (status {})",
                        bucketName, e.awsErrorDetails().errorMessage(), e.statusCode());
            }
        }
    }
}
