package com.sunyongjie.blog.common;

import lombok.Data;

import java.util.List;

/**
 * 统一分页响应：{@code { total, page, size, list }}。
 */
@Data
public class PageResult<T> {

    private long total;
    private int page;
    private int size;
    private List<T> list;

    public PageResult() {
    }

    public PageResult(long total, int page, int size, List<T> list) {
        this.total = total;
        this.page = page;
        this.size = size;
        this.list = list;
    }

    public static <T> PageResult<T> of(PageQuery query, long total, List<T> list) {
        return new PageResult<>(total, query.normalizedPage(), query.normalizedSize(), list);
    }
}
