package com.groupeat.domain.terms.controller;

import com.groupeat.domain.terms.dto.TermsResponse;
import com.groupeat.domain.terms.enums.TermsTargetType;
import com.groupeat.domain.terms.service.TermsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/terms")
@RequiredArgsConstructor
public class TermsController {

    private final TermsService termsService;

    @GetMapping
    public List<TermsResponse> getTerms(
            @RequestParam TermsTargetType targetType
    ) {
        return termsService.getTerms(targetType);
    }
}
