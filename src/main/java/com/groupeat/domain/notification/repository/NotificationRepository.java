package com.groupeat.domain.notification.repository;

import com.groupeat.domain.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    long countByReceiverMemberIdAndDeletedAtIsNull(Long receiverMemberId);

    List<Notification> findByReceiverMemberIdAndDeletedAtIsNullOrderByIdDesc(
            Long receiverMemberId,
            Pageable pageable
    );

    List<Notification> findByReceiverMemberIdAndIdLessThanAndDeletedAtIsNullOrderByIdDesc(
            Long receiverMemberId,
            Long lastNotificationId,
            Pageable pageable
    );
}
