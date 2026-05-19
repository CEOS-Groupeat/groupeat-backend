package com.groupeat.domain.terms.repository;

import com.groupeat.domain.terms.entity.MemberTermsAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberTermsAgreementRepository extends JpaRepository<MemberTermsAgreement, Long> {
}
