package com.groupeat.domain.orders.service;

import com.groupeat.domain.orders.dto.OrderCancelPreparation;
import com.groupeat.domain.orders.dto.response.OrderCancelResponse;
import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.orders.enums.OrderCancelledBy;
import com.groupeat.domain.orders.exception.OrderErrorStatus;
import com.groupeat.domain.orders.repository.OrderRepository;
import com.groupeat.domain.payment.dto.PaymentCancelResult;
import com.groupeat.domain.payment.entity.Payment;
import com.groupeat.domain.payment.enums.PaymentStatus;
import com.groupeat.domain.payment.repository.PaymentRepository;
import com.groupeat.domain.store.repository.StoreOrderScheduleRepository;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderCancelTransactionService {

    private static final int FULL_REFUND_RATE = 100;
    private static final int HALF_REFUND_RATE = 50;

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final StoreOrderScheduleRepository storeOrderScheduleRepository;

    @Transactional(readOnly = true)
    public OrderCancelPreparation prepareCustomerCancel(Long memberId, Long orderId) {
        Order order = orderRepository.findByIdAndMemberId(orderId, memberId)
                .orElseThrow(() -> new GeneralException(OrderErrorStatus.ORDER_NOT_FOUND));

        validateCustomerCancelable(order);

        int refundRate = calculateCustomerCancelRefundRate(order);
        int refundAmount = calculateRefundAmount(order.getPaymentAmount(), refundRate);
        Payment payment = paymentRepository.findFirstByOrderId(order.getOrderId()).orElse(null);

        return new OrderCancelPreparation(refundRate, refundAmount, payment);
    }

    @Transactional
    public OrderCancelResponse cancelCustomerOrder(
            Long memberId,
            Long orderId,
            String cancelReason,
            int refundRate,
            int refundAmount,
            PaymentCancelResult paymentCancelResult
    ) {
        Order order = orderRepository.findByIdAndMemberId(orderId, memberId)
                .orElseThrow(() -> new GeneralException(OrderErrorStatus.ORDER_NOT_FOUND));

        validateCustomerCancelable(order);

        LocalDateTime cancelledAt = LocalDateTime.now();
        order.cancel(
                cancelReason,
                OrderCancelledBy.CUSTOMER,
                memberId,
                refundRate,
                refundAmount,
                cancelledAt
        );

        paymentRepository.findByOrderId(order.getOrderId())
                .ifPresent(payment -> applyPaymentCancel(payment, refundAmount, paymentCancelResult));

        return new OrderCancelResponse(order.getId(), order.getOrderStatus(), refundRate, refundAmount, cancelledAt);
    }

    private void validateCustomerCancelable(Order order) {
        switch (order.getOrderStatus()) {
            case PENDING, PAID, ACCEPTED -> {
                return;
            }
            default -> throw new GeneralException(OrderErrorStatus.ORDER_CANCEL_NOT_ALLOWED);
        }
    }

    private void applyPaymentCancel(Payment payment, int refundAmount, PaymentCancelResult paymentCancelResult) {
        if (!paymentCancelResult.canceled()) {
            return;
        }

        if (refundAmount >= payment.getPaidAmount()) {
            payment.cancel(refundAmount, paymentCancelResult.canceledAt(), paymentCancelResult.lastTransactionKey());
            return;
        }

        payment.partialCancel(refundAmount, paymentCancelResult.canceledAt(), paymentCancelResult.lastTransactionKey());
    }

    private int calculateCustomerCancelRefundRate(Order order) {
        Integer minOrderDays = storeOrderScheduleRepository
                .findActiveScheduleByStoreIdAndDate(order.getStore().getId(), order.getPickupDate())
                .map(schedule -> schedule.getMinOrderDays())
                .orElse(null);
        if (minOrderDays == null) {
            return HALF_REFUND_RATE;
        }

        java.time.LocalDate refundDeadline = order.getPickupDate().minusDays(minOrderDays);
        if (!java.time.LocalDate.now().isAfter(refundDeadline)) {
            return FULL_REFUND_RATE;
        }

        return HALF_REFUND_RATE;
    }

    private int calculateRefundAmount(Integer paymentAmount, int refundRate) {
        return paymentAmount * refundRate / 100;
    }
}
