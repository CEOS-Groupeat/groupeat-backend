package com.groupeat.global.dto;

import java.util.List;
import java.util.function.Function;

public record CursorResponse<T>(
        List<T> content,
        boolean hasNext,
        Long nextCursor
) {
    public <U> CursorResponse<U> map(Function<T, U> converter) {
        List<U> mappedContent = content.stream()
                .map(converter)
                .toList();

        return new CursorResponse<>(mappedContent, hasNext, nextCursor);
    }
}