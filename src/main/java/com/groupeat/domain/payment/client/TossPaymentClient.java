package com.groupeat.domain.payment.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.groupeat.domain.payment.config.TossPaymentProperties;
import com.groupeat.domain.payment.dto.toss.TossPaymentCancelRequest;
import com.groupeat.domain.payment.dto.toss.TossPaymentConfirmRequest;
import com.groupeat.domain.payment.dto.toss.TossPaymentConfirmResponse;
import com.groupeat.domain.payment.dto.toss.TossPaymentErrorResponse;
import com.groupeat.domain.payment.exception.TossPaymentException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class TossPaymentClient {

    private final TossPaymentProperties properties;
    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;

    // 토스페이먼츠 결제 승인 요청
    public TossPaymentConfirmResponse confirmPayment(TossPaymentConfirmRequest request, String idempotencyKey) {
        try {
            return restClientBuilder.build()
                    .post()
                    .uri(properties.confirmUrl())
                    .header(HttpHeaders.AUTHORIZATION, createAuthorizationHeader())
                    .header("Idempotency-Key", idempotencyKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(TossPaymentConfirmResponse.class);
        } catch (RestClientResponseException e) {
            TossPaymentErrorResponse errorResponse = parseErrorResponse(e);
            throw new TossPaymentException(
                    e.getStatusCode(),
                    errorResponse.code(),
                    errorResponse.message()
            );
        }
    }

    // 토스페이먼츠 결제 취소 요청
    public TossPaymentConfirmResponse cancelPayment(String paymentKey, TossPaymentCancelRequest request) {
        try {
            return restClientBuilder.build()
                    .post()
                    .uri(properties.cancelBaseUrl() + "/" + paymentKey + "/cancel")
                    .header(HttpHeaders.AUTHORIZATION, createAuthorizationHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(TossPaymentConfirmResponse.class);
        } catch (RestClientResponseException e) {
            TossPaymentErrorResponse errorResponse = parseErrorResponse(e);
            throw new TossPaymentException(
                    e.getStatusCode(),
                    errorResponse.code(),
                    errorResponse.message()
            );
        }
    }

    private String createAuthorizationHeader() {
        // Toss Payments Basic 인증은 시크릿 키 뒤에 콜론을 붙인 값을 Base64 인코딩
        String credential = properties.secretKey() + ":";
        String encodedCredential = Base64.getEncoder()
                .encodeToString(credential.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encodedCredential;
    }

    private TossPaymentErrorResponse parseErrorResponse(RestClientResponseException e) {
        try {
            return objectMapper.readValue(e.getResponseBodyAsString(), TossPaymentErrorResponse.class);
        } catch (JsonProcessingException parseException) {
            return new TossPaymentErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.name(),
                    "토스페이먼츠 결제 승인 응답을 처리하지 못했습니다."
            );
        }
    }
}
