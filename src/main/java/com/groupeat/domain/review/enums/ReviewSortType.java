package com.groupeat.domain.review.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReviewSortType {
    LATEST("최신순"),
    HIGHEST_RATING("별점 높은 순"),
    LOWEST_RATING("별점 낮은 순");

    private final String description;
}