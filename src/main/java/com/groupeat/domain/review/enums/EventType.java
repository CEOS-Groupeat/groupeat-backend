package com.groupeat.domain.review.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.groupeat.domain.review.exception.ReviewErrorStatus;
import com.groupeat.global.exception.GeneralException;
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

    @JsonCreator
    public static EventType from(String value) {
        for (EventType type : EventType.values()) {
            if (type.name().equalsIgnoreCase(value) || type.getDescription().equals(value)) {
                return type;
            }
        }
        throw new GeneralException(ReviewErrorStatus.INVALID_EVENT_TYPE);
    }
}
