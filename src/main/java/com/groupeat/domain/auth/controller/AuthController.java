package com.groupeat.domain.auth.controller;

import com.groupeat.domain.auth.dto.AuthenticatedMemberResponse;
import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/me")
    public AuthenticatedMemberResponse me(
            @AuthenticationPrincipal AuthenticatedMember member
    ) {
        return AuthenticatedMemberResponse.from(member);
    }
}
