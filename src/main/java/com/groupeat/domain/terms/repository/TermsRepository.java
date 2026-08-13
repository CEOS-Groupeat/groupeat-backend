package com.groupeat.domain.terms.repository;

import com.groupeat.domain.terms.entity.Terms;
import com.groupeat.domain.terms.enums.TermsTargetType;
import com.groupeat.domain.terms.enums.TermsType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TermsRepository extends JpaRepository<Terms, Long> {

    List<Terms> findByTargetTypeAndActiveTrue(TermsTargetType targetType);

    List<Terms> findByTargetTypeInAndActiveTrue(Collection<TermsTargetType> targetTypes);

    List<Terms> findByTargetTypeInAndActiveTrueAndRequiredFalse(Collection<TermsTargetType> targetTypes);

    Optional<Terms> findFirstByTargetTypeInAndActiveTrueAndRequiredFalseAndType(
            Collection<TermsTargetType> targetTypes,
            TermsType type
    );

    Optional<Terms> findByIdAndActiveTrue(Long id);

    List<Terms> findByIdIn(Collection<Long> ids);
}
