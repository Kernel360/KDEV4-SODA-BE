package com.soda.project.domain.stage.request.response;

import com.soda.common.BaseEntity;
import com.soda.member.entity.Member;
import com.soda.project.domain.stage.request.Request;
import com.soda.project.domain.stage.request.response.enums.ResponseStatus;
import com.soda.project.domain.stage.request.response.file.ResponseFile;
import com.soda.project.domain.stage.request.response.link.ResponseLink;
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

    @Enumerated(EnumType.STRING)
    private ResponseStatus status;

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
    public Response(Member member, String comment, ResponseStatus status, Request request, List<ResponseFile> files, List<ResponseLink> links) {
        this.member = member;
        this.comment = comment;
        this.status = status;
        this.request = request;
        this.files = files;
        this.links = links;
    }

    protected static Response createApprove(Member member, Request request, String comment, List<ResponseLink> links) {
        Response approval = Response.builder()
                .member(member)
                .request(request)
                .comment(comment)
                .status(ResponseStatus.APPROVED)
                .build();

        approval.addLinks(links);

        approval.approveApproverRequest(request);

        return approval;
    }

    private void approveApproverRequest(Request request) {
        if (request.isOneRemainUntilApproved(request)) {
            request.approved();
        } else {
            request.approving();
        }
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
