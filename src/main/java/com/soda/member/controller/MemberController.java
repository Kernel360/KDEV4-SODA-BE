package com.soda.member.controller;

import com.soda.global.response.ApiResponseForm;
import com.soda.member.dto.FindAuthIdRequest;
import com.soda.member.dto.FindAuthIdResponse;
import com.soda.member.dto.InitialUserInfoRequestDto;
import com.soda.member.dto.MemberUpdateRequest;
import com.soda.member.dto.member.ChangePasswordRequest;
import com.soda.member.dto.member.MemberStatusResponse;
import com.soda.member.dto.member.MemberStatusUpdate;
import com.soda.member.dto.member.admin.MemberDetailDto;
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

    @GetMapping("/my")
    public ResponseEntity<ApiResponseForm<MemberDetailDto>> getMemberDetail(HttpServletRequest request) {
        Long currentMemberId = (Long) request.getAttribute("memberId");
        MemberDetailDto member = memberService.getMemberDetail(currentMemberId);
        return ResponseEntity.ok(ApiResponseForm.success(member, "마이페이지 조회 성공 "));
    }

    @PutMapping("/my")
    public ResponseEntity<ApiResponseForm<Void>> updateMyProfile(
            @Valid @RequestBody MemberUpdateRequest requestDto, HttpServletRequest request) {
        Long currentMemberId = (Long) request.getAttribute("memberId");
        memberService.updateMyProfile(currentMemberId, requestDto);
        return ResponseEntity.ok(ApiResponseForm.success(null, "개인 정보 수정 성공"));
    }

    @GetMapping("/{memberId}/status")
    public ResponseEntity<ApiResponseForm<MemberStatusResponse>> getMemberStatus(@PathVariable Long memberId) {
        MemberStatusResponse responseDto = memberService.getMemberStatus(memberId);
        return ResponseEntity.ok(ApiResponseForm.success(responseDto, "멤버 상태 조회 성공"));
    }

    @PatchMapping("/{memberId}/status")
    public ResponseEntity<ApiResponseForm<MemberStatusResponse>> updateMemberStatus(
            @PathVariable Long memberId,
            @Valid @RequestBody MemberStatusUpdate requestDto) {

        MemberStatusResponse updatedStatusDto = memberService.updateMemberStatus(memberId, requestDto.getNewStatus());
        return ResponseEntity.ok(ApiResponseForm.success(updatedStatusDto, "멤버 상태 수정 성공"));
    }

    @PutMapping("/my/password")
    public ResponseEntity<ApiResponseForm<Void>> changeMyPassword(
            @Valid @RequestBody ChangePasswordRequest requestDto, HttpServletRequest request) {
        Long currentMemberId = (Long) request.getAttribute("memberId");

        memberService.changeUserPassword(currentMemberId, requestDto);

        return ResponseEntity.ok(ApiResponseForm.success(null, "비밀번호가 성공적으로 변경되었습니다."));
    }

}
