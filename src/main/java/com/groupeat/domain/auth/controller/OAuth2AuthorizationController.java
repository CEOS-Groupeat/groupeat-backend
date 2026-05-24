package com.groupeat.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/api/auth/oauth2")
@Tag(name = "OAuth2", description = "소셜 로그인 시작 API")
public class OAuth2AuthorizationController {

    @GetMapping("/authorize/{provider}")
    @Operation(summary = "소셜 로그인 시작", description = "선택한 소셜 로그인 페이지로 이동합니다.")
    public ResponseEntity<Void> authorize(
            @PathVariable String provider
    ) {
        String registrationId = provider.toLowerCase(Locale.ROOT);

        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, "/oauth2/authorization/" + registrationId)
                .build();
    }
}
