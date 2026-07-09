package com.groupeat.domain.owner.controller;

import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.domain.owner.dto.request.OwnerNotificationSettingsUpdateRequest;
import com.groupeat.domain.owner.dto.response.OwnerNotificationSettingsResponse;
import com.groupeat.domain.owner.service.OwnerNotificationSettingsService;
import com.groupeat.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/owner/mypage/notification-settings")
@RequiredArgsConstructor
@Tag(name = "Owner Notification Settings", description = "사업자 알림 설정 API")
public class OwnerNotificationSettingsController {

    private final OwnerNotificationSettingsService notificationSettingsService;

    @GetMapping
    @Operation(
            summary = "사업자 알림 설정 조회",
            description = "마케팅 정보 수신 동의 여부와 주문 현황 알림 동의 여부를 조회합니다."
    )
    public ApiResponse<OwnerNotificationSettingsResponse> getNotificationSettings(
            @AuthenticationPrincipal AuthenticatedMember member
    ) {
        return ApiResponse.onSuccess(notificationSettingsService.getSettings(member.memberId()));
    }

    @PatchMapping
    @Operation(
            summary = "사업자 알림 설정 수정",
            description = "마케팅 정보 수신 동의 여부와 주문 현황 알림 동의 여부를 각각 토글합니다. 변경할 값만 전달할 수 있으며, FCM 토큰 및 푸시 발송 기능은 포함하지 않습니다."
    )
    public ApiResponse<OwnerNotificationSettingsResponse> updateNotificationSettings(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @RequestBody OwnerNotificationSettingsUpdateRequest request
    ) {
        return ApiResponse.onSuccess(notificationSettingsService.updateSettings(member.memberId(), request));
    }
}
