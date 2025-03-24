package com.soda.request.service;

import com.soda.global.response.ErrorCode;
import com.soda.global.response.GeneralException;
import com.soda.member.entity.Member;
import com.soda.member.enums.MemberProjectRole;
import com.soda.member.repository.MemberRepository;
import com.soda.project.entity.Task;
import com.soda.project.repository.TaskRepository;
import com.soda.request.dto.RequestCreateRequest;
import com.soda.request.dto.RequestUpdateRequest;
import com.soda.request.entity.Request;
import com.soda.request.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RequestService {
    private final RequestRepository requestRepository;
    private final MemberRepository memberRepository;
    private final TaskRepository taskRepository;

    /*
    승인 요청 생성
    어떤 유저가 어떤 단계별 task에서 승인요청을 생성했는지 알아야함
    위 정보 바탕으로 save만 하면 끝
    */
    @Transactional
    public void createRequest(Long memberId, RequestCreateRequest requestCreateRequest) throws GeneralException {
        Member member = memberRepository.findByMemberId(memberId);
        Task task = taskRepository.findByTaskId(requestCreateRequest.getTaskId());

        // 현재 프로젝트에 속한 "개발사"의 멤버인지 확인하고 아니면 예외 throw
        boolean isDevInCurrentProject = member.getMemberProjects().stream()
                .anyMatch(mp ->
                        mp.getProject().equals(task.getStage().getProject()) &&
                        (mp.getRole() == MemberProjectRole.DEV_MANAGER || mp.getRole() == MemberProjectRole.DEV_PARTICIPANT)
                );
        if (!isDevInCurrentProject) {
            throw new GeneralException(ErrorCode.USER_NOT_IN_PROJECT_DEV);
        }

        requestRepository.save(Request.builder()
                .member(member)
                .task(task)
                .build());
    }

    /*
    현재 task의 승인요청 모두 조회
    */
    public List<Request> findAllByTaskId(Long taskId) {
        return requestRepository.findAllByTaskId(taskId);
    }

    /*
    requestId, title, content
    현재 유저가 request의 작성자인지 확인하고
    title, content 수정
     */
    @Transactional
    public void updateRequest(Long memberId, RequestUpdateRequest requestUpdateRequest) throws GeneralException {
        Member member = memberRepository.findByMemberId(memberId);
        Request request = requestRepository.findById(requestUpdateRequest.getRequestId())
                .orElseThrow(() -> new GeneralException(ErrorCode.REQUEST_NOT_FOUND));
        boolean isRequestWriter = request.getMember().equals(member);
        if (!isRequestWriter) { throw new GeneralException(ErrorCode.USER_NOT_WRITE_REQUEST); }

        if(requestUpdateRequest.getTitle() != null) {
            request.updateTitle(requestUpdateRequest.getTitle());
        }
        if(requestUpdateRequest.getContent() != null) {
            request.updateContent(requestUpdateRequest.getContent());
        }
        requestRepository.save(request);
    }

    @Transactional
    public void deleteRequest(Long memberId, Long requestId) throws GeneralException {
        Member member = memberRepository.findByMemberId(memberId);
        Request request = requestRepository.findById(requestId).orElseThrow(() -> new GeneralException(ErrorCode.REQUEST_NOT_FOUND));

        boolean isRequestWriter = request.getMember().equals(member);
        if (!isRequestWriter) { throw new GeneralException(ErrorCode.USER_NOT_WRITE_REQUEST); }

        requestRepository.delete(request);
    }
}
