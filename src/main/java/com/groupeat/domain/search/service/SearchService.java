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

        List<Store> stores = searchRepository.searchStores(condition);

        long totalElements = searchRepository.countStores(condition);

        return SearchConverter.toStoreListDTO(stores, totalElements);
    }
}