package com.groupeat.domain.admin.business.enums;

import com.groupeat.domain.business.enums.BusinessVerificationStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum AdminVerificationFilterType {
    ALL("전체", null),
    PENDING("승인 대기", List.of(BusinessVerificationStatus.PENDING)),
    APPROVED("승인 완료", List.of(BusinessVerificationStatus.APPROVED)),
    REJECTED("승인 반려", List.of(BusinessVerificationStatus.REJECTED));

    private final String description;
    private final List<BusinessVerificationStatus> mappedStatuses;
}
