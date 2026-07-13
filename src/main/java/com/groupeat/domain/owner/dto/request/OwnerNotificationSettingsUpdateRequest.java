package com.groupeat.domain.owner.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record OwnerNotificationSettingsUpdateRequest(
        @Schema(description = "마케팅 정보 수신 동의 여부. 변경하지 않을 경우 생략", example = "true", nullable = true)
        Boolean marketingAgreed,

        @Schema(description = "주문 현황 알림 동의 여부. 변경하지 않을 경우 생략", example = "true", nullable = true)
        Boolean orderStatusNotificationAgreed
) {
    public boolean hasAnySetting() {
        return marketingAgreed != null || orderStatusNotificationAgreed != null;
    }
}
