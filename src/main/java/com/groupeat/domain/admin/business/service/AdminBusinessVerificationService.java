package com.groupeat.domain.admin.business.service;

import com.groupeat.domain.admin.business.converter.AdminBusinessConverter;
import com.groupeat.domain.admin.business.dto.request.AdminVerificationProcessRequest;
import com.groupeat.domain.admin.business.dto.response.AdminVerificationDetailResponse;
import com.groupeat.domain.admin.business.dto.response.AdminVerificationListResponse;
import com.groupeat.domain.admin.business.dto.response.AdminVerificationProcessResponse;
import com.groupeat.domain.admin.business.enums.AdminVerificationFilterType;
import com.groupeat.domain.admin.business.exception.AdminErrorStatus;
import com.groupeat.domain.admin.business.repository.AdminBusinessQueryRepository;
import com.groupeat.domain.business.entity.BusinessProfile;
import com.groupeat.domain.business.enums.BusinessVerificationStatus;
import com.groupeat.domain.business.repository.BusinessProfileRepository;
import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.member.repository.MemberRepository;
import com.groupeat.global.dto.CursorResponse;
import com.groupeat.global.exception.GeneralException;
import com.groupeat.global.util.CursorUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminBusinessVerificationService {

    private final BusinessProfileRepository businessProfileRepository;
    private final AdminBusinessQueryRepository adminQueryRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public AdminVerificationListResponse.VerificationListDTO getVerificationList(
            AdminVerificationFilterType filter, Long lastProfileId, int size
    ) {
        List<BusinessVerificationStatus> statusList =
                (filter == null || filter == AdminVerificationFilterType.ALL) ? null : filter.getMappedStatuses();

        long totalElements = adminQueryRepository.countVerifications(statusList);

        List<BusinessProfile> profiles = adminQueryRepository.findVerificationsByCursor(statusList, lastProfileId, size);

        CursorResponse<BusinessProfile> cursorResponse =
                CursorUtils.getCursorResponse(profiles, size, BusinessProfile::getId);

        return AdminBusinessConverter.toVerificationListDTO(cursorResponse, totalElements);
    }

    @Transactional(readOnly = true)
    public AdminVerificationDetailResponse getVerificationDetail(Long profileId) {
        BusinessProfile profile = businessProfileRepository.findById(profileId)
                .orElseThrow(() -> new GeneralException(AdminErrorStatus.VERIFICATION_NOT_FOUND));

        Member member = memberRepository.findById(profile.getMemberId())
                .orElseThrow(() -> new GeneralException(AdminErrorStatus.MEMBER_NOT_FOUND));

        return AdminBusinessConverter.toVerificationDetailResponse(profile, member);
    }

    public AdminVerificationProcessResponse processVerification(Long adminId, Long profileId, AdminVerificationProcessRequest request) {
        // 사업자 프로필 조회
        BusinessProfile profile = businessProfileRepository.findByIdWithPessimisticLock(profileId)
                .orElseThrow(() -> new GeneralException(AdminErrorStatus.VERIFICATION_NOT_FOUND));

        // 이미 처리된 건인지 검증
        if (profile.getVerificationStatus() != BusinessVerificationStatus.PENDING) {
            throw new GeneralException(AdminErrorStatus.ALREADY_PROCESSED_VERIFICATION);
        }

        // 연결된 회원 조회
        Member member = memberRepository.findById(profile.getMemberId())
                .orElseThrow(() -> new GeneralException(AdminErrorStatus.MEMBER_NOT_FOUND));

        // 승인 / 반려 분기 처리
        if (request.isApprove()) {
            profile.approve(adminId);
            member.approveBusiness();
        } else {
            if (request.rejectReason() == null || request.rejectReason().isBlank()) {
                throw new GeneralException(AdminErrorStatus.REJECT_REASON_REQUIRED);
            }
            profile.reject(adminId, request.rejectReason());
            member.rejectBusiness();
        }

        return AdminBusinessConverter.toVerificationProcessResponse(profile);
    }
}
