package com.groupeat.domain.notification.dto;

import java.util.Map;

public record FcmSendRequest(
        Long memberId,

        // 알림 제목
        String title,

        // 알림 본문
        String body,

        // 추가 데이터
        Map<String, String> data
) {
}
