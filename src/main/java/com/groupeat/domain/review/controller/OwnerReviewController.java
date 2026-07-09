package com.groupeat.domain.review.controller;

import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.domain.review.dto.request.OwnerReplyCreateRequest;
import com.groupeat.domain.review.dto.response.OwnerReplyCreateResponse;
import com.groupeat.domain.review.dto.response.OwnerReviewListResponse;
import com.groupeat.domain.review.dto.response.OwnerReviewSummaryResponse;
import com.groupeat.domain.review.dto.response.ReviewCreateResponse;
import com.groupeat.domain.review.service.OwnerReviewService;
import com.groupeat.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner/reviews")
@Tag(name = "Owner Review API", description = "사장님 리뷰 관리 API")
public class OwnerReviewController {

    private final OwnerReviewService ownerReviewService;

    @GetMapping("/summary")
    @Operation(summary = "리뷰 요약 정보 조회", description = "가게의 총 평점 및 별점 분포 요약 정보를 조회합니다.")
    public ApiResponse<OwnerReviewSummaryResponse> getReviewSummary(
            @AuthenticationPrincipal AuthenticatedMember member
    ) {
        OwnerReviewSummaryResponse response = ownerReviewService.getReviewSummary(member.memberId());
        return ApiResponse.onSuccess(response);
    }

    @GetMapping
    @Operation(summary = "사장님 리뷰 목록 조회", description = "가게에 작성된 리뷰 목록을 무한 스크롤로 조회합니다.")
    public ApiResponse<OwnerReviewListResponse> getStoreReviews(
            @AuthenticationPrincipal AuthenticatedMember member,
            @RequestParam(required = false) @Parameter(description = "마지막으로 조회된 리뷰 ID (첫 요청 시 null)") Long lastReviewId,
            @RequestParam(defaultValue = "10") @Parameter(description = "조회할 개수") int size
    ) {
        OwnerReviewListResponse response = ownerReviewService.getStoreReviews(member.memberId(), lastReviewId, size);
        return ApiResponse.onSuccess(response);
    }

    @PostMapping("/{reviewId}/reply")
    @Operation(summary = "사장님 답글 작성", description = "고객이 남긴 리뷰에 사장님 답글을 등록합니다.")
    public ApiResponse<OwnerReplyCreateResponse> createReply(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable @Parameter(description = "리뷰 ID") Long reviewId,
            @Valid @RequestBody OwnerReplyCreateRequest request
    ) {
        OwnerReplyCreateResponse response = ownerReviewService.createReply(member.memberId(), reviewId, request);

        return ApiResponse.onSuccess(response);
    }
}
