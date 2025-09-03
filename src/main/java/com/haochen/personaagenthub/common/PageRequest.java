package com.haochen.personaagenthub.common;

import lombok.Data;

/**
 * common page request
 * 通用分页请求类
 */
@Data
public class PageRequest {

    /**
     * Current page number
     * 当前页号
     */
    private int current = 1;

    /**
     * Current page size
     * 页面大小
     */
    private int pageSize = 10;

    /**
     * sort field
     * 排序字段
     */
    private String sortField;

    /**
     * Sort order (default descending)
     * 排序顺序（默认降序）
     */
    private String sortOrder = "descend";
}


