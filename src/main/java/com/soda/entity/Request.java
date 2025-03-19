package com.soda.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;

@Entity
@Getter
public class Request extends BaseEntity {

    private Boolean isApproved;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL)
    private List<RequestFile> files;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL)
    private List<RequestLink> links;
}
