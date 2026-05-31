package com.groupeat.domain.settlement.service;

import com.groupeat.domain.settlement.config.SettlementProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SettlementFeeCalculator {

    private final SettlementProperties settlementProperties;

    public int calculate(Integer orderAmount) {
        return orderAmount * settlementProperties.platformFeeRate() / 100;
    }
}
