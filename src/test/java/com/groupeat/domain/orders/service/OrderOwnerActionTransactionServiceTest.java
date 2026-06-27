package com.groupeat.domain.orders.service;

import com.groupeat.domain.orders.dto.OrderRejectPreparation;
import com.groupeat.domain.orders.dto.response.OrderStatusChangeResponse;
import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.domain.orders.exception.OrderErrorStatus;
import com.groupeat.domain.orders.repository.OrderRepository;
import com.groupeat.domain.payment.dto.PaymentCancelResult;
import com.groupeat.domain.payment.entity.Payment;
import com.groupeat.domain.payment.enums.PaymentProvider;
import com.groupeat.domain.payment.enums.PaymentStatus;
import com.groupeat.domain.payment.enums.PaymentType;
import com.groupeat.domain.payment.repository.PaymentRepository;
import com.groupeat.domain.settlement.entity.Settlement;
import com.groupeat.domain.settlement.enums.SettlementType;
import com.groupeat.domain.settlement.repository.SettlementRepository;
import com.groupeat.domain.settlement.service.SettlementFeeCalculator;
import com.groupeat.domain.store.entity.Store;
import com.groupeat.global.exception.GeneralException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderOwnerActionTransactionServiceTest {

    private static final Long OWNER_ID = 2L;
    private static final Long ORDER_ID = 10L;

    private OrderRepository orderRepository;
    private PaymentRepository paymentRepository;
    private SettlementRepository settlementRepository;
    private SettlementFeeCalculator settlementFeeCalculator;
    private OrderScheduleValidationService orderScheduleValidationService;
    private OrderOwnerActionTransactionService orderOwnerActionTransactionService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        paymentRepository = mock(PaymentRepository.class);
        settlementRepository = mock(SettlementRepository.class);
        settlementFeeCalculator = mock(SettlementFeeCalculator.class);
        orderScheduleValidationService = mock(OrderScheduleValidationService.class);
        orderOwnerActionTransactionService = new OrderOwnerActionTransactionService(
                orderRepository,
                paymentRepository,
                settlementRepository,
                settlementFeeCalculator,
                orderScheduleValidationService
        );
    }

    @Test
    void acceptOrder_changesStatusToAccepted() {
        Order order = order(OrderStatus.PAID);
        when(orderRepository.findByIdAndStoreOwnerIdWithItems(ORDER_ID, OWNER_ID)).thenReturn(Optional.of(order));

        OrderStatusChangeResponse response = orderOwnerActionTransactionService.acceptOrder(OWNER_ID, ORDER_ID);

        assertThat(response.orderStatus()).isEqualTo(OrderStatus.ACCEPTED);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.ACCEPTED);
        assertThat(order.getAcceptedAt()).isNotNull();
        verify(orderScheduleValidationService).validateOrderAcceptance(order);
    }

    @Test
    void acceptOrder_rejectsInvalidStatus() {
        Order order = order(OrderStatus.ACCEPTED);
        when(orderRepository.findByIdAndStoreOwnerIdWithItems(ORDER_ID, OWNER_ID)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderOwnerActionTransactionService.acceptOrder(OWNER_ID, ORDER_ID))
                .isInstanceOf(GeneralException.class)
                .extracting("code")
                .isEqualTo(OrderErrorStatus.ORDER_ACCEPT_NOT_ALLOWED);
        verify(orderScheduleValidationService, never()).validateOrderAcceptance(any(Order.class));
    }

    @Test
    void rejectOrder_changesStatusAndCancelsPayment() {
        Order order = order(OrderStatus.PAID);
        Payment payment = payment(order);
        when(orderRepository.findByIdAndStoreOwnerId(ORDER_ID, OWNER_ID)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(order.getOrderId())).thenReturn(Optional.of(payment));

        OrderStatusChangeResponse response = orderOwnerActionTransactionService.rejectOrder(
                OWNER_ID,
                ORDER_ID,
                "재료 소진",
                10000,
                new PaymentCancelResult(10000, LocalDateTime.now(), "cancel-tx", true)
        );

        assertThat(response.orderStatus()).isEqualTo(OrderStatus.REJECTED);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.REJECTED);
        assertThat(order.getRejectReason()).isEqualTo("재료 소진");
        assertThat(order.getRejectedAt()).isNotNull();
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELED);
        assertThat(payment.getRefundedAmount()).isEqualTo(10000);
        assertThat(payment.getLastTransactionKey()).isEqualTo("cancel-tx");
    }

    @Test
    void prepareRejectOrder_returnsFullRefundAmount() {
        Order order = order(OrderStatus.PAID);
        Payment payment = payment(order);
        when(orderRepository.findByIdAndStoreOwnerId(ORDER_ID, OWNER_ID)).thenReturn(Optional.of(order));
        when(paymentRepository.findFirstByOrderId(order.getOrderId())).thenReturn(Optional.of(payment));

        OrderRejectPreparation preparation = orderOwnerActionTransactionService.prepareRejectOrder(OWNER_ID, ORDER_ID);

        assertThat(preparation.refundAmount()).isEqualTo(10000);
        assertThat(preparation.payment()).isEqualTo(payment);
    }

    @Test
    void completePickup_changesStatusAndCreatesPayoutSettlementForPrepaidOrder() {
        Order order = order(OrderStatus.ACCEPTED);
        Payment payment = payment(order);
        when(orderRepository.findByIdAndStoreOwnerId(ORDER_ID, OWNER_ID)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(order.getOrderId())).thenReturn(Optional.of(payment));
        when(settlementRepository.existsByOrderId(order.getId())).thenReturn(false);
        when(settlementFeeCalculator.calculate(10000)).thenReturn(500);
        when(settlementRepository.save(any(Settlement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderStatusChangeResponse response = orderOwnerActionTransactionService.completePickup(OWNER_ID, ORDER_ID);

        assertThat(response.orderStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(order.getPickupCompletedAt()).isNotNull();

        verify(settlementRepository).save(argThat(settlement ->
                settlement.getSettlementType() == SettlementType.PAYOUT
                        && settlement.getOrderAmount().equals(10000)
                        && settlement.getPlatformFeeAmount().equals(500)
                        && settlement.getPayoutAmount().equals(9500)
                        && settlement.getChargeAmount().equals(0)
        ));
    }

    private Order order(OrderStatus orderStatus) {
        Store store = Store.builder()
                .ownerId(OWNER_ID)
                .storeName("테스트 가게")
                .address("서울시")
                .phoneNumber("02-1234-5678")
                .build();

        return Order.builder()
                .id(ORDER_ID)
                .orderId("ORDER_TEST")
                .memberId(1L)
                .store(store)
                .totalOriginalPrice(10000)
                .totalDiscountAmount(0)
                .paymentAmount(10000)
                .customerName("고객")
                .customerPhone("010-1234-5678")
                .pickupDate(LocalDate.now().plusDays(3))
                .pickupTime(LocalTime.NOON)
                .paymentMethod(com.groupeat.domain.orders.enums.PaymentMethod.PREPAID)
                .orderStatus(orderStatus)
                .build();
    }

    private Payment payment(Order order) {
        return Payment.builder()
                .order(order)
                .orderId(order.getOrderId())
                .memberId(order.getMemberId())
                .paymentKey("payment-key")
                .paymentType(PaymentType.PREPAID)
                .paymentProvider(PaymentProvider.TOSS)
                .totalOrderAmount(10000)
                .paidAmount(10000)
                .remainingAmount(0)
                .paymentStatus(PaymentStatus.DONE)
                .build();
    }
}
