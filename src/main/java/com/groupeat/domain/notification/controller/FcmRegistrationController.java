package com.groupeat.domain.notification.controller;

import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.domain.notification.dto.request.FcmRegistrationDeactivateRequest;
import com.groupeat.domain.notification.dto.request.FcmRegistrationRequest;
import com.groupeat.domain.notification.dto.response.FcmRegistrationDeactivateResponse;
import com.groupeat.domain.notification.dto.response.FcmRegistrationResponse;
import com.groupeat.domain.notification.service.FcmRegistrationService;
import com.groupeat.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications/fcm-registrations")
@RequiredArgsConstructor
@Tag(name = "FCM Registration", description = "FCM 등록 API")
public class FcmRegistrationController {

    private final FcmRegistrationService fcmRegistrationService;

    @PostMapping
    @Operation(
            summary = "FCM 등록",
            description = "로그인한 회원의 웹 FCM registration token을 등록하거나 갱신합니다."
    )
    public ApiResponse<FcmRegistrationResponse> register(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @RequestBody FcmRegistrationRequest request
    ) {
        return ApiResponse.onSuccess(fcmRegistrationService.register(member.memberId(), request));
    }

    @DeleteMapping
    @Operation(
            summary = "FCM 등록 비활성화",
            description = "로그인한 회원의 FCM registration token을 비활성화합니다. 등록값이 없거나 이미 비활성화된 경우에도 성공 응답을 반환합니다."
    )
    public ApiResponse<FcmRegistrationDeactivateResponse> deactivate(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @RequestBody FcmRegistrationDeactivateRequest request
    ) {
        return ApiResponse.onSuccess(fcmRegistrationService.deactivate(member.memberId(), request));
    }
}
