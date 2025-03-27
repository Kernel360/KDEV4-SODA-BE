package com.soda.request.entity;

import com.soda.common.link.LinkBase;
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
public class RequestLink extends LinkBase {

    private String urlAddress;

    private String urlDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    @Builder
    public RequestLink(String urlAddress, String urlDescription, Request request) {
        this.urlAddress = urlAddress;
        this.urlDescription = urlDescription;
        this.request = request;
    }
}
