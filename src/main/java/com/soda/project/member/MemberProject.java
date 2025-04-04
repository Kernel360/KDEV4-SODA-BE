package com.soda.project.member;

import com.soda.common.BaseEntity;
import com.soda.member.Member;
import com.soda.member.MemberProjectRole;
import com.soda.project.Project;

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

    public static MemberProject createDevManager(Member member, Project project) {
        return MemberProject.builder()
                .member(member)
                .project(project)
                .memberProjectRole(MemberProjectRole.DEV_MANAGER)
                .build();
    }

    public static MemberProject createDevMember(Member member, Project project) {
        return MemberProject.builder()
                .member(member)
                .project(project)
                .memberProjectRole(MemberProjectRole.DEV_PARTICIPANT)
                .build();
    }

    public static MemberProject createClientManager(Member member, Project project) {
        return MemberProject.builder()
                .member(member)
                .project(project)
                .memberProjectRole(MemberProjectRole.CLI_MANAGER)
                .build();
    }

    public static MemberProject createClientMember(Member member, Project project) {
        return MemberProject.builder()
                .member(member)
                .project(project)
                .memberProjectRole(MemberProjectRole.CLI_PARTICIPANT)
                .build();
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
