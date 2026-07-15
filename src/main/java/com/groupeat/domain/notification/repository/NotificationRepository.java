package com.groupeat.domain.notification.repository;

import com.groupeat.domain.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    long countByReceiverMemberIdAndDeletedAtIsNull(Long receiverMemberId);

    long countByReceiverMemberIdAndReadAtIsNullAndDeletedAtIsNull(Long receiverMemberId);

    Optional<Notification> findByIdAndDeletedAtIsNull(Long notificationId);

    List<Notification> findByReceiverMemberIdAndDeletedAtIsNullOrderByIdDesc(
            Long receiverMemberId,
            Pageable pageable
    );

    List<Notification> findByReceiverMemberIdAndIdLessThanAndDeletedAtIsNullOrderByIdDesc(
            Long receiverMemberId,
            Long lastNotificationId,
            Pageable pageable
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Notification n
            SET n.readAt = :readAt
            WHERE n.receiverMemberId = :receiverMemberId
              AND n.readAt IS NULL
              AND n.deletedAt IS NULL
            """)
    int markAllAsReadByReceiverMemberId(
            @Param("receiverMemberId") Long receiverMemberId,
            @Param("readAt") LocalDateTime readAt
    );
}
