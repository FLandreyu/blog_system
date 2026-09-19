package com.sunyongjie.blog.common;

import lombok.Data;

/**
 * 分页查询基类。
 *
 * <p>page / size 由前端传入，可能是 null、0 或负数，也可能有人手动传 size=100000 拖垮数据库，
 * 所以统一在这里做「归一化」：非法值回落到默认值，size 最大 50。
 * Mapper 里用 {@code #{offset}} / {@code #{limit}} 取值，即下面的 getOffset() / getLimit()。
 */
@Data
public class PageQuery {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;

    private Integer page = DEFAULT_PAGE;
    private Integer size = DEFAULT_SIZE;

    /** 供 MyBatis 取 LIMIT 偏移量 */
    public int getOffset() {
        return (normalizedPage() - 1) * normalizedSize();
    }

    /** 供 MyBatis 取 LIMIT 行数 */
    public int getLimit() {
        return normalizedSize();
    }

    public int normalizedPage() {
        return (page == null || page < 1) ? DEFAULT_PAGE : page;
    }

    public int normalizedSize() {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }
}
