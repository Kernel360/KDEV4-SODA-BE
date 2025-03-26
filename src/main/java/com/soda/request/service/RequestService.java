package com.soda.request.service;

import com.soda.global.response.CommonErrorCode;
import com.soda.global.response.GeneralException;
import com.soda.global.security.auth.UserDetailsImpl;
import com.soda.member.entity.Member;
import com.soda.member.enums.MemberProjectRole;
import com.soda.member.enums.MemberRole;
import com.soda.member.repository.MemberRepository;
import com.soda.project.entity.Task;
import com.soda.project.repository.TaskRepository;
import com.soda.request.dto.*;
import com.soda.request.entity.Request;
import com.soda.request.enums.RequestStatus;
import com.soda.request.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RequestService {
    private final RequestRepository requestRepository;
    private final MemberRepository memberRepository;
    private final TaskRepository taskRepository;

    /*
    Request(승인요청) 생성
    어떤 member가 어떤 task에서 승인요청을 생성했는지를 request 데이터에 넣어줘야함.
    Request 데이터 생성 전에, 요청한 member가 현재 프로젝트에 속한 "개발사"의 멤버이거나 ADMIN유저인지 확인해야함.
    */
    @Transactional
    public RequestCreateResponse createRequest(UserDetailsImpl userDetails, Long projectId, Long taskId, RequestCreateRequest requestCreateRequest) throws GeneralException {
        // isDevInCurrentProject에서 memberProject를 조회해 userDetails.getMember로 멤버객체를 그대로 사용하면 "LazyInitializationException"이 발생해
        // userDetails.getMember.getId를 바탕으로 (레프트)페치조인해 memberProject와 함께 영속성 컨텍스트에 등록
        System.out.println(userDetails.getMember().getId());
        Member member = memberRepository.findWithProjectsById(userDetails.getMember().getId())
                .orElseThrow(() -> new GeneralException(CommonErrorCode.MEMBER_NOT_FOUND));
        Task task = getTaskOrThrow(taskId);

        // 현재 프로젝트에 속한 "개발사"의 멤버가 아니고, 어드민도 아니면 USER_NOT_IN_PROJECT_DEV 반환
        if (!isDevInCurrentProject(projectId, member) && !isAdmin(member)) {
            throw new GeneralException(CommonErrorCode.USER_NOT_IN_PROJECT_DEV);
        }

        Request request = Request.builder()
                .member(member)
                .task(task)
                .title(requestCreateRequest.getTitle())
                .content(requestCreateRequest.getContent())
                .status(RequestStatus.PENDING)
                .build();
        requestRepository.save(request);

        return RequestCreateResponse.fromEntity(request);
    }


    public List<RequestDTO> findAllByTaskId(Long taskId) {
        List<Request> requests = requestRepository.findAllByTask_IdAndIsDeletedFalse(taskId);
        return requests.stream().map(RequestDTO::fromEntity).collect(Collectors.toList());
    }

    public RequestDTO findById(Long requestId) {
        Request request = getRequestOrThrow(requestId);
        return RequestDTO.fromEntity(request);
    }


    @Transactional
    public RequestUpdateResponse updateRequest(UserDetailsImpl userDetails, Long requestId, RequestUpdateRequest requestUpdateRequest) throws GeneralException {
        Member member = userDetails.getMember();
        Request request = getRequestOrThrow(requestId);

        // update요청을 한 member가 승인요청을 작성했던 member인지 확인
        validateRequestWriter(request, member);

        // request의 제목, 내용을 수정
        updateRequestFields(requestUpdateRequest, request);

        requestRepository.save(request);
        requestRepository.flush();

        return RequestUpdateResponse.fromEntity(request);
    }

    @Transactional
    public RequestDeleteResponse deleteRequest(UserDetailsImpl userDetails, Long requestId) throws GeneralException {
        Member member = userDetails.getMember();
        Request request = getRequestOrThrow(requestId);

        // delete요청을 한 member가 승인요청을 작성했던 member인지 확인
        validateRequestWriter(request, member);

        // request 소프트 삭제
        request.delete();

        return RequestDeleteResponse.fromEntity(request);
    }


    // 분리한 메서드들
    private Task getTaskOrThrow(Long taskId) {
        return taskRepository.findById(taskId).orElseThrow(() -> new GeneralException(CommonErrorCode.TASK_NOT_FOUND));
    }

    private Request getRequestOrThrow(Long requestId) {
        return requestRepository.findById(requestId).orElseThrow(() -> new GeneralException(CommonErrorCode.REQUEST_NOT_FOUND));
    }

    // member가 현재 프로젝트에 속한 "개발사"의 멤버인지 확인하는 메서드
    private static boolean isDevInCurrentProject(Long projectId, Member member) {
        return member.getMemberProjects().stream()
                .anyMatch(mp ->
                        mp.getProject().getId().equals(projectId) &&
                                (mp.getRole() == MemberProjectRole.DEV_MANAGER || mp.getRole() == MemberProjectRole.DEV_PARTICIPANT)
                );
    }

    private static boolean isAdmin(Member member) {
        return member.getRole() == MemberRole.ADMIN;
    }

    // Request(승인요청)을 작성한 멤버가 (인자의)Member인지 확인하는 메서드
    private static void validateRequestWriter(Request request, Member member) {
        boolean isRequestWriter = request.getMember().getId().equals(member.getId());
        if (!isRequestWriter) { throw new GeneralException(CommonErrorCode.USER_NOT_WRITE_REQUEST); }
    }

    // Request(승인요청)의 제목이나 내용을 수정하는 메서드
    private static void updateRequestFields(RequestUpdateRequest requestUpdateRequest, Request request) {
        if(requestUpdateRequest.getTitle() != null) {
            request.updateTitle(requestUpdateRequest.getTitle());
        }
        if(requestUpdateRequest.getContent() != null) {
            request.updateContent(requestUpdateRequest.getContent());
        }
    }
}
