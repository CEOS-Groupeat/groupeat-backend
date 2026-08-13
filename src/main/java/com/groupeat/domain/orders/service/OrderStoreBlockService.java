package com.groupeat.domain.orders.service;

import com.groupeat.domain.orders.config.OrderProperties;
import com.groupeat.domain.orders.exception.OrderErrorStatus;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderStoreBlockService {

    private final OrderProperties orderProperties;

    public void validateOrderableStore(Long storeId) {
        if (storeId != null && orderProperties.blockedStoreIds().contains(storeId)) {
            throw new GeneralException(OrderErrorStatus.ORDER_STORE_BLOCKED);
        }
    }
}
