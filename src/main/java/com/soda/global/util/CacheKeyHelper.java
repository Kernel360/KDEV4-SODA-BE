package com.soda.global.util;

import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;

// 정렬 키 일관되게 생성하기 위한 helper class
public class CacheKeyHelper {

    public static String generateSortKey(Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return "UNSORTED";
        }

        return sort.stream()
            .map(order -> order.getProperty() + "_" + order.getDirection().name())
            .collect(Collectors.joining(","));
    }

}
