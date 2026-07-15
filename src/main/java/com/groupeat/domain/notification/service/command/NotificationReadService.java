package com.groupeat.domain.notification.service.command;

import com.groupeat.domain.notification.dto.response.NotificationReadAllResponse;
import com.groupeat.domain.notification.dto.response.NotificationReadResponse;
import com.groupeat.domain.notification.dto.response.NotificationUnreadCountResponse;
import com.groupeat.domain.notification.entity.Notification;
import com.groupeat.domain.notification.exception.NotificationErrorStatus;
import com.groupeat.domain.notification.repository.NotificationRepository;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationReadService {

    private final NotificationRepository notificationRepository;

    // 로그인 회원의 알림을 멱등적으로 읽음 처리
    @Transactional
    public NotificationReadResponse markAsRead(Long memberId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndDeletedAtIsNull(notificationId)
                .orElseThrow(() -> new GeneralException(NotificationErrorStatus.NOTIFICATION_NOT_FOUND));

        validateOwner(memberId, notification);
        notification.markAsRead(LocalDateTime.now());

        return new NotificationReadResponse(
                notification.getId(),
                notification.isRead(),
                notification.getReadAt()
        );
    }

    // 로그인 회원의 미읽음 알림 전체를 읽음 처리
    @Transactional
    public NotificationReadAllResponse markAllAsRead(Long memberId) {
        int updatedCount = notificationRepository.markAllAsReadByReceiverMemberId(
                memberId,
                LocalDateTime.now()
        );
        return new NotificationReadAllResponse(updatedCount);
    }

    // 로그인 회원의 미읽음 알림 개수 조회
    public NotificationUnreadCountResponse getUnreadCount(Long memberId) {
        return new NotificationUnreadCountResponse(
                notificationRepository.countByReceiverMemberIdAndReadAtIsNullAndDeletedAtIsNull(memberId)
        );
    }

    private void validateOwner(Long memberId, Notification notification) {
        if (!notification.getReceiverMemberId().equals(memberId)) {
            throw new GeneralException(NotificationErrorStatus.NOTIFICATION_FORBIDDEN);
        }
    }
}
