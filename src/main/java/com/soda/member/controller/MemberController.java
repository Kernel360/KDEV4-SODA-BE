package com.soda.member.controller;

import com.soda.global.response.ApiResponseForm;
import com.soda.member.dto.FindAuthIdRequest;
import com.soda.member.dto.FindAuthIdResponse;
import com.soda.member.dto.InitialUserInfoRequestDto;
import com.soda.member.dto.member.MemberStatusResponse;
import com.soda.member.dto.member.MemberStatusUpdate;
import com.soda.member.dto.member.admin.MemberDetailDto;
import com.soda.member.entity.Member;
import com.soda.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
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
    public ResponseEntity<ApiResponseForm<Void>> setupInitialProfile(
            @PathVariable Long memberId,
            @Valid @RequestBody InitialUserInfoRequestDto requestDto) {
        memberService.setupInitialProfile(memberId, requestDto);
        return ResponseEntity.ok(ApiResponseForm.success(null, "초기 사용자 정보가 등록 성공"));
    }

    @GetMapping("/{memberId}/status")
    public ResponseEntity<ApiResponseForm<MemberStatusResponse>> getMemberStatus(@PathVariable Long memberId) {
        MemberStatusResponse responseDto = memberService.getMemberStatus(memberId);
        return ResponseEntity.ok(ApiResponseForm.success(responseDto,"멤버 상태 조회 성공"));
    }

    @PatchMapping("/{memberId}/status")
    public ResponseEntity<ApiResponseForm<MemberStatusResponse>> updateMemberStatus(
            @PathVariable Long memberId,
            @Valid @RequestBody MemberStatusUpdate requestDto) {

        MemberStatusResponse updatedStatusDto = memberService.updateMemberStatus(memberId, requestDto.getNewStatus());
        return ResponseEntity.ok(ApiResponseForm.success(updatedStatusDto,"멤버 상태 수정 성공"));
    }

}
