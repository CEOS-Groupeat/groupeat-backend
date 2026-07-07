package com.groupeat.domain.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "OwnerReplyCreateRequest", description = "사장님 리뷰 답글 작성 요청")
public record OwnerReplyCreateRequest(
        @Schema(description = "답글 내용", example = "맛있게 드셔주셔서 감사합니다!")
        @NotBlank(message = "답글 내용을 입력해주세요.")
        @Size(max = 500, message = "답글은 500자를 초과할 수 없습니다.")
        String replyContent
) {}