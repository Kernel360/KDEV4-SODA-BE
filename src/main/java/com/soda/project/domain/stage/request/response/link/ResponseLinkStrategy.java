package com.soda.project.domain.stage.request.response.link;

import com.soda.global.response.GeneralException;
import com.soda.project.domain.stage.common.link.LinkStrategy;
import com.soda.project.domain.stage.request.response.Response;
import com.soda.project.domain.stage.request.response.ResponseErrorCode;
import com.soda.project.domain.stage.request.response.ResponseProvider;
import com.soda.project.interfaces.stage.common.link.dto.LinkUploadRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ResponseLinkStrategy implements LinkStrategy<Response, ResponseLink> {

    private final ResponseProvider responseProvider;
    private final ResponseLinkProvider responseLinkProvider;


    @Override
    public String getSupportedDomain() {
        return "response";
    }

    @Override
    public Response getDomainOrThrow(Long domainId) {
        return responseProvider.findById(domainId)
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
        return ResponseLink.create(dto.getUrlAddress(), dto.getUrlDescription(), response);
    }

    @Override
    public List<ResponseLink> toEntities(List<LinkUploadRequest.LinkUploadDTO> dtos, Response response) {
        if (dtos == null || dtos.isEmpty()) { return List.of();}

        return dtos.stream()
                .map(dto -> ResponseLink.create(dto.getUrlAddress(), dto.getUrlDescription(), response))
                .toList();
    }

    @Override
    public void saveAll(List<ResponseLink> entities) {
        responseLinkProvider.saveAll(entities);
    }

    @Override
    public ResponseLink getLinkOrThrow(Long linkId) {
        return responseLinkProvider.findById(linkId)
                .orElseThrow(() -> new GeneralException(ResponseErrorCode.RESPONSE_LINK_NOT_FOUND));
    }

    @Override
    public void validateLinkUploader(Long memberId, ResponseLink link) {
        if (!link.getResponse().getMember().getId().equals(memberId)) {
            throw new GeneralException(ResponseErrorCode.USER_NOT_UPLOAD_LINK);
        }
    }
}
