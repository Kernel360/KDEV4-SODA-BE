package com.soda.member.entity;

import com.soda.common.BaseEntity;
import com.soda.member.dto.UpdateMemberRequest;
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

    @Column(nullable = false)
    private String email;

    private String position;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    private boolean isEnabled;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberProject> memberProjects = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberNotice> noticeList = new ArrayList<>();

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }


}
