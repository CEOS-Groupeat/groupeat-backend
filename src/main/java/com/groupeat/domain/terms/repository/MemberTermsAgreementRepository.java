package com.groupeat.domain.terms.repository;

import com.groupeat.domain.terms.entity.MemberTermsAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberTermsAgreementRepository extends JpaRepository<MemberTermsAgreement, Long> {
    List<MemberTermsAgreement> findByMemberIdAndTermsIdIn(Long memberId, Collection<Long> termsIds);

    Optional<MemberTermsAgreement> findFirstByMemberIdAndTermsIdOrderByIdDesc(Long memberId, Long termsId);

    @Query("""
            SELECT agreement
            FROM MemberTermsAgreement agreement
            WHERE agreement.memberId = :memberId
              AND agreement.termsId IN (
                  SELECT terms.id FROM Terms terms WHERE terms.required = false
              )
            """)
    List<MemberTermsAgreement> findOptionalTermsAgreementsByMemberId(@Param("memberId") Long memberId);
}
