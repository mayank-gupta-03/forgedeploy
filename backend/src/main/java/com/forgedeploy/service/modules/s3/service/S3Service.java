package com.forgedeploy.service.modules.s3.service;

import com.forgedeploy.service.common.exception.StorageException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Client s3Client;

    @Value("${storage.s3.bucket-name}")
    private String bucketName;

    private static final String SOURCE_KEY_PATTERN = "projects/%s/deployments/%s/source.zip";
    private static final String ARTIFACTS_KEY_PATTERN = "projects/%s/deployments/%s/artifacts/";

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
            throw new StorageException("Failed to initialize S3 storage", e);
        }
    }

    public void uploadFile(String key, Path filePath) {
        log.info("Uploading file to S3: {}/{}", bucketName, key);
        String mimeType = null;
        try {
            mimeType = Files.probeContentType(filePath);

            if (mimeType == null || mimeType.isBlank()) {
                mimeType = getFallbackMimeType(filePath.toString());
            }
        } catch (IOException e) {
            throw new StorageException("Unable to determine MIME type for key: " + key, e);
        }
        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType(mimeType)
                            .build(),
                    filePath);
            log.info("Successfully uploaded file to S3: {}", key);
        } catch (Exception e) {
            log.error("Failed to upload file to S3: {}/{}", bucketName, key, e);
            throw new StorageException("S3 upload failed for key: " + key, e);
        }
    }

    public void uploadInputStream(String key, InputStream inputStream, long contentLength, String contentType) {
        log.info("Uploading stream to S3: {}/{} ({} bytes)", bucketName, key, contentLength);
        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromInputStream(inputStream, contentLength));
            log.info("Successfully uploaded stream to S3: {}", key);
        } catch (Exception e) {
            log.error("Failed to upload stream to S3: {}/{}", bucketName, key, e);
            throw new StorageException("S3 stream upload failed for key: " + key, e);
        }
    }

    public void uploadDirectory(String s3Prefix, Path sourceFolder) {
        log.info("Recursively uploading directory {} to S3 prefix {}", sourceFolder, s3Prefix);
        if (!Files.isDirectory(sourceFolder)) {
            throw new IllegalArgumentException("Provided path is not a directory: " + sourceFolder);
        }

        try (Stream<Path> paths = Files.walk(sourceFolder)) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                String relativePath = sourceFolder.relativize(path).toString();
                // Construct S3 key: prefix + relative path
                String s3Key = s3Prefix.endsWith("/") ? s3Prefix + relativePath : s3Prefix + "/" + relativePath;
                uploadFile(s3Key, path);
            });
        } catch (IOException e) {
            log.error("Failed to walk directory for S3 upload: {}", sourceFolder, e);
            throw new StorageException("Failed to upload directory to S3", e);
        }
    }

    public InputStream downloadFile(String key) {
        log.info("Downloading file from S3: {}/{}", bucketName, key);
        try {
            return s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
        } catch (NoSuchKeyException e) {
            log.error("File not found in S3: {}/{}", bucketName, key);
            throw new StorageException("File not found in storage: " + key, e);
        } catch (Exception e) {
            log.error("Failed to download file from S3: {}/{}", bucketName, key, e);
            throw new StorageException("S3 download failed for key: " + key, e);
        }
    }

    public void deleteFile(String key) {
        log.info("Deleting file from S3: {}/{}", bucketName, key);
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
            log.info("Successfully deleted file from S3: {}", key);
        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}/{}", bucketName, key, e);
            throw new StorageException("S3 deletion failed for key: " + key, e);
        }
    }

    public String generateSourceKey(UUID projectId, UUID deploymentId) {
        return String.format(SOURCE_KEY_PATTERN, projectId, deploymentId);
    }

    public String generateArtifactsKey(UUID projectId, UUID deploymentId) {
        return String.format(ARTIFACTS_KEY_PATTERN, projectId, deploymentId);
    }

    private String getFallbackMimeType(String path) {
        String lowerFilename = path.toLowerCase();

        if (lowerFilename.endsWith(".html") || lowerFilename.endsWith(".htm")) {
            return "text/html";
        } else if (lowerFilename.endsWith(".css")) {
            return "text/css";
        } else if (lowerFilename.endsWith(".js")) {
            return "application/javascript";
        } else if (lowerFilename.endsWith(".svg")) {
            return "image/svg+xml";
        }

        return "application/octet-stream";
    }
}
