package com.soda.project.domain.stage.request.approver;

import com.soda.common.BaseEntity;
import com.soda.member.domain.Member;
import com.soda.project.domain.stage.request.Request;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ApproverDesignation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private Request request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder
    public ApproverDesignation(Request request, Member member) {
        this.request = request;
        this.member = member;
    }

    public static List<ApproverDesignation> designateApprover(Request request, List<Member> members) {
        return members.stream()
                .map(member -> ApproverDesignation.builder()
                        .request(request)
                        .member(member)
                        .build())
                .collect(Collectors.toList());
    }

    public void delete() {
        markAsDeleted();
    }
}
