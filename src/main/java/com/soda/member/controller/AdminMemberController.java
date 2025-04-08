package com.soda.member.controller;

import com.soda.global.response.ApiResponseForm;
import com.soda.global.response.PagedData;
import com.soda.member.dto.UpdateUserStatusRequestDto;
import com.soda.member.dto.MemberListDto;
import com.soda.member.service.AdminMemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminMemberController {

    private final AdminMemberService adminService;

    /**
     * 사용자 활성/비활성 상태 변경 API
     */
    @PutMapping("/{userId}/status")
    public ResponseEntity<ApiResponseForm<Void>> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusRequestDto requestDto,
            HttpServletRequest request) {
        Long currentMemberId = (Long) request.getAttribute("memberId");
        adminService.updateMemberStatus(userId, currentMemberId, requestDto);
        return ResponseEntity.ok(ApiResponseForm.success(null, "사용자 상태가 성공적으로 변경되었습니다."));
    }

    /**
     * 전체 사용자 목록 조회 API (페이징 및 검색 지원)
     */
    @GetMapping
    public ResponseEntity<ApiResponseForm<PagedData<MemberListDto>>> getAllUsers(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(value = "search", required = false) String searchKeyword) {
        Page<MemberListDto> userPage = adminService.getAllUsers(pageable, searchKeyword);

        PagedData<MemberListDto> responseData = new PagedData<>(userPage);
        return ResponseEntity.ok(ApiResponseForm.success(responseData, "사용자 목록 조회가 완료되었습니다."));
    }
}
