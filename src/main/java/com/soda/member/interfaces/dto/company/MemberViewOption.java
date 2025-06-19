package com.soda.member.interfaces.dto.company;

import com.soda.member.domain.member.Member;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum MemberViewOption {
    ACTIVE("삭제되지 않은 멤버만 조회") {
        @Override
        public List<Member> filterMembers(List<Member> members) {
            return members.stream()
                    .filter(member -> !member.getIsDeleted())
                    .collect(Collectors.toList());
        }
    },
    DELETED("삭제된 멤버만 조회") {
        @Override
        public List<Member> filterMembers(List<Member> members) {
            return members.stream()
                    .filter(Member::getIsDeleted)
                    .collect(Collectors.toList());
        }
    },
    ALL("전체 멤버 조회") {
        @Override
        public List<Member> filterMembers(List<Member> members) {
            return members;
        }
    };

    private final String description;

    public abstract List<Member> filterMembers(List<Member> members);
}