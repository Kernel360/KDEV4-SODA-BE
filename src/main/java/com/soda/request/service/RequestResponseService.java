package com.soda.request.service;

import com.soda.global.response.ErrorCode;
import com.soda.global.response.GeneralException;
import com.soda.global.security.auth.UserDetailsImpl;
import com.soda.member.entity.Member;
import com.soda.member.enums.MemberProjectRole;
import com.soda.member.enums.MemberRole;
import com.soda.member.repository.MemberRepository;
import com.soda.request.dto.ApproveRequestRequest;
import com.soda.request.dto.ApproveRequestResponse;
import com.soda.request.entity.Rejection;
import com.soda.request.entity.Request;
import com.soda.request.repository.RejectionRepository;
import com.soda.request.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RequestResponseService {
    private final RequestRepository requestRepository;
    private final RejectionRepository rejectionRepository;
    private final MemberRepository memberRepository;

    /*
    어떤 멤버가 어떤 request를 approve하는지만 알면 됨
    멤버가 유효한 멤버인지는 체크해야함
     */
    @Transactional
    public ApproveRequestResponse approveRequest(UserDetailsImpl userDetails, Long requestId, Long projectId, ApproveRequestRequest approveRequestRequest) {
        Member member = memberRepository.findWithProjectsById(userDetails.getMember().getId())
                .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));
        Request request = getRequestOrThrow(requestId);

        if(!isCliInCurrentProject(projectId, member) && !isAdmin(member)) {
            throw new GeneralException(ErrorCode.USER_NOT_IN_PROJECT_CLI);
        }

        request.approve();
        requestRepository.save(request);
        requestRepository.flush();

        return ApproveRequestResponse.fromEntity(request);
    }

//    @Transactional
//    public RejectRequestResponse rejectRequest(UserDetailsImpl userDetails, Long requestId, Long projectId, RejectRequestRequest approveRequestRequest) {
//        Member member = memberRepository.findWithProjectsById(userDetails.getMember().getId())
//                .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));
//        Request request = getRequestOrThrow(requestId);
//
//        if(!isCliInCurrentProject(projectId, member) && !isAdmin(member)) {
//            throw new GeneralException(ErrorCode.USER_NOT_IN_PROJECT_CLI);
//        }
//
//        Rejection rejection = Rejection.builder()
//                .member(member)
//                .request(request)
//                .comment(approveRequestRequest.getcomment())
//                .files()
//                .links()
//                .build();
//    }

    // 분리한 메서드들
    private Request getRequestOrThrow(Long requestId) {
        return requestRepository.findById(requestId).orElseThrow(() -> new GeneralException(ErrorCode.REQUEST_NOT_FOUND));
    }

    // member가 현재 프로젝트에 속한 "개발사"의 멤버인지 확인하는 메서드
    private static boolean isCliInCurrentProject(Long projectId, Member member) {
        return member.getMemberProjects().stream()
                .anyMatch(mp ->
                        mp.getProject().getId().equals(projectId) &&
                                (mp.getRole() == MemberProjectRole.CLI_MANAGER || mp.getRole() == MemberProjectRole.CLI_PARTICIPANT)
                );
    }

    private static boolean isAdmin(Member member) {
        return member.getRole() == MemberRole.ADMIN;
    }

}
