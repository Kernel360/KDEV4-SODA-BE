package com.soda.project.domain.stage.request.link;

import com.soda.project.domain.stage.common.link.LinkBase;
import com.soda.project.domain.stage.request.Request;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    @Builder
    public RequestLink(String urlAddress, String urlDescription, Request request) {
        this.urlAddress = urlAddress;
        this.urlDescription = urlDescription;
        this.request = request;
    }

    public static RequestLink create(String urlAddress, String urlDescription, Request request) {
        return RequestLink.builder()
                .urlAddress(urlAddress)
                .urlDescription(urlDescription)
                .request(request)
                .build();
    }

    public void updateRequest(Request request) {
        this.request = request;
    }

    @Override
    public Long getDomainId() {
        return request.getId();
    }
}
