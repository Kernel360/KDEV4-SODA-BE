package com.soda.project.entity;

import com.soda.common.BaseEntity;
import com.soda.member.entity.Member;
import com.soda.member.enums.MemberProjectRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class MemberProject extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Enumerated(EnumType.STRING)
    private MemberProjectRole role;

    @Builder
    public MemberProject(Member member, Project project, MemberProjectRole memberProjectRole) {
        this.member = member;
        this.project = project;
        this.role = memberProjectRole;
    }

    public void delete() {
        this.markAsDeleted();
    }

    public void reActive() {
        this.markAsActive();
    }

    public void updateMemberProject(Member member, Project project, MemberProjectRole memberProjectRole) {
        this.member = member;
        this.project = project;
        this.role = memberProjectRole;
    }
}
