package com.groupeat.domain.store.repository;

import com.groupeat.domain.store.entity.MenuOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MenuOptionRepository extends JpaRepository<MenuOption, Long> {

    // 주어진 옵션 ID들이 모두 특정 메뉴(menuId)에 속해 있는지 개수를 세는 쿼리
    @Query("SELECT COUNT(mo) FROM MenuOption mo " +
            "JOIN mo.optionGroup mog WHERE mo.id " +
            "IN :optionIds AND mog.menu.id = :menuId")
    long countValidOptions(@Param("optionIds") List<Long> optionIds, @Param("menuId") Long menuId);
}