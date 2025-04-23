package com.soda.common.file.service;

import com.soda.global.config.AWSS3Config;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AWSS3Config awsS3Config;

    public String generatePresignedPutUrl(String fileName, String contentType) {
        S3Presigner presigner = awsS3Config.getPresigner();

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(awsS3Config.getBucket())
                .key("uploads/" + fileName)
                .contentType(contentType)
                .build();

        PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(builder ->
                builder.signatureDuration(Duration.ofMinutes(10))
                        .putObjectRequest(objectRequest)
        );

        return presignedRequest.url().toString();
    }

    public String buildCloudFrontUrl(String fileName) {
        return awsS3Config.getCloudFrontDomain() + "/uploads/" + fileName;
    }
}