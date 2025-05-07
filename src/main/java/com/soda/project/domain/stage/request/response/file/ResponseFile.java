package com.soda.project.domain.stage.request.response.file;

import com.soda.project.domain.stage.common.file.FileBase;
import com.soda.project.domain.stage.request.response.Response;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResponseFile extends FileBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id", nullable = false)
    private Response response;

    @Builder
    public ResponseFile(String name, String url, Response response) {
        this.name = name;
        this.url = url;
        this.response = response;
    }

    public static ResponseFile create(String fileName, String url, Response response) {
        return ResponseFile.builder()
                .name(fileName)
                .url(url)
                .response(response)
                .build();
    }

    @Override
    public Long getDomainId() {
        return response.getId();
    }
}
