package com.groupeat.domain.review.dto.response;

import com.groupeat.domain.review.enums.EventType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
@Schema(name = "OwnerReviewListResponse", description = "사장님 리뷰 목록 조회 응답")
public record OwnerReviewListResponse(
        @Schema(description = "리뷰 목록")
        List<OwnerReviewCardDTO> reviewList,

        @Schema(description = "다음 페이지 존재 여부 (무한 스크롤용)", example = "true")
        boolean hasNext,

        @Schema(description = "다음 커서 ID (마지막 리뷰의 PK ID)", example = "42")
        Long nextCursor
) {
    @Builder
    @Schema(name = "OwnerReviewCardDTO", description = "사장님용 단일 리뷰 상세 카드")
    public record OwnerReviewCardDTO(
            @Schema(description = "리뷰 ID", example = "1")
            Long reviewId,

            @Schema(description = "작성자 닉네임 (마스킹 처리)", example = "안**")
            String authorNickname,

            @Schema(description = "리뷰 별점 (1~5)", example = "5")
            Integer rating,

            @Schema(description = "행사 유형", example = "강연")
            EventType eventType,

            @Schema(description = "행사 인원", example = "56")
            Integer headcount,

            @Schema(description = "1인당 예산", example = "8000")
            Integer perPersonBudget,

            @Schema(description = "리뷰 내용", example = "와, 여기 샌드위치 진짜 뚱뚱하네요!")
            String content,

            @Schema(description = "리뷰 작성 일자", example = "2026-07-02")
            LocalDate createdAt,

            @Schema(description = "첨부된 리뷰 이미지 URL 목록")
            List<String> imageUrls,

            @Schema(description = "주문한 메뉴 이름 목록", example = "[\"반반 세트\", \"참치 김밥 + 에그마요 샌드위치 세트\"]")
            List<String> orderedMenuNames,

            @Schema(description = "사장님 답글 내용 (답글이 없을 경우 null)", example = "세빙빙님, 맛있게 드셔주셔서 감사합니다!")
            String ownerReplyContent,

            @Schema(description = "답글 작성 일자", example = "2026-07-05")
            LocalDate repliedAt
    ) {}
}