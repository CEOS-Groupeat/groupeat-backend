package com.groupeat.domain.review.controller;

import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.domain.review.dto.request.ReviewCreateRequest;
import com.groupeat.domain.review.dto.response.ReviewCreateResponse;
import com.groupeat.domain.review.service.ReviewService;
import com.groupeat.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
@Tag(name = "Customer Review API", description = "고객용 리뷰 관련 API")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "리뷰 작성 API", description = "고객이 완료된 주문에 대해 리뷰를 작성합니다.")
    public ApiResponse<ReviewCreateResponse> createReview(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        ReviewCreateResponse response = reviewService.createReview(member.memberId(), request);
        return ApiResponse.onSuccess(response);
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "리뷰 삭제 API", description = "작성한 리뷰를 소프트 딜리트 방식으로 삭제하며, 가게의 별점 통계가 자동으로 롤백됩니다.")
    public ApiResponse<String> deleteReview(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable Long reviewId
    ) {
        reviewService.deleteReview(member.memberId(), reviewId);
        return ApiResponse.onSuccess("리뷰가 성공적으로 삭제되었습니다.");
    }
}
