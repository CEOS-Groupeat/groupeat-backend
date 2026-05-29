package com.groupeat.global.config.converter;

import com.groupeat.domain.store.enums.StoreRegion;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToStoreRegionConverter implements Converter<String, StoreRegion> {

    @Override
    public StoreRegion convert(String source) {
        return StoreRegion.from(source);
    }
}