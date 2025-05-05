package com.soda.project.domain.stage.request.response;

import com.soda.global.response.GeneralException;
import com.soda.member.entity.Member;
import com.soda.project.domain.stage.request.Request;
import com.soda.project.interfaces.stage.request.response.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResponseService {

    private final ResponseFactory responseFactory;
    private final ResponseProvider responseProvider;


    public RequestApproveResponse approveRequest(Member member, Request request, RequestApproveRequest requestApproveRequest) {
        Response approval = responseFactory.createApproveResponse(
                member,
                request,
                requestApproveRequest.getComment(),
                requestApproveRequest.getLinks()
        );
        return RequestApproveResponse.fromEntity(responseProvider.store(approval));
    }

    public RequestRejectResponse rejectRequest(Member member, Request request, RequestRejectRequest requestRejectRequest) {
        Response rejection = responseFactory.createRejectResponse(
                member,
                request,
                requestRejectRequest.getComment(),
                requestRejectRequest.getLinks()
        );
        return RequestRejectResponse.fromEntity(responseProvider.store(rejection));
    }

    public ResponseUpdateResponse updateResponse(Response response, ResponseUpdateRequest responseUpdateRequest) {
        Response updatedResponse = responseFactory.updateResponse(
                response,
                responseUpdateRequest.getComment(),
                responseUpdateRequest.getLinks()
        );
        return ResponseUpdateResponse.fromEntity(responseProvider.storeAndflush(updatedResponse));
    }

    public ResponseDeleteResponse deleteResponse(Response response){
        Long countResponse = responseProvider.countNotDeletedByRequestId(response);
        response.delete(countResponse);
        return ResponseDeleteResponse.fromEntity(response);
    }

    public List<ResponseDTO> findAllByRequestId(Long requestId) {
        return responseProvider.findAllByRequestId(requestId);
    }

    public ResponseDTO findById(Long responseId) {
        return ResponseDTO.fromEntity(responseProvider.getResponseOrThrow(responseId));
    }

    public Response getResponseOrThrow(Long responseId) {
        return responseProvider.findById(responseId).orElseThrow(() -> new GeneralException(ResponseErrorCode.RESPONSE_NOT_FOUND));
    }
}
