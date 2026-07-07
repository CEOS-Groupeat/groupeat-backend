package com.groupeat.domain.payment.entity;

import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.orders.enums.PaymentMethod;
import com.groupeat.domain.payment.enums.PaymentType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentTest {

    @Test
    void onSitePayment_usesHalfDepositAndRemainingHalf() {
        Order order = Order.builder()
                .orderId("ORDER_TEST")
                .paymentMethod(PaymentMethod.ON_SITE)
                .paymentAmount(5000)
                .build();

        Payment payment = Payment.ready(
                order,
                1L,
                PaymentType.ON_SITE,
                10000,
                5000
        );

        assertThat(PaymentMethod.ON_SITE.getPaymentRatio()).isEqualTo(0.5);
        assertThat(payment.getTotalOrderAmount()).isEqualTo(10000);
        assertThat(payment.getPaidAmount()).isEqualTo(5000);
        assertThat(payment.getRemainingAmount()).isEqualTo(5000);
    }
}
