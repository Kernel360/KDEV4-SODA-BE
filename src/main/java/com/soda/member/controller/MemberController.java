package com.soda.member.controller;

import com.soda.global.response.ApiResponseForm;
import com.soda.member.dto.FindAuthIdRequest;
import com.soda.member.dto.FindAuthIdResponse;
import com.soda.member.dto.InitialUserInfoRequestDto;
import com.soda.member.dto.member.admin.MemberDetailDto;
import com.soda.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/members")
@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    /**
     * 이름과 이메일을 받아 마스킹된 아이디를 반환합니다.
     *
     * @param request 이름과 이메일 정보
     * @return 성공 시 200 OK와 마스킹된 아이디, 실패 시 404 Not Found
     */
    @PostMapping("/find-id")
    public ResponseEntity<ApiResponseForm<FindAuthIdResponse>> findAuthId(
            @Valid @RequestBody FindAuthIdRequest request) {

        FindAuthIdResponse responseDto = memberService.findMaskedAuthId(request);

        return ResponseEntity.ok(ApiResponseForm.success(responseDto, "아이디 찾기 성공"));
    }

    @PutMapping("/{memberId}/initial-profile")
    public ResponseEntity<ApiResponseForm<MemberDetailDto>> setupInitialProfile(
            @PathVariable Long memberId,
            @Valid @RequestBody InitialUserInfoRequestDto requestDto) {
        memberService.setupInitialProfile(memberId, requestDto);
        return ResponseEntity.ok(ApiResponseForm.success(null, "초기 사용자 정보가 성공적으로 등록되었습니다."));
    }
}
