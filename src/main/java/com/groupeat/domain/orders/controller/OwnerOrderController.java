package com.groupeat.domain.orders.controller;

import com.groupeat.domain.orders.dto.response.OwnerOrderListResponse;
import com.groupeat.domain.orders.dto.response.OwnerOrderListResponse.OwnerOrderListDTO;
import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.domain.orders.enums.OrderTab;
import com.groupeat.domain.orders.service.OwnerOrderService;
import com.groupeat.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner/orders")
@Tag(name = "Owner Order", description = "사장님용 주문 관리 API")
public class OwnerOrderController {

    private final OwnerOrderService ownerOrderService;

    @GetMapping
    @Operation(summary = "사장님 주문 목록 조회",
            description = "탭(상태)별로 사장님의 주문 목록을 조회합니다.")
    public ApiResponse<OwnerOrderListResponse.OwnerOrderListDTO> getOwnerOrderList(
            @RequestParam Long ownerId,
            @RequestParam OrderTab tab,
            @RequestParam(required = false) Long lastOrderId,
            @RequestParam(defaultValue = "20") int size
    ) {
        OwnerOrderListResponse.OwnerOrderListDTO response = ownerOrderService.getOwnerOrderListByTab(ownerId, tab, lastOrderId, size);
        return ApiResponse.onSuccess(response);
    }
}
