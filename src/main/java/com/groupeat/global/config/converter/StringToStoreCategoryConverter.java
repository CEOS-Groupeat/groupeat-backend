package com.groupeat.global.config.converter;

import com.groupeat.domain.store.entity.StoreCategory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToStoreCategoryConverter implements Converter<String, StoreCategory> {

    @Override
    public StoreCategory convert(String source) {
        return StoreCategory.from(source);
    }
}