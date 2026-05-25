package com.groupeat.domain.orders.controller;

import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.domain.orders.dto.request.OrderCreateRequest;
import com.groupeat.domain.orders.dto.response.OrderCreateResponse;
import com.groupeat.domain.orders.service.OrderService;
import com.groupeat.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "주문 관리 API")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "주문 생성", description = "로그인한 회원이 새로운 주문을 생성합니다.")
    public ApiResponse<OrderCreateResponse> createOrder(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @RequestBody OrderCreateRequest request
    ) {
        OrderCreateResponse response = orderService.createOrder(member.memberId(), request);
        return ApiResponse.onSuccess(response);
    }
}