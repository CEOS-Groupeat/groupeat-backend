package com.groupeat.domain.orders.service;

import com.groupeat.domain.cart.repository.CartItemOptionRepository;
import com.groupeat.domain.cart.repository.CartItemRepository;
import com.groupeat.domain.cart.service.CartCalculateService;
import com.groupeat.domain.orders.dto.request.OrderCancelRequest;
import com.groupeat.domain.orders.dto.response.OrderCancelResponse;
import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.orders.enums.OrderCancelledBy;
import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.domain.orders.exception.OrderErrorStatus;
import com.groupeat.domain.orders.repository.OrderItemOptionRepository;
import com.groupeat.domain.orders.repository.OrderItemRepository;
import com.groupeat.domain.orders.repository.OrderQueryRepository;
import com.groupeat.domain.orders.repository.OrderRepository;
import com.groupeat.domain.payment.repository.PaymentRepository;
import com.groupeat.domain.store.entity.Store;
import com.groupeat.domain.store.repository.MenuOptionRepository;
import com.groupeat.domain.store.repository.MenuRepository;
import com.groupeat.domain.store.repository.StoreRepository;
import com.groupeat.global.exception.GeneralException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long ORDER_ID = 10L;

    private OrderRepository orderRepository;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        orderService = new OrderService(
                orderRepository,
                mock(OrderQueryRepository.class),
                mock(OrderItemRepository.class),
                mock(OrderItemOptionRepository.class),
                mock(PaymentRepository.class),
                mock(CartItemRepository.class),
                mock(CartItemOptionRepository.class),
                mock(StoreRepository.class),
                mock(MenuRepository.class),
                mock(MenuOptionRepository.class),
                mock(CartCalculateService.class)
        );
    }

    @Test
    void cancelOrder_fullRefundBeforeStoreCancelDeadline() {
        Order order = order(OrderStatus.PAID, LocalDate.now().plusDays(3), 2);
        when(orderRepository.findByIdAndMemberId(ORDER_ID, MEMBER_ID)).thenReturn(Optional.of(order));

        OrderCancelResponse response = orderService.cancelOrder(MEMBER_ID, ORDER_ID, new OrderCancelRequest("일정 변경"));

        assertThat(response.orderStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(response.refundRate()).isEqualTo(100);
        assertThat(response.refundAmount()).isEqualTo(10000);
        assertThat(order.getCancelReason()).isEqualTo("일정 변경");
        assertThat(order.getCancelledBy()).isEqualTo(OrderCancelledBy.CUSTOMER);
        assertThat(order.getCancelledByMemberId()).isEqualTo(MEMBER_ID);
        assertThat(order.getCancelRefundRate()).isEqualTo(100);
        assertThat(order.getCancelRefundAmount()).isEqualTo(10000);
        assertThat(order.getCancelledAt()).isNotNull();
    }

    @Test
    void cancelOrder_halfRefundAfterStoreCancelDeadline() {
        Order order = order(OrderStatus.ACCEPTED, LocalDate.now().plusDays(1), 2);
        when(orderRepository.findByIdAndMemberId(ORDER_ID, MEMBER_ID)).thenReturn(Optional.of(order));

        OrderCancelResponse response = orderService.cancelOrder(MEMBER_ID, ORDER_ID, new OrderCancelRequest("일정 변경"));

        assertThat(response.refundRate()).isEqualTo(50);
        assertThat(response.refundAmount()).isEqualTo(5000);
        assertThat(order.getCancelRefundRate()).isEqualTo(50);
        assertThat(order.getCancelRefundAmount()).isEqualTo(5000);
    }

    @Test
    void cancelOrder_rejectsInvalidOrderStatus() {
        Order order = order(OrderStatus.COMPLETED, LocalDate.now().plusDays(3), 2);
        when(orderRepository.findByIdAndMemberId(ORDER_ID, MEMBER_ID)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(MEMBER_ID, ORDER_ID, new OrderCancelRequest("일정 변경")))
                .isInstanceOf(GeneralException.class)
                .extracting("code")
                .isEqualTo(OrderErrorStatus.ORDER_CANCEL_NOT_ALLOWED);
    }

    private Order order(OrderStatus orderStatus, LocalDate pickupDate, Integer minOrderDays) {
        Store store = Store.builder()
                .ownerId(2L)
                .storeName("테스트 가게")
                .address("서울시")
                .phoneNumber("02-1234-5678")
                .minOrderDays(minOrderDays)
                .build();

        return Order.builder()
                .id(ORDER_ID)
                .orderId("ORDER_TEST")
                .memberId(MEMBER_ID)
                .store(store)
                .totalOriginalPrice(10000)
                .totalDiscountAmount(0)
                .paymentAmount(10000)
                .customerName("고객")
                .customerPhone("010-1234-5678")
                .pickupDate(pickupDate)
                .pickupTime(java.time.LocalTime.NOON)
                .paymentMethod(com.groupeat.domain.orders.enums.PaymentMethod.PREPAID)
                .orderStatus(orderStatus)
                .build();
    }
}
