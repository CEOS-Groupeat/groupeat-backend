package com.groupeat.domain.store.repository;

import com.groupeat.domain.store.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    @Query("SELECT m FROM Menu m WHERE m.store.id = :storeId AND m.deletedAt IS NULL")
    List<Menu> findAllByStoreId(@Param("storeId") Long storeId);

    @Query("SELECT m FROM Menu m WHERE m.id = :menuId AND m.store.id = :storeId AND m.deletedAt IS NULL")
    Optional<Menu> findActiveByIdAndStoreId(@Param("menuId") Long menuId, @Param("storeId") Long storeId);

}
