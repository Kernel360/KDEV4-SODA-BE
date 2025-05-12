package com.soda.project.domain.stage.request.link;

import com.soda.global.response.GeneralException;
import com.soda.project.domain.stage.common.link.LinkStrategy;
import com.soda.project.domain.stage.request.Request;
import com.soda.project.domain.stage.request.RequestErrorCode;
import com.soda.project.domain.stage.request.RequestProvider;
import com.soda.project.interfaces.stage.common.link.dto.LinkUploadRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RequestLinkStrategy implements LinkStrategy<Request, RequestLink> {

    private final RequestProvider requestProvider;
    private final RequestLinkProvider requestLinkProvider;


    @Override
    public String getSupportedDomain() {
        return "request";
    }

    @Override
    public Request getDomainOrThrow(Long domainId) {
        return requestProvider.findById(domainId)
                .orElseThrow(() -> new GeneralException(RequestErrorCode.REQUEST_NOT_FOUND));
    }

    @Override
    public void validateWriter(Long memberId, Request request) {
        if (!request.getMember().getId().equals(memberId)) {
            throw new GeneralException(RequestErrorCode.USER_NOT_WRITE_REQUEST);
        }
    }

    @Override
    public RequestLink toEntity(LinkUploadRequest.LinkUploadDTO dto, Request request) {
        return RequestLink.create(dto.getUrlAddress(), dto.getUrlDescription(), request);
    }

    @Override
    public List<RequestLink> toEntities(List<LinkUploadRequest.LinkUploadDTO> dtos, Request request) {
        if (dtos == null || dtos.isEmpty()) { return List.of();}

        return dtos.stream()
                .map(dto -> RequestLink.create(dto.getUrlAddress(), dto.getUrlDescription(), request))
                .toList();
    }

    @Transactional
    @Override
    public void saveAll(List<RequestLink> entities) {
        requestLinkProvider.saveAll(entities);
    }

    @Override
    public RequestLink getLinkOrThrow(Long linkId) {
        return requestLinkProvider.findById(linkId)
                .orElseThrow(() -> new GeneralException(RequestErrorCode.REQUEST_LINK_NOT_FOUND));
    }

    @Override
    public void validateLinkUploader(Long memberId, RequestLink link) {
        if (!link.getRequest().getMember().getId().equals(memberId)) {
            throw new GeneralException(RequestErrorCode.USER_NOT_UPLOAD_LINK);
        }
    }
}
