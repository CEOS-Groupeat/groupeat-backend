package com.groupeat.global.util;

import com.groupeat.global.dto.CursorResponse;

import java.util.List;
import java.util.function.Function;

public class CursorUtils {

    private CursorUtils() {
        throw new IllegalStateException("Utility class");
    }


    public static <T> CursorResponse<T> getCursorResponse(List<T> content, int size, Function<T, Long> cursorExtractor) {
        boolean hasNext = false;
        Long nextCursor = null;

        // 요청한 사이즈보다 많이 가져왔다면 다음 페이지가 존재함 (hasNext = true)
        if (content.size() > size) {
            hasNext = true;
            content.remove(size); // 확인용으로 가져온 마지막 데이터 하나를 안전하게 제거
        }

        // 리스트가 비어있지 않다면, 남은 데이터 중 가장 마지막 요소의 커서 값을 추출
        if (!content.isEmpty()) {
            nextCursor = cursorExtractor.apply(content.getLast());
        }

        return new CursorResponse<>(content, hasNext, nextCursor);
    }
}
