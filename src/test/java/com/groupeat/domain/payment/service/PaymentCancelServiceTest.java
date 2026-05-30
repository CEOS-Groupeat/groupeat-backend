package com.groupeat.domain.payment.service;

import com.groupeat.domain.payment.client.TossPaymentClient;
import com.groupeat.domain.payment.dto.PaymentCancelResult;
import com.groupeat.domain.payment.dto.toss.TossPaymentCancelRequest;
import com.groupeat.domain.payment.dto.toss.TossPaymentConfirmResponse;
import com.groupeat.domain.payment.entity.Payment;
import com.groupeat.domain.payment.enums.PaymentProvider;
import com.groupeat.domain.payment.enums.PaymentStatus;
import com.groupeat.domain.payment.enums.PaymentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PaymentCancelServiceTest {

    private TossPaymentClient tossPaymentClient;
    private PaymentCancelService paymentCancelService;

    @BeforeEach
    void setUp() {
        tossPaymentClient = mock(TossPaymentClient.class);
        paymentCancelService = new PaymentCancelService(tossPaymentClient);
    }

    @Test
    void cancel_skipsWhenPaymentHasNoPaymentKey() {
        Payment payment = payment(PaymentStatus.DONE, null, 10000);

        PaymentCancelResult result = paymentCancelService.cancel(payment, "고객 요청", 5000);

        assertThat(result.canceled()).isFalse();
        verifyNoInteractions(tossPaymentClient);
    }

    @Test
    void cancel_requestsPartialCancelAmount() {
        Payment payment = payment(PaymentStatus.DONE, "payment-key", 10000);
        TossPaymentConfirmResponse response = tossCancelResponse(5000);
        when(tossPaymentClient.cancelPayment(eq("payment-key"), any(TossPaymentCancelRequest.class)))
                .thenReturn(response);

        PaymentCancelResult result = paymentCancelService.cancel(payment, "고객 요청", 5000);

        ArgumentCaptor<TossPaymentCancelRequest> captor = ArgumentCaptor.forClass(TossPaymentCancelRequest.class);
        verify(tossPaymentClient).cancelPayment(eq("payment-key"), captor.capture());
        assertThat(captor.getValue().cancelAmount()).isEqualTo(5000);
        assertThat(result.canceled()).isTrue();
        assertThat(result.refundedAmount()).isEqualTo(5000);
        assertThat(result.lastTransactionKey()).isEqualTo("cancel-transaction-key");
    }

    @Test
    void cancel_omitsCancelAmountForFullCancel() {
        Payment payment = payment(PaymentStatus.DONE, "payment-key", 10000);
        TossPaymentConfirmResponse response = tossCancelResponse(10000);
        when(tossPaymentClient.cancelPayment(eq("payment-key"), any(TossPaymentCancelRequest.class)))
                .thenReturn(response);

        paymentCancelService.cancel(payment, "고객 요청", 10000);

        ArgumentCaptor<TossPaymentCancelRequest> captor = ArgumentCaptor.forClass(TossPaymentCancelRequest.class);
        verify(tossPaymentClient).cancelPayment(eq("payment-key"), captor.capture());
        assertThat(captor.getValue().cancelAmount()).isNull();
    }

    private Payment payment(PaymentStatus paymentStatus, String paymentKey, Integer paidAmount) {
        return Payment.builder()
                .orderId("ORDER_TEST")
                .memberId(1L)
                .paymentKey(paymentKey)
                .paymentType(PaymentType.PREPAID)
                .paymentProvider(PaymentProvider.TOSS)
                .totalOrderAmount(paidAmount)
                .paidAmount(paidAmount)
                .remainingAmount(0)
                .paymentStatus(paymentStatus)
                .build();
    }

    private TossPaymentConfirmResponse tossCancelResponse(Integer cancelAmount) {
        OffsetDateTime canceledAt = OffsetDateTime.now();
        return new TossPaymentConfirmResponse(
                "mid",
                "2022-11-16",
                "payment-key",
                "ORDER_TEST",
                "테스트 주문",
                "CANCELED",
                "cancel-transaction-key",
                "카드",
                "NORMAL",
                "KRW",
                10000,
                10000 - cancelAmount,
                null,
                null,
                null,
                false,
                false,
                true,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(new TossPaymentConfirmResponse.Cancel(
                        "고객 요청",
                        cancelAmount,
                        null,
                        10000 - cancelAmount,
                        null,
                        null,
                        canceledAt,
                        "cancel-transaction-key",
                        null,
                        "DONE",
                        null
                )),
                null
        );
    }
}
