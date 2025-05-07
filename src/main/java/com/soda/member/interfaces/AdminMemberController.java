package com.soda.member.interfaces;

import com.soda.global.response.ApiResponseForm;
import com.soda.member.application.MemberFacade;
import com.soda.member.interfaces.dto.AdminUpdateUserRequestDto;
import com.soda.member.interfaces.dto.member.admin.MemberDetailDto;
import com.soda.member.interfaces.dto.member.admin.MemberListDto;
import com.soda.member.interfaces.dto.member.admin.UpdateUserStatusRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

    private final MemberFacade memberFacade;

    /**
     * 사용자 활성/비활성 상태 변경 API
     */
    @PutMapping("/{userId}/status")
    public ResponseEntity<ApiResponseForm<Void>> updateMemberStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusRequestDto requestDto,
            HttpServletRequest request) {
        Long currentMemberId = (Long) request.getAttribute("memberId");
        memberFacade.updateMemberDeletionStatus(userId, currentMemberId, requestDto);
        return ResponseEntity.ok(ApiResponseForm.success(null, "회원 상태 변경 성공"));
    }

    /**
     * 전체 사용자 목록 조회 API (페이징 및 검색 지원)
     */
    @GetMapping
    public ResponseEntity<ApiResponseForm<Page<MemberListDto>>> getAllUsers(
            Pageable pageable,
            @RequestParam(required = false) String searchKeyword) {
        Page<MemberListDto> users = memberFacade.getAllUsers(pageable, searchKeyword);
        return ResponseEntity.ok(ApiResponseForm.success(users, "전체 회원 목록 조회 성공"));
    }

    /**
     * 특정 사용자 상세 정보 조회 API
     * 
     * @param userId 조회할 사용자의 ID
     * @return 사용자 상세 정보 응답
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponseForm<MemberDetailDto>> getMemberDetail(@PathVariable Long userId) {
        MemberDetailDto member = memberFacade.getMemberDetail(userId);
        return ResponseEntity.ok(ApiResponseForm.success(member, "회원 상세 정보 조회 성공"));
    }

    /**
     * 관리자에 의한 사용자 정보 수정 API
     * 
     * @param userId     수정할 사용자의 ID
     * @param requestDto 수정할 사용자 정보
     * @return 수정된 사용자 상세 정보 응답
     */
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponseForm<MemberDetailDto>> updateMemberInfo(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUpdateUserRequestDto requestDto) {
        MemberDetailDto updatedMember = memberFacade.updateMemberInfo(userId, requestDto);
        return ResponseEntity.ok(ApiResponseForm.success(updatedMember, "회원 정보 수정 성공"));
    }

}
