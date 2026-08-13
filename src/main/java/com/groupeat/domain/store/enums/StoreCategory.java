package com.groupeat.domain.store.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.groupeat.domain.store.exception.StoreErrorStatus;
import com.groupeat.global.exception.GeneralException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum StoreCategory {

    SANDWICH_KIMBAP("샌드위치&김밥"),
    DESSERT("디저트"),
    BEVERAGE("음료"),
    ETC("기타");

    private final String description;

    @JsonCreator
    public static StoreCategory from(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return Stream.of(StoreCategory.values())
                .filter(category -> category.name().equals(description) || category.getDescription().equals(description))
                .findFirst()
                .orElseThrow(() -> new GeneralException(StoreErrorStatus.INVALID_CATEGORY));
    }
}
