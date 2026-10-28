package com.groupeat.domain.payment.service;

import com.groupeat.domain.payment.client.TossPaymentClient;
import com.groupeat.domain.payment.dto.PreparedPaymentConfirm;
import com.groupeat.domain.payment.dto.request.PaymentConfirmRequest;
import com.groupeat.domain.payment.dto.response.PaymentConfirmResponse;
import com.groupeat.domain.payment.dto.toss.TossPaymentConfirmResponse;
import com.groupeat.domain.payment.enums.PaymentStatus;
import com.groupeat.domain.payment.exception.PaymentErrorStatus;
import com.groupeat.domain.payment.exception.TossPaymentException;
import com.groupeat.global.exception.GeneralException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.client.RestClientException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentConfirmServiceTest {

    private static final Long MEMBER_ID = 2L;
    private static final Long PAYMENT_ID = 1L;
    private static final String ORDER_ID = "ORDER_TEST_001";
    private static final String PAYMENT_KEY = "tgen_20260528033827CH4N9";
    private static final String IDEMPOTENCY_KEY = "8b7f0f4e-7c3a-4c85-9f36-111111111111";
    private static final int AMOUNT = 15000;

    @Mock
    private TossPaymentClient tossPaymentClient;

    @Mock
    private PaymentConfirmTransactionService paymentConfirmTransactionService;

    @Mock
    private PaymentReconciliationService paymentReconciliationService;

    private PaymentConfirmService paymentConfirmService;

    @BeforeEach
    void setUp() {
        paymentConfirmService = new PaymentConfirmService(tossPaymentClient, paymentConfirmTransactionService, paymentReconciliationService);
    }

    // 토스 승인에 성공하면 결제 완료 저장을 호출한다.
    @Test
    void confirm_success_approvesPayment() {
        PaymentConfirmRequest request = confirmRequest();
        TossPaymentConfirmResponse tossResponse = tossDoneResponse(ORDER_ID, AMOUNT);
        PaymentConfirmResponse approvedResponse = doneResponse();

        when(paymentConfirmTransactionService.prepareConfirm(MEMBER_ID, IDEMPOTENCY_KEY, request))
                .thenReturn(PreparedPaymentConfirm.ready(PAYMENT_ID, ORDER_ID, AMOUNT));
        when(tossPaymentClient.confirmPayment(any(), eq(IDEMPOTENCY_KEY))).thenReturn(tossResponse);
        when(paymentConfirmTransactionService.approvePayment(PAYMENT_ID, tossResponse)).thenReturn(approvedResponse);

        PaymentConfirmResponse response = paymentConfirmService.confirm(MEMBER_ID, IDEMPOTENCY_KEY, request);

        assertThat(response).isEqualTo(approvedResponse);
        verify(tossPaymentClient).confirmPayment(any(), eq(IDEMPOTENCY_KEY));
        verify(paymentConfirmTransactionService).approvePayment(PAYMENT_ID, tossResponse);
        verify(paymentConfirmTransactionService, never()).failPayment(any(), any(), any());
    }

    // 이미 승인된 결제는 토스 승인 API를 다시 호출하지 않는다.
    @Test
    void confirm_alreadyConfirmed_returnsResponseWithoutTossCall() {
        PaymentConfirmRequest request = confirmRequest();
        PaymentConfirmResponse alreadyConfirmedResponse = doneResponse();

        when(paymentConfirmTransactionService.prepareConfirm(MEMBER_ID, IDEMPOTENCY_KEY, request))
                .thenReturn(PreparedPaymentConfirm.alreadyConfirmed(alreadyConfirmedResponse));

        PaymentConfirmResponse response = paymentConfirmService.confirm(MEMBER_ID, IDEMPOTENCY_KEY, request);

        assertThat(response).isEqualTo(alreadyConfirmedResponse);
        verifyNoInteractions(tossPaymentClient);
        verify(paymentConfirmTransactionService, never()).approvePayment(any(), any());
    }

    // 토스 승인 API가 실패하면 결제 실패 상태로 저장한다.
    @Test
    void confirm_tossFailure_marksPaymentFailed() {
        PaymentConfirmRequest request = confirmRequest();

        when(paymentConfirmTransactionService.prepareConfirm(MEMBER_ID, IDEMPOTENCY_KEY, request))
                .thenReturn(PreparedPaymentConfirm.ready(PAYMENT_ID, ORDER_ID, AMOUNT));
        when(tossPaymentClient.confirmPayment(any(), eq(IDEMPOTENCY_KEY)))
                .thenThrow(new TossPaymentException(HttpStatus.BAD_REQUEST, "NOT_FOUND_PAYMENT_SESSION", "결제 시간이 만료되었습니다."));

        assertThatThrownBy(() -> paymentConfirmService.confirm(MEMBER_ID, IDEMPOTENCY_KEY, request))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(PaymentErrorStatus.TOSS_CONFIRM_FAILED)
                );

        verify(paymentConfirmTransactionService).failPayment(PAYMENT_ID, "NOT_FOUND_PAYMENT_SESSION", "결제 시간이 만료되었습니다.");
    }

    // 토스에서 동일 멱등 요청 처리 중 응답이 오면 실패로 저장하지 않는다.
    @Test
    void confirm_tossIdempotentRequestProcessing_doesNotMarkPaymentFailed() {
        PaymentConfirmRequest request = confirmRequest();

        when(paymentConfirmTransactionService.prepareConfirm(MEMBER_ID, IDEMPOTENCY_KEY, request))
                .thenReturn(PreparedPaymentConfirm.ready(PAYMENT_ID, ORDER_ID, AMOUNT));
        when(tossPaymentClient.confirmPayment(any(), eq(IDEMPOTENCY_KEY)))
                .thenThrow(new TossPaymentException(HttpStatus.CONFLICT, "IDEMPOTENT_REQUEST_PROCESSING", "이전 멱등 요청이 처리중입니다."));

        assertThatThrownBy(() -> paymentConfirmService.confirm(MEMBER_ID, IDEMPOTENCY_KEY, request))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(PaymentErrorStatus.PAYMENT_CONFIRM_IN_PROGRESS)
                );

        verify(paymentConfirmTransactionService, never()).failPayment(any(), any(), any());
    }

    // 토스 승인 응답이 요청 정보와 다르면 실패 상태로 저장한다.
    @Test
    void confirm_invalidTossResponse_marksPaymentFailed() {
        PaymentConfirmRequest request = confirmRequest();

        when(paymentConfirmTransactionService.prepareConfirm(MEMBER_ID, IDEMPOTENCY_KEY, request))
                .thenReturn(PreparedPaymentConfirm.ready(PAYMENT_ID, ORDER_ID, AMOUNT));
        when(tossPaymentClient.confirmPayment(any(), eq(IDEMPOTENCY_KEY))).thenReturn(tossDoneResponse(ORDER_ID, 1000));

        assertThatThrownBy(() -> paymentConfirmService.confirm(MEMBER_ID, IDEMPOTENCY_KEY, request))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(PaymentErrorStatus.TOSS_CONFIRM_FAILED)
                );

        verify(paymentConfirmTransactionService).failPayment(
                PAYMENT_ID,
                "INVALID_TOSS_CONFIRM_RESPONSE",
                "토스페이먼츠 결제 승인 응답이 결제 요청 정보와 일치하지 않습니다."
        );
        verify(paymentConfirmTransactionService, never()).approvePayment(any(), any());
    }

    // 결과가 불확실한 네 경로 모두 복구 서비스에 위임
    @ParameterizedTest
    @ValueSource(strings = {"emptyResponse", "transportFailure", "uncertainTossFailure", "approvalSaveFailure"})
    void confirm_uncertainResult_delegatesToReconciliation(String scenario) {
        PreparedPaymentConfirm prepared = PreparedPaymentConfirm.ready(PAYMENT_ID, ORDER_ID, AMOUNT);
        when(paymentConfirmTransactionService.prepareConfirm(MEMBER_ID, IDEMPOTENCY_KEY, confirmRequest()))
                .thenReturn(prepared);
        switch (scenario) {
            case "emptyResponse" -> when(tossPaymentClient.confirmPayment(any(), eq(IDEMPOTENCY_KEY))).thenReturn(null);
            case "transportFailure" -> when(tossPaymentClient.confirmPayment(any(), eq(IDEMPOTENCY_KEY)))
                    .thenThrow(new RestClientException("통신 실패"));
            case "uncertainTossFailure" -> when(tossPaymentClient.confirmPayment(any(), eq(IDEMPOTENCY_KEY)))
                    .thenThrow(new TossPaymentException(HttpStatus.INTERNAL_SERVER_ERROR, "UNKNOWN", "결과 불확실"));
            case "approvalSaveFailure" -> {
                TossPaymentConfirmResponse payment = tossDoneResponse(ORDER_ID, AMOUNT);
                when(tossPaymentClient.confirmPayment(any(), eq(IDEMPOTENCY_KEY))).thenReturn(payment);
                when(paymentConfirmTransactionService.approvePayment(PAYMENT_ID, payment))
                        .thenThrow(new IllegalStateException("승인 저장 실패"));
            }
            default -> throw new IllegalArgumentException(scenario);
        }
        when(paymentReconciliationService.reconcilePayment(eq(prepared), eq(PAYMENT_KEY), any(RuntimeException.class)))
                .thenReturn(doneResponse());

        assertThat(paymentConfirmService.confirm(MEMBER_ID, IDEMPOTENCY_KEY, confirmRequest())).isEqualTo(doneResponse());

        verify(paymentReconciliationService).reconcilePayment(eq(prepared), eq(PAYMENT_KEY), any(RuntimeException.class));
        verify(tossPaymentClient, never()).getPayment(any());
        verify(tossPaymentClient, never()).cancelPayment(any(), any());
    }

    private PaymentConfirmRequest confirmRequest() {
        return new PaymentConfirmRequest(PAYMENT_KEY, ORDER_ID, AMOUNT);
    }

    private PaymentConfirmResponse doneResponse() {
        return new PaymentConfirmResponse(
                PAYMENT_ID,
                ORDER_ID,
                PAYMENT_KEY,
                AMOUNT,
                PaymentStatus.DONE,
                LocalDate.of(2026, 5, 28),
                LocalTime.of(3, 39, 21)
        );
    }

    private TossPaymentConfirmResponse tossDoneResponse(String orderId, Integer totalAmount) {
        return tossResponse(orderId, totalAmount, "DONE");
    }

    private TossPaymentConfirmResponse tossResponse(String orderId, Integer totalAmount, String status) {
        return new TossPaymentConfirmResponse(
                "tosspayments",
                "2024-06-01",
                PAYMENT_KEY,
                orderId,
                "그룹잇 테스트 주문",
                status,
                "transaction-key",
                "카드",
                "NORMAL",
                "KRW",
                totalAmount,
                totalAmount,
                13636,
                1364,
                0,
                false,
                false,
                true,
                OffsetDateTime.parse("2026-05-28T03:38:27+09:00"),
                OffsetDateTime.parse("2026-05-28T03:39:21+09:00"),
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
