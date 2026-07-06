package com.groupeat.domain.review.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventType {
    LECTURE("강연"),
    SEMINAR("세미나"),
    WORKSHOP("워크숍"),
    SMALL_GROUP("소모임"),
    ETC("기타");

    @JsonValue
    private final String description;
}
