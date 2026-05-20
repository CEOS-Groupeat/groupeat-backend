package com.groupeat.domain.signup.controller;

import com.groupeat.domain.signup.dto.CommonSignupRequest;
import com.groupeat.domain.signup.dto.CommonSignupResponse;
import com.groupeat.domain.signup.dto.CustomerSignupRequest;
import com.groupeat.domain.signup.dto.CustomerSignupResponse;
import com.groupeat.domain.signup.service.SignupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/signup")
@RequiredArgsConstructor
@Tag(name = "Signup", description = "회원가입 API")
public class SignupController {

    private final SignupService signupService;

    @PostMapping("/common")
    @Operation(summary = "공통 회원가입", description = "소셜 계정, 공통 약관, 휴대폰 인증 정보를 저장합니다.")
    public CommonSignupResponse signupCommon(
            @Valid @RequestBody CommonSignupRequest request
            ) {
        return signupService.signupCommon(request);
    }

    @PostMapping("/customer")
    @Operation(summary = "고객 회원가입 완료", description = "고객 추가 약관과 프로필 정보를 저장하고 가입을 완료합니다.")
    public CustomerSignupResponse signupCustomer(
            @Valid @RequestBody CustomerSignupRequest request
    ) {
        return signupService.signupCustomer(request);
    }
}
