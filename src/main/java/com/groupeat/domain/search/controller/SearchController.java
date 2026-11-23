package com.groupeat.domain.search.controller;

import com.groupeat.domain.search.dto.request.StoreSearchCondition;
import com.groupeat.domain.search.dto.response.StoreSearchResponse;
import com.groupeat.domain.search.service.SearchService;
import com.groupeat.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Search", description = "메인 화면 동적 검색 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "가게 목록 검색", description = "위치, 카테고리, 예산, 픽업 시간 등으로 가게를 검색합니다. size는 기본 20, 최대 100입니다. 다음 페이지는 동일 검색 조건과 nextCursor를 cursor로 전달합니다. 필터/정렬 변경 시 커서를 초기화하며, 마지막 페이지는 hasNext=false, nextCursor=null입니다.")
    @GetMapping("/stores")
    public ApiResponse<StoreSearchResponse.StoreListDTO> searchStores(
            @Valid @ModelAttribute StoreSearchCondition condition
    ) {
        StoreSearchResponse.StoreListDTO response = searchService.searchStores(condition);

        return ApiResponse.onSuccess(response);
    }
}