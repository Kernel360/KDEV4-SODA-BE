package com.soda.project.domain;

import com.soda.member.entity.Member;
import com.soda.member.enums.MemberProjectRole;
import com.soda.project.entity.MemberProject;
import com.soda.project.entity.Project;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MemberProjectDTO {

    private Long memberId;
    private Long projectId;
    private MemberProjectRole memberProjectRole;

    @Builder
    public MemberProjectDTO (Long memberId, Long projectId, MemberProjectRole memberProjectRole) {
        this.memberId = memberId;
        this.projectId = projectId;
        this.memberProjectRole = memberProjectRole;
    }

    // Entity → DTO 변환
    public static MemberProjectDTO fromEntity(MemberProject memberProject) {
        return MemberProjectDTO.builder()
                .memberId(memberProject.getMember().getId())
                .projectId(memberProject.getProject().getId())
                .memberProjectRole(memberProject.getRole())
                .build();
    }

    // DTO → Entity 변환
    public MemberProject toEntity(Member member, Project project, MemberProjectRole memberProjectRole) {
        return MemberProject.builder()
                .member(member)
                .project(project)
                .memberProjectRole(memberProjectRole)
                .build();
    }
}
