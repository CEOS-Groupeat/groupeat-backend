package com.groupeat.domain.admin.business.repository;

import com.groupeat.domain.business.entity.BusinessProfile;
import com.groupeat.domain.business.enums.BusinessVerificationStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.groupeat.domain.business.entity.QBusinessProfile.businessProfile;

@Repository
@RequiredArgsConstructor
public class AdminBusinessQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<BusinessProfile> findVerificationsByCursor(List<BusinessVerificationStatus> statuses, Long lastProfileId, int size) {
        return queryFactory
                .selectFrom(businessProfile)
                .where(
                        statusIn(statuses),
                        cursorCondition(lastProfileId)
                )
                .orderBy(businessProfile.id.desc())
                .limit(size + 1)
                .fetch();
    }

    // 전체 개수 카운트
    public long countVerifications(List<BusinessVerificationStatus> statuses) {
        Long count = queryFactory
                .select(businessProfile.count())
                .from(businessProfile)
                .where(statusIn(statuses))
                .fetchOne();
        return count != null ? count : 0L;
    }

    private BooleanExpression statusIn(List<BusinessVerificationStatus> statuses) {
        return (statuses == null || statuses.isEmpty()) ? null : businessProfile.verificationStatus.in(statuses);
    }

    private BooleanExpression cursorCondition(Long lastProfileId) {
        return lastProfileId == null ? null : businessProfile.id.lt(lastProfileId);
    }
}
