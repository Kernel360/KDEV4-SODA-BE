package com.soda.request.entity;

import com.soda.common.file.model.FileBase;
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

    @Override
    public Long getDomainId() {
        return request.getId();
    }
}
