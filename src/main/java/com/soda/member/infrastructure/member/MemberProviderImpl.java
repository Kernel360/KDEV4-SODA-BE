package com.soda.member.infrastructure.member;

import com.soda.global.response.CommonErrorCode;
import com.soda.global.response.GeneralException;
import com.soda.member.domain.member.Member;
import com.soda.member.domain.member.MemberErrorCode;
import com.soda.member.domain.member.MemberProvider;
import com.soda.member.domain.company.Company;
import com.soda.member.interfaces.dto.member.admin.MemberDetailDto;
import com.soda.project.domain.ProjectErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberProviderImpl implements MemberProvider {
    private final MemberRepository memberRepository;

    @Override
    public Member store(Member member) {
        return memberRepository.save(member);
    }

    @Override
    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

    @Override
    public List<Member> findAllById(List<Long> ids) {
        return memberRepository.findAllById(ids);
    }

    @Override
    public Optional<Member> findByIdAndIsDeletedFalse(Long id) {
        return memberRepository.findByIdAndIsDeletedFalse(id);
    }

    @Override
    public Optional<Member> findByAuthIdAndIsDeletedFalse(String authId) {
        return memberRepository.findByAuthIdAndIsDeletedFalse(authId);
    }

    @Override
    public Optional<Member> findByEmailAndIsDeletedFalse(String email) {
        return memberRepository.findByEmailAndIsDeletedFalse(email);
    }

    @Override
    public Optional<Member> findByNameAndEmailAndIsDeletedFalse(String name, String email) {
        return memberRepository.findByNameAndEmailAndIsDeletedFalse(name, email);
    }

    @Override
    public Optional<Member> findWithProjectsById(Long id) {
        return memberRepository.findWithProjectsById(id);
    }

    @Override
    public List<Member> findByIdInAndIsDeletedFalse(List<Long> ids) {
        return memberRepository.findByIdInAndIsDeletedFalse(ids);
    }

    @Override
    public List<Member> findMembersByIdsAndCompany(List<Long> ids, Company company) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        if (company == null || company.getId() == null) {
            throw new GeneralException(CommonErrorCode.BAD_REQUEST);
        }

        log.debug("ID 목록 {} 과 회사 ID {} 로 멤버 조회 시작", ids, company.getId());
        List<Member> members = memberRepository.findByIdInAndIsDeletedFalse(ids);

        if (members.size() != ids.size()) {
            List<Long> foundIds = members.stream().map(Member::getId).toList();
            List<Long> notFoundIds = ids.stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toList());
            log.warn("요청된 멤버 ID 중 일부를 찾을 수 없습니다. Requested: {}, NotFound: {}", ids, notFoundIds);
            throw new GeneralException(MemberErrorCode.NOT_FOUND_MEMBER);
        }

        Long expectedCompanyId = company.getId();
        for (Member member : members) {
            if (member.getCompany() == null || !member.getCompany().getId().equals(expectedCompanyId)) {
                log.error("멤버(ID:{})가 예상된 회사(ID:{}, 이름:'{}') 소속이 아닙니다. 실제 소속: {}",
                        member.getId(), expectedCompanyId, company.getName(),
                        (member.getCompany() != null
                                ? member.getCompany().getId() + "('" + member.getCompany().getName() + "')"
                                : "없음"));
                throw new GeneralException(ProjectErrorCode.MEMBER_NOT_IN_SPECIFIED_COMPANY);
            }
        }
        log.debug("ID 목록 {} 의 모든 멤버가 회사 ID {} 소속임을 확인", ids, company.getId());

        return members;
    }

    @Override
    public Page<Member> findAll(Pageable pageable) {
        return memberRepository.findAll(pageable);
    }

    @Override
    public Page<Member> findByKeywordIncludingDeleted(String keyword, Pageable pageable) {
        return memberRepository.findByKeywordIncludingDeleted(keyword, pageable);
    }

    @Override
    public boolean existsByAuthId(String authId) {
        return memberRepository.existsByAuthId(authId);
    }

    @Override
    public boolean existsByEmailAndIsDeletedFalse(String email) {
        return memberRepository.existsByEmailAndIsDeletedFalse(email);
    }

    @Override
    public Page<Member> findAllWithCompany(Pageable pageable) {
        return memberRepository.findAllWithCompany(pageable);
    }

    @Override
    public Page<Member> findByKeywordWithCompany(String keyword, Pageable pageable) {
        return memberRepository.findByKeywordWithCompany(keyword, pageable);
    }

    @Override
    public MemberDetailDto getMemberDetailWithCompany(Long userId) {
        Member member = findById(userId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.NOT_FOUND_MEMBER));
        return MemberDetailDto.fromEntity(member);
    }

    @Override
    public List<Member> findMembersByCompany(Company company) {
        if (company == null || company.getId() == null) {
            throw new GeneralException(CommonErrorCode.BAD_REQUEST);
        }
        return memberRepository.findByCompanyAndIsDeletedFalse(company);
    }

    @Override
    public Optional<Member> findByNameAndEmail(String name, String email) {
        return memberRepository.findByNameAndEmailAndIsDeletedFalse(name, email);
    }
}