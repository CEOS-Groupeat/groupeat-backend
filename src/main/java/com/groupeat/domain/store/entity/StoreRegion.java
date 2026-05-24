package com.groupeat.domain.store.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.groupeat.domain.store.exception.StoreErrorStatus;
import com.groupeat.global.exception.GeneralException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum StoreRegion {

    GANGNAM("강남구"),
    GANGDONG("강동구"),
    GANGBUK("강북구"),
    GANGSEO("강서구"),
    GWANAK("관악구"),
    GWANGJIN("광진구"),
    GURO("구로구"),
    GEUMCHEON("금천구"),
    NOWON("노원구"),
    DOBONG("도봉구"),
    DONGDAEMUN("동대문구"),
    DONGJAK("동작구"),
    MAPO("마포구"),
    SEODAEMUN("서대문구"),
    SEOCHO("서초구"),
    SEONGDONG("성동구"),
    SEONGBUK("성북구"),
    SONGPA("송파구"),
    YANGCHEON("양천구"),
    YEONGDEUNGPO("영등포구"),
    YONGSAN("용산구"),
    EUNPYEONG("은평구"),
    JONGNO("종로구"),
    JUNG("중구"),
    JUNGNANG("중랑구");

    private final String description;

    @JsonCreator
    public static StoreRegion from(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return Stream.of(StoreRegion.values())
                .filter(region -> region.getDescription().equals(description))
                .findFirst()
                .orElseThrow(() -> new GeneralException(StoreErrorStatus.INVALID_CATEGORY));
    }
}