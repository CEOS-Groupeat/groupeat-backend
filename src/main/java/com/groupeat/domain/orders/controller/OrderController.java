package com.groupeat.domain.orders.controller;

import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.domain.orders.dto.request.OrderCancelRequest;
import com.groupeat.domain.orders.dto.request.OrderCreateRequest;
import com.groupeat.domain.orders.dto.request.OrderRejectRequest;
import com.groupeat.domain.orders.dto.response.OrderCancelResponse;
import com.groupeat.domain.orders.dto.response.OrderCreateResponse;
import com.groupeat.domain.orders.dto.response.OrderDetailResponse;
import com.groupeat.domain.orders.dto.response.OrderListResponse;
import com.groupeat.domain.orders.dto.response.OrderStatusChangeResponse;
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

    @GetMapping("/{orderId}")
    @Operation(summary = "주문 상세 내역 조회", description = "특정 주문의 상세 정보(메뉴, 옵션, 결제정보 등)를 조회합니다.")
    public ApiResponse<OrderDetailResponse> getOrderDetail(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable @Parameter(description = "조회할 주문 ID") Long orderId
    ) {
        OrderDetailResponse response = orderService.getOrderDetail(member.memberId(), orderId);
        return ApiResponse.onSuccess(response);
    }

    @PatchMapping("/{orderId}/cancel")
    @Operation(summary = "주문 취소", description = "로그인한 회원이 본인의 주문을 취소합니다.")
    public ApiResponse<OrderCancelResponse> cancelOrder(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable @Parameter(description = "취소할 주문 ID") Long orderId,
            @Valid @RequestBody OrderCancelRequest request
    ) {
        OrderCancelResponse response = orderService.cancelOrder(member.memberId(), orderId, request);
        return ApiResponse.onSuccess(response);
    }

    @PatchMapping("/{orderId}/accept")
    @Operation(summary = "주문 승인", description = "사업자가 본인 가게의 주문을 승인합니다.")
    public ApiResponse<OrderStatusChangeResponse> acceptOrder(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable @Parameter(description = "승인할 주문 ID") Long orderId
    ) {
        OrderStatusChangeResponse response = orderService.acceptOrder(member.memberId(), member.memberType(), orderId);
        return ApiResponse.onSuccess(response);
    }

    @PatchMapping("/{orderId}/reject")
    @Operation(summary = "주문 거절", description = "사업자가 본인 가게의 주문을 거절하고 결제 금액을 전액 환불합니다.")
    public ApiResponse<OrderStatusChangeResponse> rejectOrder(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable @Parameter(description = "거절할 주문 ID") Long orderId,
            @Valid @RequestBody OrderRejectRequest request
    ) {
        OrderStatusChangeResponse response = orderService.rejectOrder(member.memberId(), member.memberType(), orderId, request);
        return ApiResponse.onSuccess(response);
    }
}
