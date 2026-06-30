package com.groupeat.domain.store.converter;

import com.groupeat.domain.store.dto.response.MenuListResponse;
import com.groupeat.domain.store.dto.response.OwnerMenuResponse;
import com.groupeat.domain.store.entity.Menu;

import java.util.List;
import java.util.stream.Collectors;

public class MenuConverter {

    public static MenuListResponse toMenuListResponse(List<Menu> menus) {
        List<MenuListResponse.MenuDetailDTO> menuDetails = menus.stream()
                .map(menu -> MenuListResponse.MenuDetailDTO.builder()
                        .menuId(menu.getId())
                        .name(menu.getName())
                        .basePrice(menu.getBasePrice())
                        .description(menu.getDescription())
                        .imageUrl(menu.getImageUrl())
                        .optionGroups(menu.getOptionGroups().stream()
                                .map(group -> MenuListResponse.OptionGroupDTO.builder()
                                        .optionGroupId(group.getId())
                                        .name(group.getName())
                                        .isRequired(group.getIsRequired())
                                        .isMultiple(group.getIsMultiple())
                                        .options(group.getOptions().stream()
                                                .map(opt -> MenuListResponse.OptionDTO.builder()
                                                        .optionId(opt.getId())
                                                        .name(opt.getName())
                                                        .additionalPrice(opt.getAdditionalPrice())
                                                        .build())
                                                .collect(Collectors.toList()))
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        return MenuListResponse.builder().menus(menuDetails).build();
    }

    public static OwnerMenuResponse toOwnerMenuResponse(Menu menu) {
        return OwnerMenuResponse.builder()
                .menuId(menu.getId())
                .name(menu.getName())
                .basePrice(menu.getBasePrice())
                .description(menu.getDescription())
                .imageUrl(menu.getImageUrl())
                .optionGroups(menu.getOptionGroups().stream()
                        .map(group -> OwnerMenuResponse.OptionGroupResponse.builder()
                                .optionGroupId(group.getId())
                                .name(group.getName())
                                .isRequired(group.getIsRequired())
                                .isMultiple(group.getIsMultiple())
                                .options(group.getOptions().stream()
                                        .map(option -> OwnerMenuResponse.OptionResponse.builder()
                                                .optionId(option.getId())
                                                .name(option.getName())
                                                .additionalPrice(option.getAdditionalPrice())
                                                .build())
                                        .toList())
                                .build())
                        .toList())
                .build();
    }
}
