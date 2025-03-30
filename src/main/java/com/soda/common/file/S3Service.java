package com.soda.common.file;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.soda.global.response.CommonErrorCode;
import com.soda.global.response.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.cloudfront.domain}")
    private String cloudFrontDomain;

    public String uploadFile(MultipartFile file) {
        return uploadFileToS3(file);
    }

    public List<String> uploadFiles(List<MultipartFile> files) {
        List<String> fileUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                fileUrls.add(uploadFileToS3(file));
            }
        }

        return fileUrls;
    }

    private String uploadFileToS3(MultipartFile file) {
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            PutObjectRequest putRequest = new PutObjectRequest(bucketName, fileName, file.getInputStream(), metadata);
            s3Client.putObject(putRequest);

            return cloudFrontDomain + "/" + fileName;
        } catch (IOException e) {
            throw new GeneralException(CommonErrorCode.S3_UPLOAD_ERROR);
        }
    }
}