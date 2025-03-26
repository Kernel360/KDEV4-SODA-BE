package com.soda.request.entity;

import com.soda.common.BaseEntity;
import com.soda.member.entity.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Rejection extends BaseEntity {

    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private Request request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "rejection", cascade = CascadeType.ALL)
    private List<RejectionFile> files;

    @OneToMany(mappedBy = "rejection", cascade = CascadeType.ALL)
    private List<RejectionLink> links;

    @Builder
    public Rejection(Member member, String comment, Request request, List<RejectionFile> files, List<RejectionLink> links) {
        this.member = member;
        this.comment = comment;
        this.request = request;
        this.files = files;
        this.links = links;
    }
}
