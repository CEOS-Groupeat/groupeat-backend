package com.groupeat.domain.store.service;

import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.domain.member.enums.MemberStatus;
import com.groupeat.domain.member.enums.MemberType;
import com.groupeat.domain.store.converter.StoreConverter;
import com.groupeat.domain.store.dto.response.OwnerStoreResponse;
import com.groupeat.domain.store.entity.Store;
import com.groupeat.domain.store.exception.StoreErrorStatus;
import com.groupeat.domain.store.repository.StoreRepository;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerStoreService {

    private final StoreRepository storeRepository;

    // 로그인한 사업자 회원의 가게 정보를 조회
    public OwnerStoreResponse getMyStore(AuthenticatedMember member) {
        validateActiveBusinessMember(member);

        Store store = storeRepository.findActiveStoreByBusinessMemberId(member.memberId())
                .orElseThrow(() -> new GeneralException(StoreErrorStatus.OWNER_STORE_NOT_FOUND));

        return StoreConverter.toOwnerStoreResponse(store);
    }

    private void validateActiveBusinessMember(AuthenticatedMember member) {
        if (member.memberType() != MemberType.BUSINESS) {
            throw new GeneralException(StoreErrorStatus.BUSINESS_MEMBER_REQUIRED);
        }

        if (member.memberStatus() != MemberStatus.ACTIVE) {
            throw new GeneralException(StoreErrorStatus.ACTIVE_BUSINESS_MEMBER_REQUIRED);
        }
    }
}
