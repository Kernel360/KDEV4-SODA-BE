package com.soda.member.entity;

import com.soda.common.BaseEntity;
import com.soda.member.enums.MemberRole;
import com.soda.notice.entity.MemberNotice;
import com.soda.project.entity.MemberProject;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String authId;

    @Column(nullable = false)
    private String password;

    private String email;

    private String position;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberProject> memberProjects = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberNotice> noticeList = new ArrayList<>();

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateAuthId(String authId) {
        this.authId = authId;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updatePosition(String position) {
        this.position = position;
    }

    public void updatePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void updateRole(MemberRole role) {
        this.role = role;
    }

    public void updateCompany(Company company) {
        this.company = company;
    }

    // 승인요청 테스트를 위해 임시로 만든 메서드입니다.
    // 멤버프로젝트 담당하시는 분이 사용할지 말지 취사선택하시면 될 것 같습니다
    public void setMemberProjects(MemberProject memberProjects) {
        if (this.memberProjects == null) {
            this.memberProjects = new ArrayList<>();
        }
        this.memberProjects.add(memberProjects);
    }

    public boolean isAdmin() {
        return this.role == MemberRole.ADMIN;  // 예시로 role이 ADMIN일 경우 관리자
    }
}
