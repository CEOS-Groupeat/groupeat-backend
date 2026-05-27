package com.groupeat.domain.payment.service;

import com.groupeat.domain.payment.dto.request.PaymentConfirmRequest;
import com.groupeat.domain.payment.dto.response.PaymentConfirmResponse;
import org.springframework.stereotype.Service;

@Service
public class PaymentConfirmService {

    // TODO : 실제 결제 승인 로직 구현
    public PaymentConfirmResponse confirm(Long memberId, PaymentConfirmRequest request) {
        throw new UnsupportedOperationException("Payment confirm service is not implemented yet.");
    }
}
