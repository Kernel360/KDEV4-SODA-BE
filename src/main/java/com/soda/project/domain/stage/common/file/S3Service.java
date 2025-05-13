package com.soda.project.domain.stage.common.file;

import com.soda.global.config.AWSS3Config;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    public static final String S3_URL_PREFIX = "uploads/";

    private final AWSS3Config awsS3Config;

    public String generatePresignedPutUrl(String fileName, String contentType) {
        S3Presigner presigner = awsS3Config.getPresigner();

        String uniqueFileName = UUID.randomUUID() + "_" + fileName;

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(awsS3Config.getBucket())
                .key(S3_URL_PREFIX + uniqueFileName)
                .contentType(contentType)
                .build();

        PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(builder ->
                builder.signatureDuration(Duration.ofMinutes(10))
                        .putObjectRequest(objectRequest)
        );

        return presignedRequest.url().toString();
    }

    public String buildCloudFrontUrl(String fileName) {
        return awsS3Config.getCloudFrontDomain() + "/" + S3_URL_PREFIX + fileName;
    }
}