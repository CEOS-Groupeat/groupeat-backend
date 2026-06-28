package com.groupeat.domain.business.controller;

import com.groupeat.domain.business.dto.request.BusinessValidateRequest;
import com.groupeat.domain.business.dto.response.BusinessValidateResponse;
import com.groupeat.domain.business.service.BusinessValidationService;
import com.groupeat.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "사업자 인증", description = "사장님 가입을 위한 사업자등록번호 인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/business")
public class BusinessValidationController {

    private final BusinessValidationService businessValidationService;

    @PostMapping("/validate")
    @Operation(summary = "사업자등록번호 검증", description = "국세청 API를 통해 사업자번호의 정상 영업 여부를 검증하고 토큰을 발급합니다.")
    public ApiResponse<BusinessValidateResponse> validateBusinessNumber(
            @Valid @RequestBody BusinessValidateRequest request
    ) {
        BusinessValidateResponse response = businessValidationService.validateBusinessNumber(request.businessNumber());
        return ApiResponse.onSuccess(response);
    }
}