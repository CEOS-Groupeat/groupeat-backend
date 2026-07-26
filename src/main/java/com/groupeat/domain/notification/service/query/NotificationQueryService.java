package com.groupeat.domain.notification.service.query;

import com.groupeat.domain.notification.dto.response.NotificationListResponse;
import com.groupeat.domain.notification.entity.Notification;
import com.groupeat.domain.notification.repository.NotificationRepository;
import com.groupeat.global.dto.CursorResponse;
import com.groupeat.global.util.CursorUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryService {

    private final NotificationRepository notificationRepository;

    // 로그인 회원의 알림 목록을 최신순 커서 방식으로 조회
    public NotificationListResponse getNotificationList(Long memberId, Long lastNotificationId, int size) {
        PageRequest pageRequest = PageRequest.of(0, size + 1);

        List<Notification> notifications = lastNotificationId == null
                ? notificationRepository.findByReceiverMemberIdAndDeletedAtIsNullOrderByIdDesc(memberId, pageRequest)
                : notificationRepository.findByReceiverMemberIdAndIdLessThanAndDeletedAtIsNullOrderByIdDesc(memberId, lastNotificationId, pageRequest);

        CursorResponse<NotificationListResponse.NotificationDTO> cursorResponse =
                CursorUtils.getCursorResponse(notifications, size, Notification::getId)
                        .map(NotificationListResponse.NotificationDTO::from);

        return NotificationListResponse.builder()
                .totalElements(notificationRepository.countByReceiverMemberIdAndDeletedAtIsNull(memberId))
                .notificationList(cursorResponse.content())
                .hasNext(cursorResponse.hasNext())
                .nextCursor(cursorResponse.nextCursor())
                .build();
    }
}
