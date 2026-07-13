package com.groupeat.domain.notification.dto;

public record FcmSendResult(
        // 발송 대상으로 조회된 FCM 등록값 수
        int targetCount,

        // Firebase 발송 성공 수
        int successCount,

        // Firebase 발송 실패 수
        int failureCount
) {
    public static FcmSendResult empty() {
        return new FcmSendResult(0, 0, 0);
    }
}
