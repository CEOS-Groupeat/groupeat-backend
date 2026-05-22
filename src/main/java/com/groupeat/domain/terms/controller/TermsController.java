package com.groupeat.domain.terms.controller;

import com.groupeat.domain.terms.dto.TermsResponse;
import com.groupeat.domain.terms.enums.TermsTargetType;
import com.groupeat.domain.terms.service.TermsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/terms")
@RequiredArgsConstructor
@Tag(name = "Terms", description = "약관 조회 API")
public class TermsController {

    private final TermsService termsService;

    @GetMapping
    @Operation(summary = "약관 목록 조회", description = "대상 유형별 활성 약관 목록을 조회합니다.")
    public List<TermsResponse> getTerms(
            @RequestParam TermsTargetType targetType
    ) {
        return termsService.getTerms(targetType);
    }
}
