package com.soda.global.log.data.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
public class DataLogSearchResponse<T> {
    private List<T> content;
    private int totalPages;
    private long totalElements;

    public static <T> DataLogSearchResponse<T> from(Page<T> page) {
        return new DataLogSearchResponse<>(
                page.getContent(),
                page.getTotalPages(),
                page.getTotalElements()
        );
    }
}
