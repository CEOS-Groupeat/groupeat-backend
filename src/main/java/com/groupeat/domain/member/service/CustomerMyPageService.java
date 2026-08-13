package com.groupeat.domain.member.service;

import com.groupeat.domain.member.dto.request.CustomerAccountUpdateRequest;
import com.groupeat.domain.member.dto.response.CustomerAccountResponse;
import com.groupeat.domain.member.dto.response.CustomerMyPageResponse;
import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.member.entity.SocialAccount;
import com.groupeat.domain.member.enums.MemberStatus;
import com.groupeat.domain.member.exceptoin.MemberErrorStatus;
import com.groupeat.domain.member.repository.MemberRepository;
import com.groupeat.domain.member.repository.SocialAccountRepository;
import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.domain.orders.repository.OrderRepository;
import com.groupeat.domain.review.repository.ReviewRepository;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerMyPageService {

    private static final List<OrderStatus> MY_PAGE_ORDER_COUNT_STATUSES = List.of(
            OrderStatus.PAID,
            OrderStatus.ACCEPTED,
            OrderStatus.COMPLETED,
            OrderStatus.REJECTED,
            OrderStatus.CANCELLED
    );

    private final MemberRepository memberRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;

    public CustomerMyPageResponse getMyPage(Long memberId) {
        getActiveCustomer(memberId);
        long orderCount = orderRepository.countByMemberIdAndOrderStatusIn(memberId, MY_PAGE_ORDER_COUNT_STATUSES);
        long reviewCount = reviewRepository.countByMemberId(memberId);
        return CustomerMyPageResponse.of(orderCount, reviewCount);
    }

    public CustomerAccountResponse getAccount(Long memberId) {
        Member member = getActiveCustomer(memberId);
        SocialAccount socialAccount = socialAccountRepository.findByMemberId(memberId).orElse(null);
        return CustomerAccountResponse.from(member, socialAccount);
    }

    @Transactional
    public CustomerAccountResponse updateAccount(Long memberId, CustomerAccountUpdateRequest request) {
        Member member = getActiveCustomer(memberId);
        String email = normalizeOptionalText(request.email());

        if (email != null && memberRepository.existsByEmailAndIdNot(email, memberId)) {
            throw new GeneralException(MemberErrorStatus.EMAIL_ALREADY_EXISTS);
        }

        member.updateAccount(
                email,
                request.birthDate(),
                request.gender()
        );

        SocialAccount socialAccount = socialAccountRepository.findByMemberId(memberId).orElse(null);
        return CustomerAccountResponse.from(member, socialAccount);
    }

    // 고객 마이페이지 공통 접근 조건 검증
    public Member getActiveCustomer(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorStatus.MEMBER_NOT_FOUND));

        if (!member.isCustomer()) {
            throw new GeneralException(MemberErrorStatus.NOT_CUSTOMER_MEMBER);
        }
        if (member.getMemberStatus() != MemberStatus.ACTIVE) {
            throw new GeneralException(MemberErrorStatus.MEMBER_NOT_ACTIVE);
        }
        return member;
    }

    private String normalizeOptionalText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
