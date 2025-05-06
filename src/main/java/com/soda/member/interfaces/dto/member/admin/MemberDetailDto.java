package com.soda.member.interfaces.dto.member.admin;

import com.soda.member.entity.Member;
import com.soda.member.enums.MemberRole;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class MemberDetailDto {

    private Long id;
    private String authId;
    private String name;
    private String email;
    private MemberRole role;
    private String position;
    private String phoneNumber;
    private Long companyId;
    private String companyName;
    private boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MemberDetailDto fromEntity(Member member) {
        if (member == null) {
            return null;
        }

        Long cId = null;
        String cName = null;
        if (member.getCompany() != null) {
            cId = member.getCompany().getId();
            cName = member.getCompany().getName();
        }

        return new MemberDetailDto(
                member.getId(),
                member.getAuthId(),
                member.getName(),
                member.getEmail(),
                member.getRole(),
                member.getPosition(),
                member.getPhoneNumber(),
                cId,
                cName,
                member.getIsDeleted(),
                member.getCreatedAt(),
                member.getUpdatedAt()
        );
    }

    private MemberDetailDto(Long id, String authId, String name, String email, MemberRole role, String position, String phoneNumber, Long companyId, String companyName, boolean isDeleted, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.authId = authId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.position = position;
        this.phoneNumber = phoneNumber;
        this.companyId = companyId;
        this.companyName = companyName;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
