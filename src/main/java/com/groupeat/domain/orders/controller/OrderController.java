package com.groupeat.domain.orders.controller;

import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.domain.orders.dto.request.OrderCreateRequest;
import com.groupeat.domain.orders.dto.response.OrderCreateResponse;
import com.groupeat.domain.orders.dto.response.OrderListResponse;
import com.groupeat.domain.orders.enums.OrderListFilterType;
import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.domain.orders.service.OrderService;
import com.groupeat.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    @Operation(summary = "내 주문 내역 조회", description = "나의 주문 내역을 조회합니다.")
    public ApiResponse<OrderListResponse> getOrderList(
            @AuthenticationPrincipal AuthenticatedMember member,
            @RequestParam(required = false) @Parameter(description = "정렬 필터 타입") OrderListFilterType filter,
            @RequestParam(required = false) @Parameter(description = "마지막으로 조회된 주문 ID (최초 조회 시 생략)") Long lastOrderId,
            @RequestParam(defaultValue = "10") @Parameter(description = "한 번에 가져올 데이터 개수") int size
    ) {
        List<OrderStatus> statusList = (filter == null || filter == OrderListFilterType.ALL) ? null : filter.getMappedStatuses();

        OrderListResponse response = orderService.getOrderList(member.memberId(), statusList, lastOrderId, size);
        return ApiResponse.onSuccess(response);
    }
}