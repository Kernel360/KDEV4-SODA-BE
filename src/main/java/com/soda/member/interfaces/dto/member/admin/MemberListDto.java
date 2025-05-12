package com.soda.member.interfaces.dto.member.admin;

import com.soda.member.domain.member.Member;
import com.soda.member.domain.member.MemberRole;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MemberListDto {
    private Long id;
    private String authId;
    private String name;
    private String email;
    private MemberRole role;
    private String company;
    private String position;
    private boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Member 엔티티를 UserListDto로 변환하는 정적 팩토리 메소드
    public static MemberListDto fromEntity(Member member) {
        if (member == null) {
            return null;
        }

        String companyNameResult = null;
        if (member.getCompany() != null) {
            companyNameResult = member.getCompany().getName();
        }
        return new MemberListDto(
                member.getId(),
                member.getAuthId(),
                member.getName(),
                member.getEmail(),
                member.getRole(),
                companyNameResult,
                member.getPosition(),
                member.getIsDeleted(),
                member.getCreatedAt(),
                member.getUpdatedAt()
        );
    }

    // 모든 필드를 받는 생성자 (팩토리 메소드 내부에서 사용)
    private MemberListDto(Long id, String authId, String name, String email, MemberRole role, String company, String position, boolean isDeleted, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.authId = authId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.company = company;
        this.position = position;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
