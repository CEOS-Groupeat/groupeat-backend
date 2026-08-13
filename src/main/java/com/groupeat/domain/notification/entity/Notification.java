package com.groupeat.domain.notification.entity;

import com.groupeat.domain.notification.enums.NotificationReferenceType;
import com.groupeat.domain.notification.enums.NotificationType;
import com.groupeat.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "notification")
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Column(name = "receiver_member_id", nullable = false)
    private Long receiverMemberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType notificationType;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "body", nullable = false, length = 500)
    private String body;

    @Column(name = "store_name", length = 100)
    private String storeName;

    @Column(name = "menu_summary", length = 150)
    private String menuSummary;

    @Column(name = "pickup_date")
    private LocalDate pickupDate;

    @Column(name = "pickup_time")
    private LocalTime pickupTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", length = 50)
    private NotificationReferenceType referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    public static Notification create(
            Long receiverMemberId,
            NotificationType notificationType,
            String title,
            String body,
            String storeName,
            String menuSummary,
            LocalDate pickupDate,
            LocalTime pickupTime,
            NotificationReferenceType referenceType,
            Long referenceId
    ) {
        return Notification.builder()
                .receiverMemberId(receiverMemberId)
                .notificationType(notificationType)
                .title(title)
                .body(body)
                .storeName(storeName)
                .menuSummary(menuSummary)
                .pickupDate(pickupDate)
                .pickupTime(pickupTime)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .build();
    }

    public boolean isRead() {
        return readAt != null;
    }

    public void markAsRead(LocalDateTime readAt) {
        if (this.readAt == null) {
            this.readAt = readAt;
        }
    }
}
