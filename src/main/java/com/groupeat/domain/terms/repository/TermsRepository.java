package com.groupeat.domain.terms.repository;

import com.groupeat.domain.terms.entity.Terms;
import com.groupeat.domain.terms.enums.TermsTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TermsRepository extends JpaRepository<Terms, Long> {

    List<Terms> findByTargetTypeAndActiveTrue(TermsTargetType targetType);

    List<Terms> findByIdIn(Collection<Long> ids);
}
