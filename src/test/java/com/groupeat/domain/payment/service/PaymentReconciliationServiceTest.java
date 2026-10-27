package com.groupeat.domain.payment.service;

import com.groupeat.domain.payment.client.TossPaymentClient;
import com.groupeat.domain.payment.dto.PreparedPaymentConfirm;
import com.groupeat.domain.payment.dto.response.PaymentConfirmResponse;
import com.groupeat.domain.payment.dto.toss.TossPaymentConfirmResponse;
import com.groupeat.domain.payment.enums.PaymentStatus;
import com.groupeat.domain.payment.exception.PaymentErrorStatus;
import com.groupeat.global.exception.GeneralException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentReconciliationServiceTest {

    private static final Long PAYMENT_ID = 1L;
    private static final String ORDER_ID = "ORDER_TEST_001";
    private static final String PAYMENT_KEY = "tgen_20260528033827CH4N9";
    private static final int AMOUNT = 15000;

    @Mock
    private TossPaymentClient tossPaymentClient;

    @Mock
    private PaymentConfirmTransactionService paymentConfirmTransactionService;

    private final PreparedPaymentConfirm preparedPayment = PreparedPaymentConfirm.ready(PAYMENT_ID, ORDER_ID, AMOUNT);
    private final RuntimeException initialFailure = new IllegalStateException("승인 결과 불확실");
    private PaymentReconciliationService paymentReconciliationService;

    @BeforeEach
    void setUp() {
        paymentReconciliationService = new PaymentReconciliationService(tossPaymentClient, paymentConfirmTransactionService);
    }

    @Test
    void reconcile_approvedPayment_returnsSuccess() {
        TossPaymentConfirmResponse payment = tossDoneResponse(ORDER_ID, AMOUNT);
        when(tossPaymentClient.getPayment(PAYMENT_KEY)).thenReturn(payment);
        when(paymentConfirmTransactionService.approvePayment(PAYMENT_ID, payment)).thenReturn(doneResponse());

        assertThat(paymentReconciliationService.reconcilePayment(preparedPayment, PAYMENT_KEY, initialFailure))
                .isEqualTo(doneResponse());
        verify(tossPaymentClient, never()).cancelPayment(any(), any());
        verify(paymentConfirmTransactionService, never()).markReconciliationRequired(any(), any(), any());
    }

    @Test
    void reconcile_reconciliationLookupFails_recordsReconciliationRequired() {
        when(tossPaymentClient.getPayment(PAYMENT_KEY)).thenThrow(new IllegalStateException("조회 실패"));

        assertReconciliationError(PaymentErrorStatus.PAYMENT_RECONCILIATION_REQUIRED);

        verify(paymentConfirmTransactionService).markReconciliationRequired(eq(PAYMENT_ID), any(), any());
        verify(paymentConfirmTransactionService, never()).approvePayment(any(), any());
        verify(tossPaymentClient, never()).cancelPayment(any(), any());
    }

    @Test
    void reconcile_reconciledApprovalSaveFails_attemptsCancel() {
        TossPaymentConfirmResponse payment = tossDoneResponse(ORDER_ID, AMOUNT);
        when(tossPaymentClient.getPayment(PAYMENT_KEY)).thenReturn(payment);
        when(paymentConfirmTransactionService.approvePayment(PAYMENT_ID, payment))
                .thenThrow(new IllegalStateException("승인 저장 실패"));
        when(tossPaymentClient.cancelPayment(eq(PAYMENT_KEY), any()))
                .thenThrow(new IllegalStateException("취소 실패"));

        assertReconciliationError(PaymentErrorStatus.PAYMENT_RECONCILIATION_REQUIRED);

        verify(paymentConfirmTransactionService).prepareApprovalFailureCancel(PAYMENT_ID, PAYMENT_KEY);
        verify(tossPaymentClient).cancelPayment(eq(PAYMENT_KEY), any());
        verify(paymentConfirmTransactionService).markReconciliationRequired(eq(PAYMENT_ID), any(), any());
    }

    @Test
    void reconcile_reconciledFailure_recordsFailureWithoutReconciliationRequired() {
        when(tossPaymentClient.getPayment(PAYMENT_KEY)).thenReturn(tossResponse(ORDER_ID, AMOUNT, "EXPIRED"));

        assertReconciliationError(PaymentErrorStatus.TOSS_CONFIRM_FAILED);

        verify(paymentConfirmTransactionService).failPayment(PAYMENT_ID, "EXPIRED", "토스 결제 승인 실패 또는 만료");
        verify(paymentConfirmTransactionService, never()).markReconciliationRequired(any(), any(), any());
        verify(tossPaymentClient, never()).cancelPayment(any(), any());
    }

    @Test
    void reconcile_reconciledFailureSaveFails_recordsReconciliationRequiredWithoutCancel() {
        when(tossPaymentClient.getPayment(PAYMENT_KEY)).thenReturn(tossResponse(ORDER_ID, AMOUNT, "EXPIRED"));
        doThrow(new IllegalStateException("실패 저장 실패"))
                .when(paymentConfirmTransactionService).failPayment(eq(PAYMENT_ID), any(), any());

        assertReconciliationError(PaymentErrorStatus.PAYMENT_RECONCILIATION_REQUIRED);

        verify(paymentConfirmTransactionService).markReconciliationRequired(eq(PAYMENT_ID), any(), any());
        verify(tossPaymentClient, never()).cancelPayment(any(), any());
    }

    private void assertReconciliationError(PaymentErrorStatus expected) {
        assertThatThrownBy(() -> paymentReconciliationService.reconcilePayment(preparedPayment, PAYMENT_KEY, initialFailure))
                .isInstanceOfSatisfying(GeneralException.class, e -> assertThat(e.getCode()).isEqualTo(expected));
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
