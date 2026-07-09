package com.groupeat.domain.owner.service;

import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.member.enums.MemberStatus;
import com.groupeat.domain.member.exceptoin.MemberErrorStatus;
import com.groupeat.domain.member.repository.MemberRepository;
import com.groupeat.domain.member.repository.SocialAccountRepository;
import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.domain.orders.repository.OrderRepository;
import com.groupeat.domain.owner.dto.response.OwnerWithdrawalResponse;
import com.groupeat.domain.settlement.enums.SettlementStatus;
import com.groupeat.domain.settlement.repository.SettlementRepository;
import com.groupeat.domain.store.entity.Store;
import com.groupeat.domain.store.repository.StoreRepository;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class OwnerWithdrawalService {

    private static final Set<OrderStatus> ACTIVE_ORDER_STATUSES =
            Set.of(OrderStatus.PENDING, OrderStatus.PAID, OrderStatus.ACCEPTED);

    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final SettlementRepository settlementRepository;
    private final StoreRepository storeRepository;
    private final SocialAccountRepository socialAccountRepository;

    // 사업자 회원 탈퇴
    @Transactional
    public OwnerWithdrawalResponse withdraw(Long memberId) {
        Member member = getActiveBusinessOwnerForUpdate(memberId);

        validateWithdrawable(memberId); // 탈퇴 정책 조건 확인
        socialAccountRepository.deleteByMemberId(memberId);
        stopOperatingStore(memberId);
        member.withdraw();

        return new OwnerWithdrawalResponse("사업자 회원 탈퇴가 완료되었습니다.");
    }

    private Member getActiveBusinessOwnerForUpdate(Long memberId) {
        Member member = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorStatus.MEMBER_NOT_FOUND));

        if (!member.isBusiness()) {
            throw new GeneralException(MemberErrorStatus.NOT_BUSINESS_MEMBER);
        }
        if (member.getMemberStatus() != MemberStatus.ACTIVE) {
            throw new GeneralException(MemberErrorStatus.MEMBER_NOT_ACTIVE);
        }
        return member;
    }

    private void validateWithdrawable(Long memberId) {
        if (orderRepository.existsByStoreOwnerIdAndOrderStatusIn(memberId, ACTIVE_ORDER_STATUSES)) {
            throw new GeneralException(MemberErrorStatus.ACTIVE_ORDER_EXISTS);
        }
        if (settlementRepository.existsByOwnerIdAndSettlementStatus(memberId, SettlementStatus.PENDING)) {
            throw new GeneralException(MemberErrorStatus.PENDING_SETTLEMENT_EXISTS);
        }
    }

    // 주문 및 정산 이력 보존을 위해 가게는 물리 삭제하지 않고 일반 조회에서만 제외
    private void stopOperatingStore(Long memberId) {
        storeRepository.findByBusinessMemberId(memberId)
                .filter(store -> store.getDeletedAt() == null)
                .ifPresent(Store::markAsDeleted);
    }
}
