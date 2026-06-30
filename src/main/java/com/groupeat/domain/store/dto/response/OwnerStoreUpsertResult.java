package com.groupeat.domain.store.dto.response;

public record OwnerStoreUpsertResult(
        OwnerStoreResponse store,
        boolean created
) {
}
