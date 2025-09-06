package com.goormthon.careroad.common.paging;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "PagedList", description = "페이지 기반 목록 응답 컨테이너")
public class PagedList<T> {
    @Schema(description = "목록 아이템")
    private List<T> items;

    @Schema(description = "현재 페이지(0부터 시작)", example = "0")
    private int page;

    @Schema(description = "페이지 크기", example = "20")
    private int size;

    @Schema(description = "전체 아이템 수", example = "1234")
    private long totalElements;

    @Schema(description = "전체 페이지 수", example = "62")
    private int totalPages;

    @Schema(description = "정렬 파라미터", example = "[\"name,asc\",\"grade,desc\"]")
    private List<String> sort;

    public PagedList() {}

    public PagedList(List<T> items, int page, int size, long totalElements, int totalPages, List<String> sort) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.sort = sort;
    }

    public List<T> getItems() { return items; }
    public void setItems(List<T> items) { this.items = items; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    public List<String> getSort() { return sort; }
    public void setSort(List<String> sort) { this.sort = sort; }
}
