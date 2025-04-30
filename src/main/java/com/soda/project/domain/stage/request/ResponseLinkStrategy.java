package com.soda.project.domain.stage.request;

import com.soda.common.link.dto.LinkUploadRequest;
import com.soda.common.link.strategy.LinkStrategy;
import com.soda.global.response.GeneralException;
import com.soda.project.domain.stage.request.error.ResponseErrorCode;
import com.soda.project.infrastructure.ResponseLinkRepository;
import com.soda.project.infrastructure.ResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ResponseLinkStrategy implements LinkStrategy<Response, ResponseLink> {

    private final ResponseRepository responseRepository;
    private final ResponseLinkRepository responseLinkRepository;


    @Override
    public String getSupportedDomain() {
        return "response";
    }

    @Override
    public Response getDomainOrThrow(Long domainId) {
        return responseRepository.findById(domainId)
                .orElseThrow(() -> new GeneralException(ResponseErrorCode.RESPONSE_NOT_FOUND));
    }

    @Override
    public void validateWriter(Long memberId, Response response) {
        if (!response.getMember().getId().equals(memberId)) {
            throw new GeneralException(ResponseErrorCode.USER_NOT_WRITE_RESPONSE);
        }
    }

    @Override
    public ResponseLink toEntity(LinkUploadRequest.LinkUploadDTO dto, Response response) {
        return ResponseLink.builder()
                .urlAddress(dto.getUrlAddress())
                .urlDescription(dto.getUrlDescription())
                .response(response)
                .build();
    }

    @Override
    public List<ResponseLink> toEntities(List<LinkUploadRequest.LinkUploadDTO> dtos, Response response) {
        if (dtos == null || dtos.isEmpty()) {
            return List.of();
        }

        return dtos.stream()
                .map(dto -> ResponseLink.builder()
                        .urlAddress(dto.getUrlAddress())
                        .urlDescription(dto.getUrlDescription())
                        .response(response)
                        .build())
                .toList();
    }

    @Override
    public void saveAll(List<ResponseLink> entities) {
        responseLinkRepository.saveAll(entities);
    }

    @Override
    public ResponseLink getLinkOrThrow(Long linkId) {
        return responseLinkRepository.findById(linkId)
                .orElseThrow(() -> new GeneralException(ResponseErrorCode.RESPONSE_LINK_NOT_FOUND));
    }

    @Override
    public void validateLinkUploader(Long memberId, ResponseLink link) {
        if (!link.getResponse().getMember().getId().equals(memberId)) {
            throw new GeneralException(ResponseErrorCode.USER_NOT_UPLOAD_LINK);
        }
    }
}
