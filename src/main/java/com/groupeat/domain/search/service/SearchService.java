package com.groupeat.domain.search.service;

import com.groupeat.domain.search.converter.SearchConverter;
import com.groupeat.domain.search.cursor.StoreSearchCursorCodec;
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
    private final StoreSearchCursorCodec cursorCodec;

    public StoreSearchResponse.StoreListDTO searchStores(StoreSearchCondition condition) {
        // 조건 미입력(null) 요청 방어
        StoreSearchCondition safeCondition = StoreSearchCondition.defaultIfNull(condition);

        // 비즈니스 규칙 검증
        safeCondition.validate();

        // 가게 목록 및 카운트 조회
        var cursor = cursorCodec.decode(safeCondition);
        searchRepository.forceCustomPlanForCurrentTransaction();
        List<Store> fetched = searchRepository.searchStores(safeCondition, cursor);
        boolean hasNext = fetched.size() > safeCondition.pageSize();
        List<Store> stores = hasNext ? fetched.subList(0, safeCondition.pageSize()) : fetched;
        String nextCursor = hasNext ? cursorCodec.encode(stores.getLast(), safeCondition) : null;
        long totalElements = searchRepository.countStores(safeCondition);

        return SearchConverter.toStoreListDTO(stores, totalElements, hasNext, nextCursor);
    }
}
