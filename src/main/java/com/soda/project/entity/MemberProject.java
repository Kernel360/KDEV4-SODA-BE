package com.soda.project.entity;

import com.soda.common.BaseEntity;
import com.soda.member.entity.Member;
import com.soda.member.enums.MemberProjectRole;
import jakarta.persistence.*;
import lombok.Getter;

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
}
