package com.groupeat.domain.notification.controller;

import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.domain.notification.dto.response.NotificationListResponse;
import com.groupeat.domain.notification.dto.response.NotificationReadAllResponse;
import com.groupeat.domain.notification.dto.response.NotificationReadResponse;
import com.groupeat.domain.notification.dto.response.NotificationUnreadCountResponse;
import com.groupeat.domain.notification.service.command.NotificationReadService;
import com.groupeat.domain.notification.service.query.NotificationQueryService;
import com.groupeat.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 목록 API")
public class NotificationController {

    private final NotificationQueryService notificationQueryService;
    private final NotificationReadService notificationReadService;

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

    @GetMapping("/unread-count")
    @Operation(summary = "미읽음 알림 개수 조회", description = "로그인한 회원의 미읽음 알림 개수를 조회합니다.")
    public ApiResponse<NotificationUnreadCountResponse> getUnreadCount(
            @AuthenticationPrincipal AuthenticatedMember member
    ) {
        return ApiResponse.onSuccess(notificationReadService.getUnreadCount(member.memberId()));
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "알림 단건 읽음 처리", description = "로그인한 회원의 알림을 읽음 처리합니다.")
    public ApiResponse<NotificationReadResponse> markAsRead(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable Long notificationId
    ) {
        return ApiResponse.onSuccess(notificationReadService.markAsRead(member.memberId(), notificationId));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "알림 전체 읽음 처리", description = "로그인한 회원의 미읽음 알림을 전체 읽음 처리합니다.")
    public ApiResponse<NotificationReadAllResponse> markAllAsRead(
            @AuthenticationPrincipal AuthenticatedMember member
    ) {
        return ApiResponse.onSuccess(notificationReadService.markAllAsRead(member.memberId()));
    }
}
