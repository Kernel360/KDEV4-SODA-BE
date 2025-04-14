package com.soda.request.entity;

import com.soda.common.BaseEntity;
import com.soda.common.TrackUpdate;
import com.soda.member.entity.Member;
import com.soda.project.entity.Stage;
import com.soda.request.enums.RequestStatus;
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

    @TrackUpdate
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL)
    private List<RequestFile> files;

    @TrackUpdate
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL)
    private List<RequestLink> links;

    @Builder
    public Request(Member member, Stage stage, String title, String content, RequestStatus status, List<RequestFile> files, List<RequestLink> links) {
        this.member = member;
        this.stage = stage;
        this.title = title;
        this.status = status;
        this.content = content;
        this.files = files;
        this.links = links;
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

    public void approve() {
        this.status = RequestStatus.APPROVED;
    }

    public void reject() {
        this.status = RequestStatus.REJECTED;
    }
}
