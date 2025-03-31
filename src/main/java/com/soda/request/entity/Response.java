package com.soda.request.entity;

import com.soda.common.BaseEntity;
import com.soda.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Response extends BaseEntity {

    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private Request request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "response", cascade = CascadeType.ALL)
    private List<ResponseFile> files;

    @OneToMany(mappedBy = "response", cascade = CascadeType.ALL)
    private List<ResponseLink> links;

    @Builder
    public Response(Member member, String comment, Request request, List<ResponseFile> files, List<ResponseLink> links) {
        this.member = member;
        this.comment = comment;
        this.request = request;
        this.files = files;
        this.links = links;
    }

    public void updateLink(List<ResponseLink> links) {
        if (this.links == null) {
            this.links = new ArrayList<>();
        }
        this.links.addAll(links);
    }

    public void updateComment(String comment) {
        this.comment = comment;
    }

    public void delete() {
        markAsDeleted();
    }

    public void addLinks(List<ResponseLink> newLinks) {
        if ( this.links == null ) {
            this.links = new ArrayList<>();
        }
        for (ResponseLink link : newLinks) {
            link.updateResponse(this);
            this.links.add(link);
        }
    }
}
