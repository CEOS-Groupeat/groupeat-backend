package com.groupeat.domain.business.dto.nts;

import java.util.List;

public record NtsValidateRequest(
        List<String> b_no
) {
    public static NtsValidateRequest from(String businessNumber) {
        return new NtsValidateRequest(List.of(businessNumber));
    }
}
