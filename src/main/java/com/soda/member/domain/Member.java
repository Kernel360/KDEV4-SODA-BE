package com.soda.member.domain;

import com.soda.common.BaseEntity;
import com.soda.member.domain.company.Company;
import com.soda.notification.entity.MemberNotification;
import com.soda.project.domain.member.MemberProject;
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

    @Column(nullable = false, unique = true)
    private String authId;

    @Column(nullable = false)
    private String password;

    private String email;

    private String position;

    private String phoneNumber;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberProject> memberProjects = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberNotification> noticeList = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MemberStatus memberStatus = MemberStatus.AVAILABLE;

    public void updateMemberStatus(MemberStatus memberStatus) {this.memberStatus = memberStatus;}

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public void updateAdminInfo(String name, String email, MemberRole role, Company company, String position, String phoneNumber) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.company = company;
        this.position = position;
        this.phoneNumber = phoneNumber;
    }

    public void initialProfile(String name, String email, String phoneNumber, String authId, String newPassword, String position) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.authId = authId;
        this.password = newPassword;
        this.position = position;
    }

    public void myProfileUpdate(String name, String email, String phoneNumber, String position) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.position = position;
    }

    public void Deleted() {
        this.markAsDeleted();
    }

    public void Active() {
        this.markAsActive();
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
