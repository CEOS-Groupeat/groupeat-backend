package com.groupeat.domain.notification.dto;

import com.groupeat.domain.notification.entity.Notification;
import com.groupeat.domain.notification.enums.NotificationReferenceType;
import com.groupeat.domain.notification.enums.NotificationType;

import java.util.Map;
import java.util.UUID;

public record NotificationFcmMessage(
        String messageId,
        Long notificationId,
        Long memberId,
        NotificationType notificationType,
        String title,
        String body,
        Map<String, String> data,
        NotificationReferenceType referenceType,
        Long referenceId
) {

    public static NotificationFcmMessage from(Notification notification, Map<String, String> data) {
        return new NotificationFcmMessage(
                UUID.randomUUID().toString(),
                notification.getId(),
                notification.getReceiverMemberId(),
                notification.getNotificationType(),
                notification.getTitle(),
                notification.getBody(),
                data,
                notification.getReferenceType(),
                notification.getReferenceId()
        );
    }

    public FcmSendRequest toFcmSendRequest() {
        return new FcmSendRequest(memberId, title, body, data);
    }
}
