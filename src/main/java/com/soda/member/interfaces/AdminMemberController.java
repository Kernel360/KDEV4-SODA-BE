package com.soda.member.interfaces;

import com.soda.global.response.ApiResponseForm;
import com.soda.global.response.PagedData;
import com.soda.member.interfaces.dto.AdminUpdateUserRequestDto;
import com.soda.member.interfaces.dto.member.admin.MemberDetailDto;
import com.soda.member.interfaces.dto.member.admin.UpdateUserStatusRequestDto;
import com.soda.member.interfaces.dto.member.admin.MemberListDto;
import com.soda.member.domain.AdminMemberService;
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

    /**
     * 특정 사용자 상세 정보 조회 API
     * @param userId 조회할 사용자의 ID
     * @return 사용자 상세 정보 응답
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponseForm<MemberDetailDto>> getUserDetail(@PathVariable Long userId) {
        MemberDetailDto memberDetail = adminService.getMemberDetail(userId);
        return ResponseEntity.ok(ApiResponseForm.success(memberDetail, "사용자 상세 정보 조회가 완료되었습니다."));
    }

    /**
     * 관리자에 의한 사용자 정보 수정 API
     * @param userId 수정할 사용자의 ID
     * @param requestDto 수정할 사용자 정보
     * @return 수정된 사용자 상세 정보 응답
     */
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponseForm<MemberDetailDto>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUpdateUserRequestDto requestDto) {
        MemberDetailDto updatedMember = adminService.updateMemberInfo(userId, requestDto);
        return ResponseEntity.ok(ApiResponseForm.success(updatedMember, "사용자 정보가 성공적으로 수정되었습니다."));
    }

}
