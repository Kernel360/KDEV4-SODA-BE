package com.soda.request.entity;

import com.soda.common.BaseEntity;
import com.soda.common.TrackUpdate;
import com.soda.member.entity.Member;
import com.soda.project.entity.Task;
import com.soda.request.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.*;

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
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL)
    private List<RequestFile> files;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL)
    private List<RequestLink> links;

    @Builder
    public Request(Member member, Task task, String title, String content, RequestStatus status, List<RequestFile> files, List<RequestLink> links) {
        this.member = member;
        this.task = task;
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
