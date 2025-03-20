package com.soda.member.entity;

import com.soda.common.BaseEntity;
import com.soda.member.enums.MemberRole;
import com.soda.notice.entity.MemberNotice;
import com.soda.project.entity.MemberProject;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
public class Member extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String authId;

    @Column(nullable = false)
    private String password;

    private String position;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberProject> memberProjects = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberNotice> noticeList = new ArrayList<>();

}
