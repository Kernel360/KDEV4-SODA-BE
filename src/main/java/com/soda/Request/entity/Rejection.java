package com.soda.Request.entity;

import com.soda.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;

@Entity
@Getter
public class Rejection extends BaseEntity {

    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private Request request;

    @OneToMany(mappedBy = "rejection", cascade = CascadeType.ALL)
    private List<RejectionFile> files;

    @OneToMany(mappedBy = "rejection", cascade = CascadeType.ALL)
    private List<RejectionLink> links;
}
