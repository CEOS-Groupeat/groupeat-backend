package com.groupeat.domain.store.service;

import com.groupeat.domain.store.converter.MenuConverter;
import com.groupeat.domain.store.dto.response.MenuListResponse;
import com.groupeat.domain.store.entity.Menu;
import com.groupeat.domain.store.exception.StoreErrorStatus;
import com.groupeat.domain.store.repository.MenuRepository;
import com.groupeat.domain.store.repository.StoreRepository;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;

    public MenuListResponse getStoreMenus(Long storeId) {

        if (!storeRepository.existsById(storeId)) {
            throw new GeneralException(StoreErrorStatus.STORE_NOT_FOUND);
        }

        List<Menu> menus = menuRepository.findAllByStoreId(storeId);

        return MenuConverter.toMenuListResponse(menus);
    }
}
