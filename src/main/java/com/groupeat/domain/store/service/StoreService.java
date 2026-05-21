package com.groupeat.domain.store.service;

import com.groupeat.domain.store.converter.StoreConverter;
import com.groupeat.domain.store.dto.response.PickupTimeResponse;
import com.groupeat.domain.store.dto.response.StoreDetailResponse;
import com.groupeat.domain.store.entity.Store;
import com.groupeat.domain.store.exception.StoreErrorStatus;
import com.groupeat.domain.store.repository.StoreRepository;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;

    public StoreDetailResponse getStoreInfo(Long storeId) {
        Store store = storeRepository.findActiveStoreById(storeId)
                .orElseThrow(() -> new GeneralException(StoreErrorStatus.STORE_NOT_FOUND));

        return StoreConverter.toStoreDetailResponse(store);
    }

    public PickupTimeResponse getAvailablePickupTimes(Long storeId, LocalDate date) {

        Store store = storeRepository.findActiveStoreById(storeId)
                .orElseThrow(() -> new GeneralException(StoreErrorStatus.STORE_NOT_FOUND));

        // 휴무일 체크
        String dayOfWeek = date.getDayOfWeek().name();
        if (store.getClosedDays() != null && store.getClosedDays().contains(dayOfWeek)) {
            return PickupTimeResponse.builder()
                    .date(date)
                    .dailyAvailableQuantity(0) // 0이면(휴무일이면) 휴무일 처리
                    .build();
        }

        // 일일 총 수량 (임시 값)
        int defaultDailyQuantity = store.getDiscountConditionQuantity() != null ?
                store.getDiscountConditionQuantity() * 2 : 100;

        return PickupTimeResponse.builder()
                .date(date)
                .dailyAvailableQuantity(defaultDailyQuantity)
                .openTime(store.getPickupOpenTime())
                .closeTime(store.getPickupCloseTime())
                .intervalMinutes(30) // 30분 간격
                .build();
    }
}
