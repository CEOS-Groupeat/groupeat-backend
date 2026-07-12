package com.groupeat.domain.notification.controller;

import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.domain.notification.dto.request.FcmRegistrationRequest;
import com.groupeat.domain.notification.dto.response.FcmRegistrationResponse;
import com.groupeat.domain.notification.service.FcmRegistrationService;
import com.groupeat.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
}
