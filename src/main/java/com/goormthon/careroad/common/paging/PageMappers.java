package com.goormthon.careroad.common.paging;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public final class PageMappers {
    private PageMappers() {}

    /** Spring Data Page -> 표준 응답 컨테이너로 변환 */
    public static <T> PagedList<T> toPagedList(Page<T> page, Pageable pageable) {
        List<String> sort = pageable.getSort().stream()
                .map(o -> o.getProperty() + "," + (o.isAscending() ? "asc" : "desc"))
                .toList();
        return new PagedList<>(
                page.getContent(),
                pageable.getPageNumber(),
                pageable.getPageSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                sort
        );
    }

    /** 허용된 정렬 필드만 통과 (보안/안정성) — B와 합의 필요: allowed 목록 */
    public static Sort whitelistSort(Sort sort, List<String> allowed) {
        return Sort.by(
                sort.stream()
                        .filter(o -> allowed.contains(o.getProperty()))
                        .map(o -> o.isAscending() ? Sort.Order.asc(o.getProperty()) : Sort.Order.desc(o.getProperty()))
                        .toList()
        );
    }

    /** size 상한/하한 캡 — B와 합의 필요: min/max 정책 */
    public static int capSize(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
