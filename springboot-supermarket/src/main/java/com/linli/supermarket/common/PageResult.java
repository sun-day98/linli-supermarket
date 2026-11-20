package com.linli.supermarket.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 统一分页响应：{@code {records,total,current,size}}。
 *
 * @param <T> 记录类型
 */
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<T> records = Collections.emptyList();
    private long total = 0L;
    private long current = 1L;
    private long size = 10L;

    public PageResult() {
    }

    public PageResult(List<T> records, long total, long current, long size) {
        this.records = records == null ? Collections.emptyList() : records;
        this.total = total;
        this.current = current;
        this.size = size;
    }

    /** 由 MyBatis-Plus 分页对象直接构造。 */
    public static <T> PageResult<T> of(IPage<T> page) {
        if (page == null) {
            return new PageResult<>();
        }
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /** 由 MyBatis-Plus 分页对象构造并转换记录类型。 */
    public static <S, T> PageResult<T> of(IPage<S> page, Function<? super S, ? extends T> converter) {
        if (page == null) {
            return new PageResult<>();
        }
        List<T> records = page.getRecords().stream().map(converter).collect(Collectors.toList());
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /** 由普通列表构造（内存分页结果）。 */
    public static <T> PageResult<T> of(List<T> records, long total, long current, long size) {
        return new PageResult<>(records, total, current, size);
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
