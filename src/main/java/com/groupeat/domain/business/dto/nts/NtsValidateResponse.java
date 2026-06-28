package com.groupeat.domain.business.dto.nts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.groupeat.domain.business.enums.NtsBusinessStatus;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public record NtsValidateResponse(
        String status_code,
        List<Data> data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(
            String b_no,      // 조회한 사업자번호
            String b_stt,     // 사업자 상태명 (예: "계속사업자", "휴업자", "폐업자")
            String b_stt_cd   // 상태 코드 (예: 01: 계속사업자, 02: 휴업자, 03: 폐업자)
    ) {
        public NtsBusinessStatus getBusinessStatus() {
            return NtsBusinessStatus.fromCode(this.b_stt_cd);
        }
    }
}
