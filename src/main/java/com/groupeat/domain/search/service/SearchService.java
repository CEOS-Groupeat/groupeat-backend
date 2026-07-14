package com.groupeat.domain.search.service;

import com.groupeat.domain.search.converter.SearchConverter;
import com.groupeat.domain.search.dto.request.StoreSearchCondition;
import com.groupeat.domain.search.dto.response.StoreSearchResponse;
import com.groupeat.domain.search.repository.SearchRepository;
import com.groupeat.domain.store.entity.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private final SearchRepository searchRepository;

    public StoreSearchResponse.StoreListDTO searchStores(StoreSearchCondition condition) {
        // 조건 미입력(null) 요청 방어
        StoreSearchCondition safeCondition = StoreSearchCondition.defaultIfNull(condition);

        // 비즈니스 규칙 검증
        safeCondition.validate();

        // 가게 목록 및 카운트 조회
        List<Store> stores = searchRepository.searchStores(safeCondition);
        long totalElements = searchRepository.countStores(safeCondition);

        return SearchConverter.toStoreListDTO(stores, totalElements);
    }
}
