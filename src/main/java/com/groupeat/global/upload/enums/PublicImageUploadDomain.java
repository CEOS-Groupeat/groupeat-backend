package com.groupeat.global.upload.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공개 이미지 사용 도메인", enumAsRef = true)
public enum PublicImageUploadDomain {

    STORE,
    MENU,
    PROFILE,
    REVIEW;

    public ImageUploadDomain toImageUploadDomain() {
        return ImageUploadDomain.valueOf(name());
    }
}
