package com.groupeat.domain.notification.dto.response;

import com.groupeat.domain.notification.entity.Notification;
import com.groupeat.domain.notification.enums.NotificationReferenceType;
import com.groupeat.domain.notification.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Builder
public record NotificationListResponse(
        @Schema(description = "조회 가능한 전체 알림 개수", example = "12")
        long totalElements,

        @Schema(description = "알림 목록")
        List<NotificationDTO> notificationList,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext,

        @Schema(description = "다음 커서 ID", example = "42")
        Long nextCursor
) {
    @Builder
    public record NotificationDTO(
            @Schema(description = "알림 ID", example = "1")
            Long notificationId,

            @Schema(description = "알림 타입", example = "ORDER_ACCEPTED")
            NotificationType notificationType,

            @Schema(description = "알림 제목", example = "승인 완료")
            String title,

            @Schema(description = "알림 본문", example = "데이브런치에서 주문을 승인했습니다.")
            String body,

            @Schema(description = "가게명", example = "데이브런치")
            String storeName,

            @Schema(description = "메뉴 요약", example = "햄치즈 샌드위치 외 1개")
            String menuSummary,

            @Schema(description = "픽업 날짜", example = "2026-07-21")
            LocalDate pickupDate,

            @Schema(description = "픽업 시간", example = "10:00:00")
            LocalTime pickupTime,

            @Schema(description = "참조 대상 타입", example = "ORDER")
            NotificationReferenceType referenceType,

            @Schema(description = "참조 대상 ID", example = "9")
            Long referenceId,

            @Schema(description = "읽음 여부", example = "false")
            boolean read,

            @Schema(description = "수신 시각", example = "2026-07-15T14:30:00")
            LocalDateTime receivedAt
    ) {
        public static NotificationDTO from(Notification notification) {
            return NotificationDTO.builder()
                    .notificationId(notification.getId())
                    .notificationType(notification.getNotificationType())
                    .title(notification.getTitle())
                    .body(notification.getBody())
                    .storeName(notification.getStoreName())
                    .menuSummary(notification.getMenuSummary())
                    .pickupDate(notification.getPickupDate())
                    .pickupTime(notification.getPickupTime())
                    .referenceType(notification.getReferenceType())
                    .referenceId(notification.getReferenceId())
                    .read(notification.isRead())
                    .receivedAt(notification.getCreatedAt())
                    .build();
        }
    }
}
