package com.groupeat.domain.notification.service.query;

import com.groupeat.domain.notification.dto.response.NotificationListResponse;
import com.groupeat.domain.notification.entity.Notification;
import com.groupeat.domain.notification.repository.NotificationRepository;
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
        int fetchSize = size + 1;
        PageRequest pageRequest = PageRequest.of(0, fetchSize);
        List<Notification> notifications = lastNotificationId == null
                ? notificationRepository.findByReceiverMemberIdAndDeletedAtIsNullOrderByIdDesc(memberId, pageRequest)
                : notificationRepository.findByReceiverMemberIdAndIdLessThanAndDeletedAtIsNullOrderByIdDesc(
                        memberId,
                        lastNotificationId,
                        pageRequest
                );

        boolean hasNext = notifications.size() > size;
        if (hasNext) {
            notifications = notifications.subList(0, size);
        }

        List<NotificationListResponse.NotificationDTO> notificationList = notifications.stream()
                .map(NotificationListResponse.NotificationDTO::from)
                .toList();

        Long nextCursor = notificationList.isEmpty()
                ? null
                : notificationList.get(notificationList.size() - 1).notificationId();

        return NotificationListResponse.builder()
                .totalElements(notificationRepository.countByReceiverMemberIdAndDeletedAtIsNull(memberId))
                .notificationList(notificationList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }
}
