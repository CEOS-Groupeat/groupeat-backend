package com.groupeat.global.upload.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ImagePresignedUrlRequest(

        @Schema(description = "원본 파일명", example = "sandwich.jpg")
        @NotBlank(message = "파일명은 필수입니다.")
        @Size(max = 255, message = "파일명은 255자 이하여야 합니다.")
        String fileName,

        @Schema(description = "이미지 Content-Type", example = "image/jpeg")
        @NotBlank(message = "Content-Type은 필수입니다.")
        @Size(max = 100, message = "Content-Type은 100자 이하여야 합니다.")
        String contentType
) {
}
