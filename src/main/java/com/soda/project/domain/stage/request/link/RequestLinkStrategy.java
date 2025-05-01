package com.soda.project.domain.stage.request.link;

import com.soda.common.link.dto.LinkUploadRequest;
import com.soda.common.link.strategy.LinkStrategy;
import com.soda.global.response.GeneralException;
import com.soda.project.domain.stage.request.Request;
import com.soda.project.domain.stage.request.error.RequestErrorCode;
import com.soda.project.infrastructure.RequestLinkRepository;
import com.soda.project.infrastructure.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class RequestLinkStrategy implements LinkStrategy<Request, RequestLink> {

    private final RequestRepository requestRepository;
    private final RequestLinkRepository requestLinkRepository;


    @Override
    public String getSupportedDomain() {
        return "request";
    }

    @Override
    public Request getDomainOrThrow(Long domainId) {
        return requestRepository.findById(domainId)
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
        return RequestLink.builder()
                .urlAddress(dto.getUrlAddress())
                .urlDescription(dto.getUrlDescription())
                .request(request)
                .build();
    }

    public List<RequestLink> toEntities(List<LinkUploadRequest.LinkUploadDTO> dtos, Request request) {
        if (dtos == null || dtos.isEmpty()) {
            return List.of();
        }

        return dtos.stream()
                .map(dto -> RequestLink.builder()
                        .urlAddress(dto.getUrlAddress())
                        .urlDescription(dto.getUrlDescription())
                        .request(request)
                        .build())
                .toList();
    }

    @Override
    public void saveAll(List<RequestLink> entities) {
        requestLinkRepository.saveAll(entities);
    }

    @Override
    public RequestLink getLinkOrThrow(Long linkId) {
        return requestLinkRepository.findById(linkId)
                .orElseThrow(() -> new GeneralException(RequestErrorCode.REQUEST_LINK_NOT_FOUND));
    }

    @Override
    public void validateLinkUploader(Long memberId, RequestLink link) {
        if (!link.getRequest().getMember().getId().equals(memberId)) {
            throw new GeneralException(RequestErrorCode.USER_NOT_UPLOAD_LINK);
        }
    }
}
