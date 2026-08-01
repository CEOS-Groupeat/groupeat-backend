package com.groupeat.domain.orders.service;

import com.groupeat.domain.orders.config.OrderProperties;
import com.groupeat.domain.orders.exception.OrderErrorStatus;
import com.groupeat.global.exception.GeneralException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderStoreBlockServiceTest {

    @Test
    void validateOrderableStore_blockedStore_throwsStoreBlocked() {
        OrderStoreBlockService service = new OrderStoreBlockService(new OrderProperties(Set.of(3L)));

        assertThatThrownBy(() -> service.validateOrderableStore(3L))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(OrderErrorStatus.ORDER_STORE_BLOCKED)
                );
    }

    @Test
    void validateOrderableStore_unblockedStore_passes() {
        OrderStoreBlockService service = new OrderStoreBlockService(new OrderProperties(Set.of(3L)));

        assertThatCode(() -> service.validateOrderableStore(4L)).doesNotThrowAnyException();
    }
}
