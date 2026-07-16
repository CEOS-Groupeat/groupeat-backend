package com.groupeat.domain.orders.service.scheduler;

import com.groupeat.domain.orders.config.OrderSchedulerProperties;
import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.domain.orders.service.OrderOwnerActionTransactionService;
import com.groupeat.domain.payment.dto.PaymentCancelResult;
import com.groupeat.domain.payment.entity.Payment;
import com.groupeat.domain.payment.enums.PaymentStatus;
import com.groupeat.domain.payment.repository.PaymentRepository;
import com.groupeat.domain.payment.service.PaymentCancelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderAutoRejectScheduler {

    private static final String AUTO_REJECT_CANCEL_REASON = "주문 수락 기한 초과로 자동 거절";
    private static final long ORDER_ACCEPT_DEADLINE_HOURS = 24;
    private static final int AUTO_REJECT_BATCH_SIZE = 100;

    private final OrderSchedulerProperties schedulerProperties;
    private final PaymentRepository paymentRepository;
    private final PaymentCancelService paymentCancelService;
    private final OrderOwnerActionTransactionService orderOwnerActionTransactionService;

    // 결제 완료 후 24시간 동안 미처리된 주문을 자동 거절하고 결제를 취소
    @Scheduled(fixedDelayString = "${app.order.scheduler.auto-reject-fixed-delay-ms}")
    public void autoRejectExpiredOrders() {
        if (!schedulerProperties.enabled()) {
            return;
        }

        LocalDateTime approvedAtOrBefore = LocalDateTime.now().minusHours(ORDER_ACCEPT_DEADLINE_HOURS);
        List<Payment> payments = paymentRepository.findAllAutoRejectCandidates(
                PaymentStatus.DONE,
                OrderStatus.PAID,
                approvedAtOrBefore,
                PageRequest.of(0, AUTO_REJECT_BATCH_SIZE)
        );

        for (Payment payment : payments) {
            rejectExpiredOrder(payment);
        }
    }

    private void rejectExpiredOrder(Payment payment) {
        Order order = payment.getOrder();
        int refundAmount = order.getPaymentAmount();
        PaymentCancelResult paymentCancelResult = null;

        try {
            paymentCancelResult = paymentCancelService.cancel(
                    payment,
                    AUTO_REJECT_CANCEL_REASON,
                    refundAmount
            );
            orderOwnerActionTransactionService.rejectExpiredOrder(
                    order.getId(),
                    refundAmount,
                    paymentCancelResult
            );
            log.info("Expired order auto rejected. orderId={}, paymentId={}", order.getId(), payment.getId());
        } catch (RuntimeException e) {
            log.warn(
                    "Expired order auto reject failed. orderId={}, paymentId={}",
                    order.getId(),
                    payment.getId(),
                    e
            );
            if (paymentCancelResult != null && paymentCancelResult.canceled()) {
                log.error(
                        "Payment cancel succeeded but auto reject persistence failed. orderId={}, paymentId={}, refundedAmount={}, canceledAt={}, lastTransactionKey={}",
                        order.getId(),
                        payment.getId(),
                        paymentCancelResult.refundedAmount(),
                        paymentCancelResult.canceledAt(),
                        paymentCancelResult.lastTransactionKey()
                );
            }
        }
    }
}
