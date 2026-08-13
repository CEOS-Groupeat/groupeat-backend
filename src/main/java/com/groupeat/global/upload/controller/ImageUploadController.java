package com.groupeat.global.upload.controller;

import com.groupeat.global.apiPayload.ApiResponse;
import com.groupeat.global.upload.dto.request.ImagePresignedUrlRequest;
import com.groupeat.global.upload.dto.response.ImagePresignedUrlResponse;
import com.groupeat.global.upload.enums.PublicImageUploadDomain;
import com.groupeat.global.upload.service.ImageUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Image Upload", description = "이미지 업로드 presigned URL API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/uploads/images")
public class ImageUploadController {

    private final ImageUploadService imageUploadService;

    @Operation(
            summary = "공개 이미지 업로드 presigned URL 발급",
            description = "매장, 메뉴, 프로필, 리뷰 이미지를 S3에 직접 업로드하기 위한 presigned URL을 발급합니다. "
                    + "사업자등록증은 사업자 회원가입 전용 API를 사용해야 합니다."
    )
    @PostMapping("/presigned-url")
    public ApiResponse<ImagePresignedUrlResponse> createPresignedUrl(
            @RequestParam @Parameter(description = "공개 이미지 사용 도메인") PublicImageUploadDomain domain,
            @Valid @RequestBody ImagePresignedUrlRequest request
    ) {
        ImagePresignedUrlResponse response = imageUploadService.createPresignedUrl(
                domain.toImageUploadDomain(),
                request
        );
        return ApiResponse.onSuccess(response);
    }
}
