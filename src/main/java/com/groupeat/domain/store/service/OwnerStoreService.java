package com.groupeat.domain.store.service;

import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.domain.store.converter.StoreConverter;
import com.groupeat.domain.store.dto.request.OwnerStoreUpdateRequest;
import com.groupeat.domain.store.dto.response.OwnerStoreResponse;
import com.groupeat.domain.store.dto.response.OwnerStoreUpsertResult;
import com.groupeat.domain.store.entity.Store;
import com.groupeat.domain.store.exception.StoreErrorStatus;
import com.groupeat.domain.store.repository.StoreRepository;
import com.groupeat.domain.store.validator.StoreBusinessMemberValidator;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerStoreService {

    private final StoreRepository storeRepository;
    private final StoreBusinessMemberValidator storeBusinessMemberValidator;

    // 로그인한 사업자 회원의 가게 정보를 조회
    public OwnerStoreResponse getMyStore(AuthenticatedMember member) {
        storeBusinessMemberValidator.validateActiveBusinessMember(member);

        Store store = findMyStore(member.memberId());

        return StoreConverter.toOwnerStoreResponse(store);
    }

    // 로그인한 사업자 회원의 가게 정보를 생성 또는 수정
    @Transactional
    @CacheEvict(cacheNames = "highDiscountStores", allEntries = true)
    public OwnerStoreUpsertResult upsertMyStore(AuthenticatedMember member, OwnerStoreUpdateRequest request) {
        storeBusinessMemberValidator.validateActiveBusinessMember(member);

        Store store = storeRepository.findActiveStoreByBusinessMemberId(member.memberId())
                .orElse(null);
        boolean created = store == null;

        if (created) {
            store = createMyStore(member.memberId(), request);
        } else {
            updateMyStore(store, request);
        }

        return new OwnerStoreUpsertResult(StoreConverter.toOwnerStoreResponse(store), created);
    }

    private Store createMyStore(Long businessMemberId, OwnerStoreUpdateRequest request) {
        OwnerStoreUpdateRequest.LocationDTO location = request.location();
        OwnerStoreUpdateRequest.DiscountDTO discount = request.discount();

        Store store = Store.builder()
                .ownerId(businessMemberId)
                .storeName(request.storeName())
                .imageUrl(request.imageUrl())
                .address(location.address())
                .district(location.district())
                .neighborhood(location.neighborhood())
                .detailAddress(location.detailAddress())
                .category(request.category())
                .phoneNumber(request.phoneNumber())
                .description(request.description())
                .discountConditionQuantity(discount != null ? discount.conditionQuantity() : null)
                .discountRate(discount != null ? discount.rate() : null)
                .build();

        return storeRepository.save(store);
    }

    private void updateMyStore(Store store, OwnerStoreUpdateRequest request) {
        OwnerStoreUpdateRequest.LocationDTO location = request.location();
        OwnerStoreUpdateRequest.DiscountDTO discount = request.discount();

        store.updateOwnerStoreInfo(
                request.storeName(),
                request.imageUrl(),
                location.address(),
                location.district(),
                location.neighborhood(),
                location.detailAddress(),
                request.category(),
                request.phoneNumber(),
                request.description(),
                discount != null ? discount.conditionQuantity() : null,
                discount != null ? discount.rate() : null
        );
    }

    private Store findMyStore(Long businessMemberId) {
        return storeRepository.findActiveStoreByBusinessMemberId(businessMemberId)
                .orElseThrow(() -> new GeneralException(StoreErrorStatus.OWNER_STORE_NOT_FOUND));
    }
}
