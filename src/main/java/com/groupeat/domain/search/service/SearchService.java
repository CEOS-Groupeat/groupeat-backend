package com.groupeat.domain.search.service;

import com.groupeat.domain.search.converter.SearchConverter;
import com.groupeat.domain.search.dto.request.StoreSearchCondition;
import com.groupeat.domain.search.dto.response.StoreSearchResponse;
import com.groupeat.domain.search.repository.SearchRepository;
import com.groupeat.domain.store.entity.Store;
import com.groupeat.domain.store.entity.StoreOrderSchedule;
import com.groupeat.domain.store.repository.StoreOrderScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private final SearchRepository searchRepository;
    private final StoreOrderScheduleRepository storeOrderScheduleRepository;

    public StoreSearchResponse.StoreListDTO searchStores(StoreSearchCondition condition) {

        List<Store> stores = searchRepository.searchStores(condition);

        long totalElements = searchRepository.countStores(condition);
        Map<Long, StoreOrderSchedule> scheduleMap = getLatestScheduleMap(stores);

        return SearchConverter.toStoreListDTO(stores, totalElements, scheduleMap);
    }

    private Map<Long, StoreOrderSchedule> getLatestScheduleMap(List<Store> stores) {
        if (stores.isEmpty()) {
            return Map.of();
        }

        List<Long> storeIds = stores.stream()
                .map(Store::getId)
                .toList();

        return storeOrderScheduleRepository.findActiveSchedulesByStoreIds(storeIds).stream()
                .collect(Collectors.toMap(
                        schedule -> schedule.getStore().getId(),
                        Function.identity(),
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));
    }
}
