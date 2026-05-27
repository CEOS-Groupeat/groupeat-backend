package com.groupeat.domain.payment.controller;

import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.domain.payment.dto.request.PaymentConfirmRequest;
import com.groupeat.domain.payment.dto.response.PaymentConfirmResponse;
import com.groupeat.domain.payment.service.PaymentConfirmService;
import com.groupeat.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
@Tag(name = "Payment", description = "결제 관리 API")
public class PaymentController {

    private final PaymentConfirmService paymentConfirmService;

    @PostMapping("/confirm")
    @Operation(summary = "결제 승인", description = "토스페이먼츠 결제 인증 성공 후 paymentKey, orderId, amount로 최종 결제 승인을 요청합니다.")
    public ApiResponse<PaymentConfirmResponse> confirmPayment(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @RequestBody PaymentConfirmRequest request
    ) {
        PaymentConfirmResponse response = paymentConfirmService.confirm(member.memberId(), request);
        return ApiResponse.onSuccess(response);
    }
}
