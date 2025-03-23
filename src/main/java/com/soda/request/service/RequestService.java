package com.soda.request.service;

import com.soda.member.entity.Member;
import com.soda.member.repository.MemberRepository;
import com.soda.project.entity.Task;
import com.soda.project.repository.TaskRepository;
import com.soda.request.dto.RequestCreateRequest;
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
    public void createRequest(Long memberId, RequestCreateRequest requestCreateRequest) {
        Member member = memberRepository.findByMemberId(memberId);
        Task task = taskRepository.findByTaskId(requestCreateRequest.getTaskId());
        requestRepository.save(Request.builder()
                .member(member)
                .task(task)
                .isApproved(requestCreateRequest.getIsApproved())
                .build());
    }

    /*
    현재 task의 승인요청 모두 조회
     */
    public List<Request> findAllByTaskId(Long taskId) {
        return requestRepository.findAllByTaskId(taskId);
    }
}
