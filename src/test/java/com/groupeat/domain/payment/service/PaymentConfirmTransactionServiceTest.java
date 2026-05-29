package com.groupeat.domain.payment.service;

import com.groupeat.domain.payment.dto.PreparedPaymentConfirm;
import com.groupeat.domain.payment.dto.request.PaymentConfirmRequest;
import com.groupeat.domain.payment.dto.response.PaymentConfirmResponse;
import com.groupeat.domain.payment.dto.toss.TossPaymentConfirmResponse;
import com.groupeat.domain.payment.entity.Payment;
import com.groupeat.domain.payment.enums.PaymentProvider;
import com.groupeat.domain.payment.enums.PaymentStatus;
import com.groupeat.domain.payment.enums.PaymentType;
import com.groupeat.domain.payment.exception.PaymentErrorStatus;
import com.groupeat.domain.payment.repository.PaymentRepository;
import com.groupeat.global.exception.GeneralException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentConfirmTransactionServiceTest {

    private static final Long MEMBER_ID = 2L;
    private static final Long PAYMENT_ID = 1L;
    private static final String ORDER_ID = "ORDER_TEST_001";
    private static final String PAYMENT_KEY = "tgen_20260528033827CH4N9";
    private static final int AMOUNT = 15000;

    @Mock
    private PaymentRepository paymentRepository;

    private PaymentConfirmTransactionService paymentConfirmTransactionService;

    @BeforeEach
    void setUp() {
        paymentConfirmTransactionService = new PaymentConfirmTransactionService(paymentRepository);
    }

    // 결제 준비 상태면 승인 진행 상태로 변경한다.
    @Test
    void prepareConfirm_readyPayment_marksInProgress() {
        Payment payment = readyPayment();
        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(payment));

        PreparedPaymentConfirm preparedPayment = paymentConfirmTransactionService.prepareConfirm(
                MEMBER_ID,
                confirmRequest(AMOUNT)
        );

        assertThat(preparedPayment.paymentId()).isEqualTo(PAYMENT_ID);
        assertThat(preparedPayment.orderId()).isEqualTo(ORDER_ID);
        assertThat(preparedPayment.paidAmount()).isEqualTo(AMOUNT);
        assertThat(preparedPayment.alreadyConfirmedResponse()).isNull();
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.IN_PROGRESS);
        assertThat(payment.getPaymentKey()).isEqualTo(PAYMENT_KEY);
    }

    // 결제 소유자가 아니면 접근을 차단한다.
    @Test
    void prepareConfirm_differentOwner_throwsForbidden() {
        Payment payment = readyPayment();
        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentConfirmTransactionService.prepareConfirm(999L, confirmRequest(AMOUNT)))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(PaymentErrorStatus.PAYMENT_FORBIDDEN)
                );
    }

    // 요청 금액이 서버 결제 금액과 다르면 승인을 차단한다.
    @Test
    void prepareConfirm_amountMismatch_throwsAmountMismatch() {
        Payment payment = readyPayment();
        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentConfirmTransactionService.prepareConfirm(MEMBER_ID, confirmRequest(1000)))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(PaymentErrorStatus.PAYMENT_AMOUNT_MISMATCH)
                );
    }

    // 이미 승인된 결제가 같은 paymentKey로 재요청되면 기존 성공 응답을 반환한다.
    @Test
    void prepareConfirm_donePaymentWithSamePaymentKey_returnsAlreadyConfirmedResponse() {
        Payment payment = donePayment();
        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(payment));

        PreparedPaymentConfirm preparedPayment = paymentConfirmTransactionService.prepareConfirm(
                MEMBER_ID,
                confirmRequest(AMOUNT)
        );

        PaymentConfirmResponse response = preparedPayment.alreadyConfirmedResponse();
        assertThat(response).isNotNull();
        assertThat(response.paymentId()).isEqualTo(PAYMENT_ID);
        assertThat(response.status()).isEqualTo(PaymentStatus.DONE);
    }

    // 이미 승인 진행 중인 결제는 중복 승인 요청을 차단한다.
    @Test
    void prepareConfirm_inProgressPayment_throwsInvalidStatus() {
        Payment payment = inProgressPayment();
        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentConfirmTransactionService.prepareConfirm(MEMBER_ID, confirmRequest(AMOUNT)))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(PaymentErrorStatus.PAYMENT_INVALID_STATUS)
                );
    }

    // 토스 승인 성공 응답을 결제 완료 상태로 저장한다.
    @Test
    void approvePayment_updatesPaymentToDone() {
        Payment payment = inProgressPayment();
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));

        PaymentConfirmResponse response = paymentConfirmTransactionService.approvePayment(
                PAYMENT_ID,
                tossDoneResponse(ORDER_ID, AMOUNT)
        );

        assertThat(response.status()).isEqualTo(PaymentStatus.DONE);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.DONE);
        assertThat(payment.getMethod()).isEqualTo("카드");
        assertThat(payment.getReceiptUrl()).isEqualTo("https://receipt.test");
        assertThat(payment.getCardApproveNo()).isEqualTo("00000000");
    }

    // 결제 실패 코드와 메시지를 저장하고 실패 상태로 변경한다.
    @Test
    void failPayment_updatesPaymentToFailed() {
        Payment payment = inProgressPayment();
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));

        paymentConfirmTransactionService.failPayment(PAYMENT_ID, "NOT_FOUND_PAYMENT_SESSION", "결제 시간이 만료되었습니다.");

        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(payment.getFailureCode()).isEqualTo("NOT_FOUND_PAYMENT_SESSION");
        assertThat(payment.getFailureMessage()).isEqualTo("결제 시간이 만료되었습니다.");
    }

    private PaymentConfirmRequest confirmRequest(Integer amount) {
        return new PaymentConfirmRequest(PAYMENT_KEY, ORDER_ID, amount);
    }

    private Payment readyPayment() {
        return Payment.builder()
                .id(PAYMENT_ID)
                .orderId(ORDER_ID)
                .memberId(MEMBER_ID)
                .paymentType(PaymentType.PREPAID)
                .paymentProvider(PaymentProvider.TOSS)
                .totalOrderAmount(AMOUNT)
                .paidAmount(AMOUNT)
                .remainingAmount(0)
                .paymentStatus(PaymentStatus.READY)
                .build();
    }

    private Payment inProgressPayment() {
        Payment payment = readyPayment();
        payment.markInProgress(PAYMENT_KEY);
        return payment;
    }

    private Payment donePayment() {
        Payment payment = readyPayment();
        payment.approve(PAYMENT_KEY, "카드", null, "transaction-key", true, "https://receipt.test", "00000000");
        return payment;
    }

    private TossPaymentConfirmResponse tossDoneResponse(String orderId, Integer totalAmount) {
        return new TossPaymentConfirmResponse(
                "tosspayments",
                "2024-06-01",
                PAYMENT_KEY,
                orderId,
                "그룹잇 테스트 주문",
                "DONE",
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
                new TossPaymentConfirmResponse.Card(
                        "61",
                        "31",
                        "12345678****789*",
                        0,
                        false,
                        null,
                        "00000000",
                        false,
                        "신용",
                        "개인",
                        "READY",
                        totalAmount
                ),
                null,
                new TossPaymentConfirmResponse.Receipt("https://receipt.test"),
                null,
                null,
                null,
                null
        );
    }
}
