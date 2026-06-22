package com.groupeat.global.upload.service;

import com.groupeat.global.exception.GeneralException;
import com.groupeat.global.upload.config.S3Properties;
import com.groupeat.global.upload.dto.request.ImagePresignedUrlRequest;
import com.groupeat.global.upload.dto.response.ImagePresignedUrlResponse;
import com.groupeat.global.upload.enums.ImageUploadDomain;
import com.groupeat.global.upload.exception.UploadErrorStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Service
public class ImageUploadService {

    private static final String S3_URL_FORMAT = "https://%s.s3.%s.amazonaws.com/%s";
    private static final String PUBLIC_PREFIX = "public/";

    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;
    private final String region;

    public ImageUploadService(
            S3Presigner s3Presigner,
            S3Properties s3Properties,
            @Value("${aws.region}") String region
    ) {
        this.s3Presigner = s3Presigner;
        this.s3Properties = s3Properties;
        this.region = region;
    }

    public ImagePresignedUrlResponse createPresignedUrl(ImageUploadDomain domain, ImagePresignedUrlRequest request) {
        String extension = getValidatedExtension(request.fileName(), request.contentType());
        String objectKey = createObjectKey(domain, extension);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(objectKey)
                .contentType(request.contentType())
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(s3Properties.presignedUrlExpirationMinutes()))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        String imageUrl = createImageUrl(objectKey);

        return ImagePresignedUrlResponse.builder()
                .uploadUrl(presignedRequest.url().toString())
                .imageUrl(imageUrl)
                .objectKey(objectKey)
                .build();
    }

    private String createImageUrl(String objectKey) {
        String cloudfrontDomain = s3Properties.cloudfrontDomain();
        // TODO: private 파일 조회 정책 확정 후 presigned GET URL 발급 흐름 추가 필요
        if (objectKey.startsWith(PUBLIC_PREFIX) && cloudfrontDomain != null && !cloudfrontDomain.isBlank()) {
            return normalizeCloudfrontDomain(cloudfrontDomain) + "/" + objectKey;
        }

        return S3_URL_FORMAT.formatted(s3Properties.bucket(), region, objectKey);
    }

    private String normalizeCloudfrontDomain(String cloudfrontDomain) {
        String normalizedDomain = cloudfrontDomain.trim();
        if (!normalizedDomain.startsWith("http://") && !normalizedDomain.startsWith("https://")) {
            normalizedDomain = "https://" + normalizedDomain;
        }

        return normalizedDomain.replaceAll("/$", "");
    }

    private String createObjectKey(ImageUploadDomain domain, String extension) {
        LocalDate today = LocalDate.now();
        String key = "%s/%d/%02d/%s.%s".formatted(
                domain.getDirectory(),
                today.getYear(),
                today.getMonthValue(),
                UUID.randomUUID(),
                extension
        );

        return key;
    }

    private String getValidatedExtension(String fileName, String contentType) {
        String normalizedContentType = contentType.toLowerCase(Locale.ROOT);
        String expectedExtension = switch (normalizedContentType) {
            case "image/jpeg", "image/jpg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            default -> throw new GeneralException(UploadErrorStatus.INVALID_IMAGE_CONTENT_TYPE);
        };

        String fileExtension = extractExtension(fileName);
        if (!fileExtension.equals(expectedExtension) && !(expectedExtension.equals("jpg") && fileExtension.equals("jpeg"))) {
            throw new GeneralException(UploadErrorStatus.INVALID_IMAGE_FILE_EXTENSION);
        }

        return expectedExtension;
    }

    private String extractExtension(String fileName) {
        int extensionStartIndex = fileName.lastIndexOf(".");
        if (extensionStartIndex < 0 || extensionStartIndex == fileName.length() - 1) {
            throw new GeneralException(UploadErrorStatus.INVALID_IMAGE_FILE_EXTENSION);
        }

        return fileName.substring(extensionStartIndex + 1).toLowerCase(Locale.ROOT);
    }
}
