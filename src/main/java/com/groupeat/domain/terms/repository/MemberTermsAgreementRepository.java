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
}
