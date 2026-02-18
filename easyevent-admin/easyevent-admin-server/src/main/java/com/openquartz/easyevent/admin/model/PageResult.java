package com.openquartz.easyevent.admin.model;

import java.util.Collections;
import java.util.List;
import lombok.Data;

@Data
public class PageResult<T> {
    private long total;
    private List<T> records;
    private int page;
    private int size;

    public PageResult(long total, List<T> records, int page, int size) {
        this.total = total;
        this.records = records != null ? records : Collections.emptyList();
        this.page = page;
        this.size = size;
    }
}
