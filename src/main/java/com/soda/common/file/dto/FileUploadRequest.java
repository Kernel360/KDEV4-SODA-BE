package com.soda.common.file.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileUploadRequest {
    private String fileName;
    private String contentType;
}
