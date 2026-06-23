package com.groupeat.domain.business.service;

import com.groupeat.domain.business.client.NtsApiClient;
import com.groupeat.domain.business.dto.nts.NtsValidateRequest;
import com.groupeat.domain.business.dto.nts.NtsValidateResponse;
import com.groupeat.domain.business.dto.response.BusinessValidateResponse;
import com.groupeat.domain.business.enums.NtsBusinessStatus;
import com.groupeat.domain.business.exception.BusinessErrorStatus;
import com.groupeat.domain.business.jwt.BusinessValidationTokenProvider; // 아까 만든 토큰 프로바이더
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessValidationService {

    private final NtsApiClient ntsApiClient;
    private final BusinessValidationTokenProvider validationTokenProvider;

    // 프론트엔드로부터 사업자번호를 받아 유효성을 검증하고, 성공 시 토큰을 반환
    public BusinessValidateResponse validateBusinessNumber(String rawBusinessNumber) {

        // 1차 데이터 정제 및 검증(하이픈 빼고 숫자만 남김)
        String bno = rawBusinessNumber.replaceAll("-", "");

        // 숫자 10자리인지 1차로 검증
        if (!bno.matches("^\\d{10}$")) {
            throw new GeneralException(BusinessErrorStatus.INVALID_BUSINESS_NUMBER);
        }

        // 국세청 API 호출
        NtsValidateRequest ntsRequest = NtsValidateRequest.from(bno);
        NtsValidateResponse ntsResponse = ntsApiClient.validateBusinessNumber(ntsRequest);

        // 국세청 응답 분석
        if (!"OK".equals(ntsResponse.status_code()) || ntsResponse.data() == null || ntsResponse.data().isEmpty()) {
            log.error("[사업자 검증 실패] 국세청 응답 비정상 - {}", ntsResponse);
            throw new GeneralException(BusinessErrorStatus.NTS_API_COMMUNICATION_FAILED);
        }

        // 상태 코드 추출
        NtsBusinessStatus status = ntsResponse.data().get(0).getBusinessStatus();

        // 상태별 분기 처리
        return switch (status) {
            case CONTINUE -> {
                String token = validationTokenProvider.createValidationToken(bno);
                yield new BusinessValidateResponse(token);
            }
            case SUSPEND, CLOSE -> {
                log.info("[사업자 가입 거절] 상태: {}, 번호: {}", status.getDescription(), bno);
                throw new GeneralException(BusinessErrorStatus.CLOSED_BUSINESS_NOT_ALLOWED);
            }
            case UNKNOWN -> {
                log.info("[사업자 가입 거절] 미등록 번호 시도: {}", bno);
                throw new GeneralException(BusinessErrorStatus.INVALID_BUSINESS_NUMBER);
            }
        };
    }
}