package com.groupeat.domain.signup.controller;

import com.groupeat.domain.signup.dto.CommonSignupRequest;
import com.groupeat.domain.signup.dto.CommonSignupResponse;
import com.groupeat.domain.signup.dto.CustomerSignupRequest;
import com.groupeat.domain.signup.dto.CustomerSignupResponse;
import com.groupeat.domain.signup.service.SignupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/signup")
@RequiredArgsConstructor
public class SignupController {

    private final SignupService signupService;

    @PostMapping("/common")
    public CommonSignupResponse signupCommon(
            @Valid @RequestBody CommonSignupRequest request
            ) {
        return signupService.signupCommon(request);
    }

    @PostMapping("/customer")
    public CustomerSignupResponse signupCustomer(
            @Valid @RequestBody CustomerSignupRequest request
    ) {
        return signupService.signupCustomer(request);
    }
}
