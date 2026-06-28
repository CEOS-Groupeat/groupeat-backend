package com.groupeat.domain.orders.service;

import com.groupeat.domain.orders.converter.OrderConverter;
import com.groupeat.domain.orders.dto.OrderRejectPreparation;
import com.groupeat.domain.orders.dto.response.OrderStatusChangeResponse;
import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.domain.orders.exception.OrderErrorStatus;
import com.groupeat.domain.orders.repository.OrderRepository;
import com.groupeat.domain.payment.dto.PaymentCancelResult;
import com.groupeat.domain.payment.entity.Payment;
import com.groupeat.domain.payment.enums.PaymentType;
import com.groupeat.domain.payment.exception.PaymentErrorStatus;
import com.groupeat.domain.payment.repository.PaymentRepository;
import com.groupeat.domain.settlement.entity.Settlement;
import com.groupeat.domain.settlement.repository.SettlementRepository;
import com.groupeat.domain.settlement.service.SettlementFeeCalculator;
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
    private final SettlementRepository settlementRepository;
    private final SettlementFeeCalculator settlementFeeCalculator;
    private final OrderScheduleValidationService orderScheduleValidationService;

    @Transactional
    public OrderStatusChangeResponse acceptOrder(Long ownerId, Long orderId) {
        Order order = orderRepository.findByIdAndStoreOwnerIdWithItems(orderId, ownerId)
                .orElseThrow(() -> new GeneralException(OrderErrorStatus.ORDER_NOT_FOUND));

        validateAcceptable(order);
        orderScheduleValidationService.validateOrderAcceptance(order);

        LocalDateTime acceptedAt = LocalDateTime.now();
        order.accept(acceptedAt);

        return OrderConverter.toOrderStatusChangeResponse(order, acceptedAt);
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
            int refundAmount,
            PaymentCancelResult paymentCancelResult
    ) {
        Order order = orderRepository.findByIdAndStoreOwnerId(orderId, ownerId)
                .orElseThrow(() -> new GeneralException(OrderErrorStatus.ORDER_NOT_FOUND));

        validateRejectable(order);

        LocalDateTime rejectedAt = LocalDateTime.now();
        order.reject(rejectedAt);

        paymentRepository.findByOrderId(order.getOrderId())
                .ifPresent(payment -> applyPaymentCancel(payment, refundAmount, paymentCancelResult));

        return OrderConverter.toOrderStatusChangeResponse(order, rejectedAt);
    }

    @Transactional
    public OrderStatusChangeResponse completePickup(Long ownerId, Long orderId) {
        Order order = orderRepository.findByIdAndStoreOwnerId(orderId, ownerId)
                .orElseThrow(() -> new GeneralException(OrderErrorStatus.ORDER_NOT_FOUND));

        validatePickupCompletable(order);

        Payment payment = paymentRepository.findByOrderId(order.getOrderId())
                .orElseThrow(() -> new GeneralException(PaymentErrorStatus.PAYMENT_NOT_FOUND));

        LocalDateTime pickupCompletedAt = LocalDateTime.now();
        order.completePickup(pickupCompletedAt);
        createSettlementIfAbsent(order, payment);

        return OrderConverter.toOrderStatusChangeResponse(order, pickupCompletedAt);
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

    private void validatePickupCompletable(Order order) {
        if (order.getOrderStatus() == OrderStatus.ACCEPTED) {
            return;
        }

        throw new GeneralException(OrderErrorStatus.ORDER_PICKUP_COMPLETE_NOT_ALLOWED);
    }

    private void applyPaymentCancel(Payment payment, int refundAmount, PaymentCancelResult paymentCancelResult) {
        if (!paymentCancelResult.canceled()) {
            return;
        }

        payment.cancel(refundAmount, paymentCancelResult.canceledAt(), paymentCancelResult.lastTransactionKey());
    }

    private void createSettlementIfAbsent(Order order, Payment payment) {
        if (settlementRepository.existsByOrderId(order.getId())) {
            return;
        }

        int orderAmount = payment.getTotalOrderAmount();
        int platformFeeAmount = settlementFeeCalculator.calculate(orderAmount); // 수수료 금액

        Settlement settlement = payment.getPaymentType() == PaymentType.PREPAID
                ? Settlement.payout(order, orderAmount, platformFeeAmount) // 선결제 주문 : 수수료 차감 후 점주에게 지급
                : Settlement.feeCharge(order, orderAmount, platformFeeAmount); // 현장결제 주문 : 플랫폼은 점주에게 수수료만 청구

        settlementRepository.save(settlement);
    }
}
