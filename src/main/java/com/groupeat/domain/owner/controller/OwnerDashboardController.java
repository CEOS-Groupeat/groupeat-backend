package com.groupeat.domain.owner.controller;

import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.domain.owner.dto.response.OwnerDashboardSummaryResponse;
import com.groupeat.domain.owner.service.OwnerDashboardService;
import com.groupeat.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner/dashboard")
@Tag(name = "Owner Dashboard", description = "사장님 홈 화면 API")
public class OwnerDashboardController {

    private final OwnerDashboardService ownerDashboardService;

    @GetMapping("/summary")
    @Operation(summary = "홈 화면 요약 정보 조회", description = "대기, 확정, 완료 건수를 한 번에 조회합니다.")
    public ApiResponse<OwnerDashboardSummaryResponse> getSummary(
            @AuthenticationPrincipal AuthenticatedMember member
    ) {
        return ApiResponse.onSuccess(ownerDashboardService.getDashboardSummary(member.memberId()));
    }
}
