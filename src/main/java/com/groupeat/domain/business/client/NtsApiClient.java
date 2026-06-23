package com.groupeat.domain.business.client;

import com.groupeat.domain.business.config.NtsApiProperties;
import com.groupeat.domain.business.dto.nts.NtsValidateRequest;
import com.groupeat.domain.business.dto.nts.NtsValidateResponse;
import com.groupeat.domain.business.exception.BusinessErrorStatus;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class NtsApiClient {

    private final NtsApiProperties properties;
    private final RestClient.Builder restClientBuilder;

    // 국세청 API를 호출하여 사업자번호 상태를 조회
    public NtsValidateResponse validateBusinessNumber(NtsValidateRequest request) {
        try {
            return restClientBuilder.build()
                    .post()
                    .uri(properties.baseUrl() + "/status?serviceKey=" + properties.serviceKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(NtsValidateResponse.class);

        } catch (RestClientResponseException e) {
            log.error("[국세청 API 에러] 상태코드: {}, 응답: {}", e.getStatusCode(), e.getResponseBodyAsString());

            throw new GeneralException(BusinessErrorStatus.NTS_API_COMMUNICATION_FAILED);
        }
    }
}