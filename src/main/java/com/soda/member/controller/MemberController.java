package com.soda.member.controller;

import com.soda.global.response.ApiResponseForm;
import com.soda.member.dto.FindAuthIdRequest;
import com.soda.member.dto.FindAuthIdResponse;
import com.soda.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping
@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    /**
     * 이름과 이메일을 받아 마스킹된 아이디를 반환합니다.
     * @param request 이름과 이메일 정보
     * @return 성공 시 200 OK와 마스킹된 아이디, 실패 시 404 Not Found
     */
    @PostMapping("/members/find-id")
    public ResponseEntity<ApiResponseForm<FindAuthIdResponse>> findAuthId(
            @Valid @RequestBody FindAuthIdRequest request) {

        FindAuthIdResponse responseDto = memberService.findMaskedAuthId(request);

        return ResponseEntity.ok(ApiResponseForm.success(responseDto, "아이디 찾기 성공"));
    }
}
