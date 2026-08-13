package com.groupeat.domain.terms.repository;

import com.groupeat.domain.terms.entity.MemberTermsAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberTermsAgreementRepository extends JpaRepository<MemberTermsAgreement, Long> {
    @Query("""
            select agreement
            from MemberTermsAgreement agreement
            where agreement.memberId = :memberId
              and agreement.termsId in :termsIds
              and not exists (
                  select 1
                  from MemberTermsAgreement latest
                  where latest.memberId = agreement.memberId
                    and latest.termsId = agreement.termsId
                    and latest.id > agreement.id
              )
            """)
    List<MemberTermsAgreement> findLatestByMemberIdAndTermsIdIn(
            @Param("memberId") Long memberId,
            @Param("termsIds") Collection<Long> termsIds
    );

    Optional<MemberTermsAgreement> findFirstByMemberIdAndTermsIdOrderByIdDesc(Long memberId, Long termsId);

    @Query("""
            SELECT agreement
            FROM MemberTermsAgreement agreement
            WHERE agreement.memberId = :memberId
              AND agreement.termsId IN (
                  SELECT terms.id FROM Terms terms WHERE terms.required = false
              )
              AND NOT EXISTS (
                  SELECT 1
                  FROM MemberTermsAgreement latest
                  WHERE latest.memberId = agreement.memberId
                    AND latest.termsId = agreement.termsId
                    AND latest.id > agreement.id
              )
            """)
    List<MemberTermsAgreement> findLatestOptionalTermsAgreementsByMemberId(@Param("memberId") Long memberId);
}
