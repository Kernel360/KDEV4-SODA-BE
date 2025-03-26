package com.soda.request.entity;

import com.soda.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResponseLink extends BaseEntity {
    private String urlAddress;

    private String urlDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rejection_id", nullable = false)
    private Response response;

    @Builder
    private ResponseLink(String urlAddress, String urlDescription, Response response) {
        this.urlAddress = urlAddress;
        this.urlDescription = urlDescription;
        this.response = response;
    }
}
