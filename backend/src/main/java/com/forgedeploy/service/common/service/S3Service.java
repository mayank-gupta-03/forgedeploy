package com.forgedeploy.service.common.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Client s3Client;

    @Value("${storage.s3.bucket-name}")
    private String bucketName;

    @PostConstruct
    public void init() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            log.info("S3 bucket '{}' already exists", bucketName);
        } catch (NoSuchBucketException e) {
            log.info("S3 bucket '{}' does not exist, creating it...", bucketName);
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
            log.info("S3 bucket '{}' created successfully", bucketName);
        } catch (Exception e) {
            log.error("Could not initialize S3 bucket: {}", bucketName, e);
        }
    }

    public void uploadFile(String key, Path filePath) {
        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build(),
                    filePath);
            log.info("Uploaded file to S3: {}/{}", bucketName, key);
        } catch (Exception e) {
            log.error("Failed to upload file to S3: {}/{}", bucketName, key, e);
            throw new RuntimeException("S3 upload failed", e);
        }
    }

    public void uploadInputStream(String key, InputStream inputStream, long contentLength, String contentType) {
        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromInputStream(inputStream, contentLength));
            log.info("Uploaded stream to S3: {}/{}", bucketName, key);
        } catch (Exception e) {
            log.error("Failed to upload stream to S3: {}/{}", bucketName, key, e);
            throw new RuntimeException("S3 upload failed", e);
        }
    }

    public InputStream downloadFile(String key) {
        try {
            return s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
        } catch (Exception e) {
            log.error("Failed to download file from S3: {}/{}", bucketName, key, e);
            throw new RuntimeException("S3 download failed", e);
        }
    }

    public void deleteFile(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
            log.info("Deleted file from S3: {}/{}", bucketName, key);
        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}/{}", bucketName, key, e);
        }
    }
}
