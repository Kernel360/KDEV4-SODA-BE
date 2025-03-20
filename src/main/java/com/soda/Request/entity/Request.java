package com.soda.Request.entity;

import com.soda.common.BaseEntity;
import com.soda.member.entity.Member;
import com.soda.project.entity.Task;
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
