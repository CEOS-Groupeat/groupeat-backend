package com.groupeat.domain.cart.controller;

import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.domain.cart.dto.request.CartCalculateRequest;
import com.groupeat.domain.cart.dto.request.CartItemAddRequest;
import com.groupeat.domain.cart.dto.response.CartCalculateResponse;
import com.groupeat.domain.cart.dto.response.CartListResponse;
import com.groupeat.domain.cart.service.CartCalculateService;
import com.groupeat.domain.cart.service.CartService;
import com.groupeat.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "장바구니 관리 API")
public class CartController {

    private final CartService cartService;
    private final CartCalculateService cartCalculateService;

    @PostMapping("/items")
    @Operation(summary = "장바구니 메뉴 담기", description = "장바구니에 새로운 메뉴와 옵션을 추가합니다.")
    public ApiResponse<CartListResponse> addCartItem(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @RequestBody CartItemAddRequest request
    ) {
        CartListResponse response = cartService.addCartItem(member.memberId(), request);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping
    @Operation(summary = "장바구니 목록 조회", description = "가게별로 그룹화된 장바구니 목록을 조회합니다.")
    public ApiResponse<CartListResponse> getCartList(
            @AuthenticationPrincipal AuthenticatedMember member
    ) {
        CartListResponse response = cartService.getCartList(member.memberId());
        return ApiResponse.onSuccess(response);
    }

    @DeleteMapping("/items/{cartItemId}")
    @Operation(summary = "장바구니 메뉴 삭제", description = "장바구니에서 특정 메뉴를 삭제합니다.")
    public ApiResponse<String> deleteCartItem(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable Long cartItemId
    ) {
        cartService.deleteCartItem(member.memberId(), cartItemId);
        return ApiResponse.onSuccess("장바구니에서 메뉴가 삭제되었습니다.");
    }

    @PostMapping("/calculate")
    @Operation(summary = "장바구니 선택 항목 계산 및 주문 검증", description = "주문할 장바구니 항목들을 선택하여 총 결제 금액을 계산하고 단일 가게 제약을 검증합니다.")
    public ApiResponse<CartCalculateResponse> calculateCart(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @RequestBody CartCalculateRequest request
    ) {
        CartCalculateResponse response = cartCalculateService.calculate(member.memberId(), request);
        return ApiResponse.onSuccess(response);
    }
}