package com.groupeat.domain.orders.service;

import com.groupeat.domain.orders.dto.OrderRejectPreparation;
import com.groupeat.domain.orders.dto.response.OrderStatusChangeResponse;
import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.domain.orders.exception.OrderErrorStatus;
import com.groupeat.domain.orders.repository.OrderRepository;
import com.groupeat.domain.payment.dto.PaymentCancelResult;
import com.groupeat.domain.payment.entity.Payment;
import com.groupeat.domain.payment.repository.PaymentRepository;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderOwnerActionTransactionService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public OrderStatusChangeResponse acceptOrder(Long ownerId, Long orderId) {
        Order order = orderRepository.findByIdAndStoreOwnerId(orderId, ownerId)
                .orElseThrow(() -> new GeneralException(OrderErrorStatus.ORDER_NOT_FOUND));

        validateAcceptable(order);

        LocalDateTime acceptedAt = LocalDateTime.now();
        order.accept(acceptedAt);

        return new OrderStatusChangeResponse(order.getId(), order.getOrderStatus(), acceptedAt);
    }

    @Transactional(readOnly = true)
    public OrderRejectPreparation prepareRejectOrder(Long ownerId, Long orderId) {
        Order order = orderRepository.findByIdAndStoreOwnerId(orderId, ownerId)
                .orElseThrow(() -> new GeneralException(OrderErrorStatus.ORDER_NOT_FOUND));

        validateRejectable(order);

        Payment payment = paymentRepository.findFirstByOrderId(order.getOrderId()).orElse(null);
        return new OrderRejectPreparation(order.getPaymentAmount(), payment);
    }

    @Transactional
    public OrderStatusChangeResponse rejectOrder(
            Long ownerId,
            Long orderId,
            String rejectReason,
            int refundAmount,
            PaymentCancelResult paymentCancelResult
    ) {
        Order order = orderRepository.findByIdAndStoreOwnerId(orderId, ownerId)
                .orElseThrow(() -> new GeneralException(OrderErrorStatus.ORDER_NOT_FOUND));

        validateRejectable(order);

        LocalDateTime rejectedAt = LocalDateTime.now();
        order.reject(rejectReason, rejectedAt);

        paymentRepository.findByOrderId(order.getOrderId())
                .ifPresent(payment -> applyPaymentCancel(payment, refundAmount, paymentCancelResult));

        return new OrderStatusChangeResponse(order.getId(), order.getOrderStatus(), rejectedAt);
    }

    private void validateAcceptable(Order order) {
        if (order.getOrderStatus() == OrderStatus.PAID) {
            return;
        }

        throw new GeneralException(OrderErrorStatus.ORDER_ACCEPT_NOT_ALLOWED);
    }

    private void validateRejectable(Order order) {
        if (order.getOrderStatus() == OrderStatus.PAID) {
            return;
        }

        throw new GeneralException(OrderErrorStatus.ORDER_REJECT_NOT_ALLOWED);
    }

    private void applyPaymentCancel(Payment payment, int refundAmount, PaymentCancelResult paymentCancelResult) {
        if (!paymentCancelResult.canceled()) {
            return;
        }

        payment.cancel(refundAmount, paymentCancelResult.canceledAt(), paymentCancelResult.lastTransactionKey());
    }
}
