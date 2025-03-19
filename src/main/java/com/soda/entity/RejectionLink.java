package com.soda.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class RejectionLink extends BaseEntity {
    private String urlAddress;

    private String urlDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rejection_id", nullable = false)
    private Rejection rejection;
}
