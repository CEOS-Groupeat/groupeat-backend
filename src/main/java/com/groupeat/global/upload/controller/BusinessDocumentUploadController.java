package com.groupeat.global.upload.controller;

import com.groupeat.global.apiPayload.ApiResponse;
import com.groupeat.global.upload.dto.request.ImagePresignedUrlRequest;
import com.groupeat.global.upload.dto.response.ImagePresignedUrlResponse;
import com.groupeat.global.upload.enums.ImageUploadDomain;
import com.groupeat.global.upload.service.ImageUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Image Upload", description = "이미지 업로드 presigned URL API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/uploads/business-document")
public class BusinessDocumentUploadController {

    private final ImageUploadService imageUploadService;

    @Operation(
            summary = "사업자등록증 업로드 presigned URL 발급",
            description = "사업자 회원가입에 사용할 사업자등록증 이미지를 S3에 직접 업로드하기 위한 presigned URL을 발급합니다. "
                    + "응답의 imageUrl을 사업자 회원가입 요청의 businessRegistrationCertificateUrl에 사용합니다."
    )
    @PostMapping("/presigned-url")
    public ApiResponse<ImagePresignedUrlResponse> createPresignedUrl(
            @Valid
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            schema = @Schema(implementation = ImagePresignedUrlRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "fileName": "business-registration-certificate.jpg",
                                      "contentType": "image/jpeg"
                                    }
                                    """)
                    )
            )
            ImagePresignedUrlRequest request
    ) {
        ImagePresignedUrlResponse response = imageUploadService.createPresignedUrl(
                ImageUploadDomain.BUSINESS_DOCUMENT,
                request
        );
        return ApiResponse.onSuccess(response);
    }
}
