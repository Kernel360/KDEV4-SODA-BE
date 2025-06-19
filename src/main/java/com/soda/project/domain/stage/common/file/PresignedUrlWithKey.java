package com.soda.project.domain.stage.common.file;

import lombok.Getter;

@Getter
public class PresignedUrlWithKey {
    private final String key;
    private final String url;

    public PresignedUrlWithKey(String key, String url) {
        this.key = key;
        this.url = url;
    }
}
