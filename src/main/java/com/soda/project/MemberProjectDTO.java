package com.soda.project;

import com.soda.member.Member;
import com.soda.member.MemberProjectRole;
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
