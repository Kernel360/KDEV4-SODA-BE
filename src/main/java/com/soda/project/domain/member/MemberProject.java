package com.soda.project.domain.member;

import com.soda.common.BaseEntity;
import com.soda.member.domain.Member;
import com.soda.project.domain.Project;
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

    protected static MemberProject create(Member member, Project project, MemberProjectRole role) {
        return MemberProject.builder()
                .member(member)
                .project(project)
                .memberProjectRole(role)
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

    public void delete() {
        this.markAsDeleted();
    }

    public void reActive() {
        this.markAsActive();
    }

    public void changeRole(MemberProjectRole newRole) {
        if (newRole != null) {
            this.role = newRole;
        }
    }

    public void updateMemberProject(Member member, Project project, MemberProjectRole memberProjectRole) {
        this.member = member;
        this.project = project;
        this.role = memberProjectRole;
    }
}
