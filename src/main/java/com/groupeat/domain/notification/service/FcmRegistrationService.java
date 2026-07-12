package com.groupeat.domain.notification.service;

import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.member.enums.MemberStatus;
import com.groupeat.domain.member.exceptoin.MemberErrorStatus;
import com.groupeat.domain.member.repository.MemberRepository;
import com.groupeat.domain.notification.dto.request.FcmRegistrationRequest;
import com.groupeat.domain.notification.dto.response.FcmRegistrationResponse;
import com.groupeat.domain.notification.entity.FcmRegistration;
import com.groupeat.domain.notification.repository.FcmRegistrationRepository;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FcmRegistrationService {

    private final FcmRegistrationRepository fcmRegistrationRepository;
    private final MemberRepository memberRepository;

    // 활성 회원의 FCM registration token을 신규 등록하거나 기존 등록을 다시 활성화
    @Transactional
    public FcmRegistrationResponse register(Long memberId, FcmRegistrationRequest request) {
        validateActiveMember(memberId);

        LocalDateTime registeredAt = LocalDateTime.now();
        FcmRegistration registration = fcmRegistrationRepository
                .findByRegistrationToken(request.registrationToken().trim())
                .map(existing -> {
                    existing.reactivate(memberId, request.platform(), registeredAt);
                    return existing;
                })
                .orElseGet(() -> fcmRegistrationRepository.save(FcmRegistration.create(
                        memberId,
                        request.registrationToken().trim(),
                        request.platform(),
                        registeredAt
                )));

        return FcmRegistrationResponse.from(registration);
    }

    private void validateActiveMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorStatus.MEMBER_NOT_FOUND));

        if (member.getMemberStatus() != MemberStatus.ACTIVE) {
            throw new GeneralException(MemberErrorStatus.MEMBER_NOT_ACTIVE);
        }
    }
}
