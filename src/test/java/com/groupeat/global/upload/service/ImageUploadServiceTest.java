package com.groupeat.global.upload.service;

import com.groupeat.global.exception.GeneralException;
import com.groupeat.global.upload.config.S3Properties;
import com.groupeat.global.upload.dto.request.ImagePresignedUrlRequest;
import com.groupeat.global.upload.dto.response.ImagePresignedUrlResponse;
import com.groupeat.global.upload.enums.ImageUploadDomain;
import com.groupeat.global.upload.exception.UploadErrorStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ImageUploadServiceTest {

    private S3Presigner s3Presigner;
    private ImageUploadService imageUploadService;

    @BeforeEach
    void setUp() throws Exception {
        s3Presigner = mock(S3Presigner.class);
        imageUploadService = new ImageUploadService(
                s3Presigner,
                new S3Properties("groupeat-test-bucket", 10, "d2f1i7fluhpn2d.cloudfront.net"),
                "ap-northeast-2"
        );

        SdkHttpFullRequest httpRequest = SdkHttpFullRequest.builder()
                .method(SdkHttpMethod.PUT)
                .uri(URI.create("https://example.com/presigned"))
                .build();
        PresignedPutObjectRequest presignedRequest = PresignedPutObjectRequest.builder()
                .httpRequest(httpRequest)
                .expiration(Instant.now().plusSeconds(600))
                .isBrowserExecutable(true)
                .signedHeaders(Map.of("host", List.of("example.com")))
                .build();
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .thenReturn(presignedRequest);
    }

    @Test
    void createPresignedUrl_returnsUploadUrlAndImageUrl() {
        ImagePresignedUrlResponse response = imageUploadService.createPresignedUrl(
                ImageUploadDomain.MENU,
                request("sandwich.jpg", "image/jpeg")
        );

        assertThat(response.uploadUrl()).isEqualTo("https://example.com/presigned");
        assertThat(response.objectKey()).startsWith("public/menus/");
        assertThat(response.objectKey()).endsWith(".jpg");
        assertThat(response.imageUrl())
                .startsWith("https://d2f1i7fluhpn2d.cloudfront.net/public/menus/");
    }

    @Test
    void createPresignedUrl_privateDomain_returnsPrivateObjectKey() {
        ImagePresignedUrlResponse response = imageUploadService.createPresignedUrl(
                ImageUploadDomain.BUSINESS_DOCUMENT,
                request("business-license.png", "image/png")
        );

        assertThat(response.objectKey()).startsWith("private/business-documents/");
        assertThat(response.objectKey()).endsWith(".png");
        assertThat(response.imageUrl())
                .startsWith("https://groupeat-test-bucket.s3.ap-northeast-2.amazonaws.com/private/business-documents/");
    }

    @Test
    void createPresignedUrl_invalidContentType_throwsException() {
        assertThatThrownBy(() -> imageUploadService.createPresignedUrl(
                ImageUploadDomain.MENU,
                request("sandwich.txt", "text/plain")
        )).isInstanceOfSatisfying(GeneralException.class, exception ->
                assertThat(exception.getCode()).isEqualTo(UploadErrorStatus.INVALID_IMAGE_CONTENT_TYPE)
        );
    }

    @Test
    void createPresignedUrl_mismatchedExtension_throwsException() {
        assertThatThrownBy(() -> imageUploadService.createPresignedUrl(
                ImageUploadDomain.MENU,
                request("sandwich.png", "image/jpeg")
        )).isInstanceOfSatisfying(GeneralException.class, exception ->
                assertThat(exception.getCode()).isEqualTo(UploadErrorStatus.INVALID_IMAGE_FILE_EXTENSION)
        );
    }

    private ImagePresignedUrlRequest request(String fileName, String contentType) {
        return ImagePresignedUrlRequest.builder()
                .fileName(fileName)
                .contentType(contentType)
                .build();
    }
}
