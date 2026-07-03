package com.groupeat.domain.member.service;

import com.groupeat.domain.cart.repository.CartItemOptionRepository;
import com.groupeat.domain.cart.repository.CartItemRepository;
import com.groupeat.domain.cart.repository.CartRepository;
import com.groupeat.domain.member.dto.response.CustomerWithdrawalResponse;
import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.member.enums.MemberStatus;
import com.groupeat.domain.member.exceptoin.MemberErrorStatus;
import com.groupeat.domain.member.repository.MemberRepository;
import com.groupeat.domain.member.repository.SocialAccountRepository;
import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.domain.orders.repository.OrderRepository;
import com.groupeat.domain.terms.entity.MemberTermsAgreement;
import com.groupeat.domain.terms.repository.MemberTermsAgreementRepository;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomerWithdrawalService {

    private static final Set<OrderStatus> ACTIVE_ORDER_STATUSES =
            Set.of(OrderStatus.PENDING, OrderStatus.PAID, OrderStatus.ACCEPTED);

    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartItemOptionRepository cartItemOptionRepository;
    private final MemberTermsAgreementRepository termsAgreementRepository;
    private final SocialAccountRepository socialAccountRepository;

    @Transactional
    public CustomerWithdrawalResponse withdraw(Long memberId) {
        Member member = getActiveCustomerForUpdate(memberId);

        if (orderRepository.existsByMemberIdAndOrderStatusIn(memberId, ACTIVE_ORDER_STATUSES)) {
            throw new GeneralException(MemberErrorStatus.ACTIVE_ORDER_EXISTS);
        }

        deleteCart(memberId);
        withdrawOptionalTerms(memberId);
        socialAccountRepository.deleteByMemberId(memberId);
        member.withdraw();

        return new CustomerWithdrawalResponse("회원 탈퇴가 완료되었습니다.");
    }

    private Member getActiveCustomerForUpdate(Long memberId) {
        Member member = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorStatus.MEMBER_NOT_FOUND));

        if (!member.isCustomer()) {
            throw new GeneralException(MemberErrorStatus.NOT_CUSTOMER_MEMBER);
        }
        if (member.getMemberStatus() != MemberStatus.ACTIVE) {
            throw new GeneralException(MemberErrorStatus.MEMBER_NOT_ACTIVE);
        }
        return member;
    }

    // 외래 키 제약을 고려한 장바구니 하위 데이터 순차 삭제 진행
    private void deleteCart(Long memberId) {
        cartItemOptionRepository.deleteAllByCartMemberId(memberId);
        cartItemRepository.deleteAllByCartMemberId(memberId);
        cartRepository.deleteByMemberId(memberId);
    }

    private void withdrawOptionalTerms(Long memberId) {
        termsAgreementRepository.findOptionalTermsAgreementsByMemberId(memberId)
                .stream()
                .filter(MemberTermsAgreement::isAgreed)
                .forEach(agreement -> agreement.updateAgreement(false));
    }
}
