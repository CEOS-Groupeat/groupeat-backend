package com.groupeat.domain.payment.service;

import com.groupeat.domain.cart.service.CartService;
import com.groupeat.domain.notification.event.NewOrderRequestNotificationEvent;
import com.groupeat.domain.payment.converter.PaymentConverter;
import com.groupeat.domain.payment.dto.PreparedPaymentConfirm;
import com.groupeat.domain.payment.dto.request.PaymentConfirmRequest;
import com.groupeat.domain.payment.dto.response.PaymentConfirmResponse;
import com.groupeat.domain.payment.dto.toss.TossPaymentConfirmResponse;
import com.groupeat.domain.payment.entity.Payment;
import com.groupeat.domain.payment.enums.PaymentStatus;
import com.groupeat.domain.payment.exception.PaymentErrorStatus;
import com.groupeat.domain.payment.repository.PaymentRepository;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PaymentConfirmTransactionService {

    private final PaymentRepository paymentRepository;
    private final CartService cartService;
    private final ApplicationEventPublisher eventPublisher;

    // 결제 승인 전 검증을 수행하고 승인 진행 상태로 저장
    @Transactional
    public PreparedPaymentConfirm prepareConfirm(Long memberId, PaymentConfirmRequest request) {
        Payment payment = paymentRepository.findByOrderId(request.orderId())
                .orElseThrow(() -> new GeneralException(PaymentErrorStatus.PAYMENT_NOT_FOUND));

        validateOwner(payment, memberId);
        validateAmount(payment, request.amount());

        if (payment.getPaymentStatus() == PaymentStatus.DONE) {
            validateAlreadyConfirmedPayment(payment, request.paymentKey());
            return PreparedPaymentConfirm.alreadyConfirmed(PaymentConverter.toConfirmResponse(payment));
        }

        validateConfirmableStatus(payment);
        payment.markInProgress(request.paymentKey());
        return PreparedPaymentConfirm.ready(payment.getId(), payment.getOrderId(), payment.getPaidAmount());
    }

    // 토스 승인 성공 응답을 결제 완료 상태로 저장
    @Transactional
    public PaymentConfirmResponse approvePayment(Long paymentId, TossPaymentConfirmResponse tossResponse) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new GeneralException(PaymentErrorStatus.PAYMENT_NOT_FOUND));

        payment.approve(
                tossResponse.paymentKey(),
                tossResponse.method(),
                toLocalDateTime(tossResponse.approvedAt()),
                tossResponse.lastTransactionKey(),
                tossResponse.isPartialCancelable(),
                tossResponse.receipt() != null ? tossResponse.receipt().url() : null,
                tossResponse.card() != null ? tossResponse.card().approveNo() : null
        );
        markOrderPaid(payment);
        publishNewOrderRequestNotificationEvent(payment); // 주문 요청 이벤트 발행

        // 결제가 성공했으므로 해당 유저의 장바구니 비우기
        cartService.clearCart(payment.getMemberId());

        return PaymentConverter.toConfirmResponse(payment);
    }

    // 결제 실패 상태 저장
    @Transactional
    public void failPayment(Long paymentId, String failureCode, String failureMessage) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new GeneralException(PaymentErrorStatus.PAYMENT_NOT_FOUND));
        // TODO: 결제 실패 보상 PR에서 실패 유형별 재시도/조회/취소 상태 전이를 정리
        payment.fail(failureCode, failureMessage);
    }

    // 결제 요청자가 현재 로그인 사용자와 같은지 검증
    private void validateOwner(Payment payment, Long memberId) {
        if (!payment.getMemberId().equals(memberId)) {
            throw new GeneralException(PaymentErrorStatus.PAYMENT_FORBIDDEN);
        }
    }

    // 프론트에서 넘어온 금액이 서버에 저장된 실제 PG 결제 금액과 같은지 검증
    private void validateAmount(Payment payment, Integer requestAmount) {
        if (!payment.getPaidAmount().equals(requestAmount)) {
            throw new GeneralException(PaymentErrorStatus.PAYMENT_AMOUNT_MISMATCH);
        }
    }

    // 이미 승인된 결제의 중복 요청인지, 다른 paymentKey를 사용한 비정상 요청인지 검증
    private void validateAlreadyConfirmedPayment(Payment payment, String requestPaymentKey) {
        if (!Objects.equals(payment.getPaymentKey(), requestPaymentKey)) {
            throw new GeneralException(PaymentErrorStatus.PAYMENT_ALREADY_CONFIRMED);
        }
    }

    // 승인 요청은 아직 처리되지 않은 READY 상태에서만 허용
    private void validateConfirmableStatus(Payment payment) {
        if (payment.getPaymentStatus() == PaymentStatus.READY) {
            return;
        }

        throw new GeneralException(PaymentErrorStatus.PAYMENT_INVALID_STATUS);
    }

    private void markOrderPaid(Payment payment) {
        if (payment.getOrder() == null) {
            return;
        }

        payment.getOrder().markPaid();
    }

    // 결제 완료로 신규 주문 요청 알림 이벤트 발행
    private void publishNewOrderRequestNotificationEvent(Payment payment) {
        if (payment.getOrder() == null) {
            return;
        }

        eventPublisher.publishEvent(new NewOrderRequestNotificationEvent(payment.getOrder().getId()));
    }

    // 토스 응답의 OffsetDateTime을 엔티티에서 사용하는 LocalDateTime으로 변환
    private LocalDateTime toLocalDateTime(OffsetDateTime offsetDateTime) {
        return offsetDateTime != null ? offsetDateTime.toLocalDateTime() : null;
    }
}
