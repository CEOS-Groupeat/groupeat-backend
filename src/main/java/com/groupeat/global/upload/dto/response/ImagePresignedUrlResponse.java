package com.groupeat.global.upload.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record ImagePresignedUrlResponse(

        @Schema(description = "S3 업로드용 presigned URL")
        String uploadUrl,

        @Schema(description = "DB에 저장할 이미지 접근 URL")
        String imageUrl,

        @Schema(description = "S3 object key", example = "menus/2026/06/uuid.jpg")
        String objectKey
) {
}
