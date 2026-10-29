package com.groupeat.domain.search.cursor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.groupeat.domain.search.enums.StoreSortType;
import com.groupeat.domain.search.dto.request.StoreSearchCondition;
import com.groupeat.domain.search.exception.SearchErrorStatus;
import com.groupeat.domain.store.entity.Store;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;

@Component
@RequiredArgsConstructor
public class StoreSearchCursorCodec {

    private final ObjectMapper objectMapper;

    public String encode(Store store, StoreSearchCondition condition) {
        Object value = switch (condition.sortType()) {
            case NONE -> store.getStoreName();
            case DISCOUNT_DESC -> store.getDiscountRate();
            case PRICE_ASC -> store.getMinPrice();
            case PRICE_DESC -> store.getMaxPrice();
            case RATING_DESC -> store.getReviewRating();
            case ORDER_DESC -> store.getId();
        };
        StoreSearchCursor cursor = new StoreSearchCursor(1, condition.sortType(), store.getId(),
                value == null ? null : value.toString(), conditionKey(condition));
        try {
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(objectMapper.writeValueAsBytes(cursor));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("검색 커서 직렬화 실패", e);
        }
    }

    public StoreSearchCursor decode(StoreSearchCondition condition) {
        if (condition.cursor() == null) return null;
        try {
            if (condition.cursor().isBlank() || condition.cursor().length() > 2048) {
                throw new IllegalArgumentException();
            }
            if (!condition.cursor().matches("[A-Za-z0-9_-]+")) throw new IllegalArgumentException();
            JsonNode payload = objectMapper.reader().with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                    .readTree(Base64.getUrlDecoder().decode(condition.cursor()));
            if (payload == null || !payload.isObject() || payload.size() != 5
                    || !payload.path("version").isIntegralNumber() || !payload.path("version").canConvertToInt()
                    || !payload.path("storeId").isIntegralNumber() || !payload.path("storeId").canConvertToLong()
                    || !payload.path("sortType").isTextual() || !payload.path("conditionKey").isTextual()
                    || !payload.has("value") || !(payload.get("value").isNull() || payload.get("value").isTextual())) {
                throw new IllegalArgumentException();
            }
            StoreSearchCursor cursor = objectMapper.treeToValue(payload, StoreSearchCursor.class);
            if (cursor == null || cursor.version() != 1 || cursor.storeId() <= 0
                    || cursor.sortType() != condition.sortType()
                    || !conditionKey(condition).equals(cursor.conditionKey())) {
                throw new IllegalArgumentException();
            }
            Object value = cursor.sortValue();
            if (value instanceof Double rating && !Double.isFinite(rating)) {
                throw new IllegalArgumentException();
            }
            if ((cursor.sortType() == StoreSortType.NONE && value == null)
                    || (cursor.sortType() == StoreSortType.ORDER_DESC
                    && !Long.valueOf(cursor.storeId()).equals(value))) {
                throw new IllegalArgumentException();
            }
            return cursor;
        } catch (IllegalArgumentException | IOException e) {
            throw new GeneralException(SearchErrorStatus.INVALID_CURSOR);
        }
    }

    private String conditionKey(StoreSearchCondition condition) {
        // 페이지 크기는 변경할 수 있지만 필터/정렬 변경 시 첫 페이지부터 다시 조회
        var times = condition.pickupTimes() == null ? null
                : condition.pickupTimes().stream().distinct().sorted().map(Object::toString).toList();
        try {
            String json = objectMapper.writeValueAsString(Arrays.asList(condition.cleanKeyword(),
                    condition.district(), condition.pickupDate() == null ? null : condition.pickupDate().toString(),
                    times == null || times.isEmpty() ? null : times, condition.quantity(), condition.budget(),
                    condition.category(), condition.sortType()));
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(json.getBytes(StandardCharsets.UTF_8)));
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("검색 조건 식별값 생성 실패", e);
        }
    }
}
