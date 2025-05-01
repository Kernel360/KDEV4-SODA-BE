package com.soda.project.domain.stage.request.response;

import com.soda.common.link.dto.LinkUploadRequest;
import com.soda.common.link.service.LinkService;
import com.soda.member.entity.Member;
import com.soda.project.domain.stage.request.Request;
import com.soda.project.domain.stage.request.response.link.ResponseLink;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ResponseFactory {

    private final LinkService linkService;

    public Response createApproveResponse(Member member, Request request, String comment,
                                          List<? extends LinkUploadRequest.LinkUploadDTO> linkContents) {

        Response approval = Response.createApprove(member, request, comment);

        List<ResponseLink> links = linkService.buildLinks("response", approval, linkContents);
        approval.addLinks(links);

        return Response.createApprove(member, request, comment);
    }
}
