package com.groupeat.domain.business.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NtsBusinessStatus {
    CONTINUE("01", "계속사업자"),
    SUSPEND("02", "휴업자"),
    CLOSE("03", "폐업자"),
    UNKNOWN("99", "미등록 또는 알 수 없는 상태");

    private final String code;
    private final String description;

    // 국세청에서 받은 문자열 코드를 Enum으로 변환하는 팩토리 메서드
    public static NtsBusinessStatus fromCode(String code) {
        if (code == null || code.isBlank()) {
            return UNKNOWN;
        }
        for (NtsBusinessStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return UNKNOWN; // 정의되지 않은 코드가 오면 방어
    }
}