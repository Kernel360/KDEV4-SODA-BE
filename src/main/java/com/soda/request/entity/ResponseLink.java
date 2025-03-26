package com.soda.request.entity;

import com.soda.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class ResponseLink extends BaseEntity {
    private String urlAddress;

    private String urlDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rejection_id", nullable = false)
    private Response response;
}
