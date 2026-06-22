package com.groupeat.global.upload.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = "이미지 사용 도메인", enumAsRef = true)
@Getter
@RequiredArgsConstructor
public enum ImageUploadDomain {

    STORE("public/stores"),
    MENU("public/menus"),
    PROFILE("public/profiles"),
    REVIEW("public/reviews"),
    BUSINESS_DOCUMENT("private/business-documents");

    private final String directory;
}
