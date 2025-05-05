package com.soda.project.domain.stage.request;

import com.soda.common.BaseEntity;
import com.soda.common.TrackUpdate;
import com.soda.member.entity.Member;
import com.soda.project.domain.stage.Stage;
import com.soda.project.domain.stage.request.dto.ReRequestCreateRequest;
import com.soda.project.domain.stage.request.dto.RequestCreateRequest;
import com.soda.project.domain.stage.request.enums.RequestStatus;
import com.soda.project.domain.stage.request.file.RequestFile;
import com.soda.project.domain.stage.request.link.RequestLink;
import com.soda.project.domain.stage.request.response.Response;
import com.soda.project.domain.stage.request.response.enums.ResponseStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Request extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RequestStatus status;

    @TrackUpdate
    private String title;

    @TrackUpdate
    @Lob
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id")
    private Stage stage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private Long parentId;

    @TrackUpdate
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL)
    private List<RequestFile> files;

    @TrackUpdate
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL)
    private List<RequestLink> links;

    @TrackUpdate
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL)
    private List<ApproverDesignation> approvers;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL)
    private List<Response> responses;

    @Builder
    public Request(Member member, Stage stage, Long parentId, String title, String content, RequestStatus status, List<RequestFile> files, List<RequestLink> links) {
        this.member = member;
        this.stage = stage;
        this.parentId = parentId;
        this.title = title;
        this.status = status;
        this.content = content;
        this.files = files;
        this.links = links;
    }

    public static Request createRequest(Member member, Stage stage, RequestCreateRequest requestCreateRequest) {
        return Request.builder()
                .member(member)
                .stage(stage)
                .title(requestCreateRequest.getTitle())
                .content(requestCreateRequest.getContent())
                .status(RequestStatus.PENDING)
                .build();
    }

    public static Request createReRequest(Long requestId, Member member, Stage stage, ReRequestCreateRequest reRequestCreateRequest) {
        return Request.builder()
                .member(member)
                .stage(stage)
                .title(reRequestCreateRequest.getTitle())
                .content(reRequestCreateRequest.getContent())
                .parentId(requestId)
                .status(RequestStatus.PENDING)
                .build();
    }

    public void updateTitle(String title) {
        this.title = title;
    }
    public void updateContent(String content) {
        this.content = content;
    }
    public void addLinks(List<RequestLink> newLinks) {
        if (this.links == null) {
            this.links = new ArrayList<>();
        }
        this.links.addAll(newLinks);
    }
    public void addFiles(List<RequestFile> newFiles) {
        if (this.files == null) {
            this.files = new ArrayList<>();
        }
        this.files.addAll(newFiles);
    }

    public void delete() {
        markAsDeleted();
    }

    public void approved() {
        this.status = RequestStatus.APPROVED;
    }

    public void reject() {
        this.status = RequestStatus.REJECTED;
    }

    public void changeStatusToPending() {
        this.status = RequestStatus.PENDING;
    }

    public void addApprovers(List<ApproverDesignation> approverDesignations) {
        if (this.approvers == null) {
            this.approvers = new ArrayList<>();
        }
        this.approvers.addAll(approverDesignations);
    }

    public void approving() {
        this.status = RequestStatus.APPROVING;
    }

    public boolean isOneRemainUntilApproved(Request request) {
        return request.getResponses().stream()
                .filter(response ->
                        response.getStatus() == ResponseStatus.APPROVED && !response.getIsDeleted())
                .count() == request.getApprovers().size() - 1;
    }
}
