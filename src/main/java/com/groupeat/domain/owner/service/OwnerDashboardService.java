package com.groupeat.domain.owner.service;

import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.member.enums.MemberStatus;
import com.groupeat.domain.member.enums.MemberType;
import com.groupeat.domain.member.exceptoin.MemberErrorStatus;
import com.groupeat.domain.member.repository.MemberRepository;
import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.domain.owner.dto.response.OwnerDashboardSummaryResponse;
import com.groupeat.domain.owner.exception.OwnerErrorStatus;
import com.groupeat.domain.owner.repository.OwnerOrderQueryRepository;
import com.groupeat.domain.signup.exception.SignupErrorStatus;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerDashboardService {

    private final OwnerOrderQueryRepository ownerOrderQueryRepository;
    private final MemberRepository memberRepository;

    public OwnerDashboardSummaryResponse getDashboardSummary(Long ownerId) {
        validateOwner(ownerId);

        List<OrderStatus> targetStatuses = List.of(OrderStatus.PAID, OrderStatus.ACCEPTED, OrderStatus.COMPLETED);
        Map<OrderStatus, Long> counts = ownerOrderQueryRepository.countOrdersByStatuses(ownerId, targetStatuses);

        if (counts == null) {
            throw new GeneralException(OwnerErrorStatus.DASHBOARD_DATA_NOT_AVAILABLE);
        }

        return OwnerDashboardSummaryResponse.builder()
                .waitingCount(counts.getOrDefault(OrderStatus.PAID, 0L))
                .confirmedCount(counts.getOrDefault(OrderStatus.ACCEPTED, 0L))
                .completedCount(counts.getOrDefault(OrderStatus.COMPLETED, 0L))
                .build();
    }

    private void validateOwner(Long ownerId) {
        Member member = memberRepository.findById(ownerId)
                .orElseThrow(() -> new GeneralException(SignupErrorStatus.MEMBER_NOT_FOUND));

        if (member.getMemberStatus() != MemberStatus.ACTIVE) {
            throw new GeneralException(MemberErrorStatus.MEMBER_NOT_ACTIVE);
        }

        if (member.getMemberType() != MemberType.BUSINESS) {
            throw new GeneralException(SignupErrorStatus.NOT_BUSINESS_MEMBER);
        }
    }
}
