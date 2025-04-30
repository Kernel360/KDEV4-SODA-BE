package com.soda.project.domain.stage.request.response;

import com.soda.common.link.dto.LinkUploadRequest;
import com.soda.common.link.service.LinkService;
import com.soda.member.entity.Member;
import com.soda.project.domain.stage.request.Request;
import com.soda.project.domain.stage.request.response.enums.ResponseStatus;
import com.soda.project.domain.stage.request.response.link.ResponseLink;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ResponseFactory {

    private final LinkService linkService;

    public Response createApproveResponse(Member member, Request request, String comment, List<? extends LinkUploadRequest.LinkUploadDTO> linkContents) {
        // 1. ID 없이 빈 Response 인스턴스 생성 (단순히 객체만)
        Response dummyResponseForLink = Response.builder()
                .member(member)
                .request(request)
                .comment(comment)
                .status(ResponseStatus.APPROVED)
                .build();

        // 2. 해당 객체를 기반으로 링크 생성
        List<ResponseLink> links = linkService.buildLinks("response", dummyResponseForLink, linkContents);

        // 3. 링크를 포함한 최종 Response 생성
        return Response.createApprove(member, request, comment, links);
    }
}
