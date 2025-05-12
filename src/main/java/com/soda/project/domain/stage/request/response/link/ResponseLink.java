package com.soda.project.domain.stage.request.response.link;

import com.soda.project.domain.stage.common.link.LinkBase;
import com.soda.project.domain.stage.request.response.Response;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResponseLink extends LinkBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id", nullable = false)
    private Response response;

    @Builder
    private ResponseLink(String urlAddress, String urlDescription, Response response) {
        this.urlAddress = urlAddress;
        this.urlDescription = urlDescription;
        this.response = response;
    }

    public static ResponseLink create(String urlAddress, String urlDescription, Response response) {
        return ResponseLink.builder()
                .urlAddress(urlAddress)
                .urlDescription(urlDescription)
                .response(response)
                .build();
    }

    public void updateResponse(Response response) {
        this.response = response;
    }

    @Override
    public Long getDomainId() {
        return this.response.getId();
    }
}
