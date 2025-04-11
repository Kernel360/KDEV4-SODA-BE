package com.soda.global.response;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * API 응답 시 페이징된 데이터를 감싸는 클래스
 * @param <T> 페이징된 데이터 목록의 타입
 */
@Getter
public class PagedData<T> {

    private final List<T> content;        // 현재 페이지에 포함된 데이터 목록
    private final int pageNumber;         // 현재 페이지 번호 (0부터 시작)
    private final int pageSize;           // 페이지 당 데이터 수
    private final long totalElements;     // 전체 데이터 개수
    private final int totalPages;         // 전체 페이지 수
    private final boolean first;          // 첫 번째 페이지 여부
    private final boolean last;           // 마지막 페이지 여부

    /**
     * Spring Data의 Page 객체를 사용하여 ApiResponseData를 생성하는 생성자
     * @param page Page 객체
     */
    public PagedData(Page<T> page) {
        this.content = page.getContent();         // 실제 데이터 리스트
        this.pageNumber = page.getNumber();       // 현재 페이지 번호 (0-based)
        this.pageSize = page.getSize();           // 페이지 크기
        this.totalElements = page.getTotalElements(); // 전체 요소 수
        this.totalPages = page.getTotalPages();     // 전체 페이지 수
        this.first = page.isFirst();              // 첫 페이지인지 여부
        this.last = page.isLast();                // 마지막 페이지인지 여부
    }

}
