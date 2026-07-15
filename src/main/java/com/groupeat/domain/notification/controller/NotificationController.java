package com.groupeat.domain.notification.controller;

import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.domain.notification.dto.response.NotificationListResponse;
import com.groupeat.domain.notification.service.query.NotificationQueryService;
import com.groupeat.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 목록 API")
public class NotificationController {

    private final NotificationQueryService notificationQueryService;

    @GetMapping
    @Operation(summary = "알림 목록 조회", description = "로그인한 회원의 알림 목록을 최신순으로 조회합니다.")
    public ApiResponse<NotificationListResponse> getNotificationList(
            @AuthenticationPrincipal AuthenticatedMember member,
            @RequestParam(required = false) @Parameter(description = "마지막으로 조회된 알림 ID") Long lastNotificationId,
            @RequestParam(defaultValue = "20") @Parameter(description = "한 번에 조회할 알림 개수") int size
    ) {
        return ApiResponse.onSuccess(
                notificationQueryService.getNotificationList(member.memberId(), lastNotificationId, size)
        );
    }
}
