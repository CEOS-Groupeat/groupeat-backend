package com.groupeat.domain.store.repository;

import com.groupeat.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {

    @Query("SELECT s FROM Store s WHERE s.id = :storeId AND s.deletedAt IS NULL")
    Optional<Store> findActiveStoreById(@Param("storeId") Long storeId);

    @Query("SELECT s FROM Store s WHERE s.ownerId = :businessMemberId AND s.deletedAt IS NULL")
    Optional<Store> findActiveStoreByBusinessMemberId(@Param("businessMemberId") Long businessMemberId);

}
