package com.soda.project.domain.stage.request.file;

import com.soda.project.domain.stage.common.file.FileBase;
import com.soda.project.domain.stage.request.Request;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RequestFile extends FileBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    @Builder
    public RequestFile(String name, String url, Request request) {
        this.name = name;
        this.url = url;
        this.request = request;
    }

    public static RequestFile create(String name, String url, Request request) {
        return RequestFile.builder()
                .name(name)
                .url(url)
                .request(request)
                .build();
    }

    @Override
    public Long getDomainId() {
        return request.getId();
    }
}
