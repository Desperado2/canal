package com.alibaba.otter.canal.k2s.starrocks.sink;


import org.apache.flink.types.RowKind;

/**
 * 操作类型
 * @author mujingjing
 * @date 2024/2/4
 **/
public enum StarRocksSinkOP {
    /**
     * 新增  修改
     */
    UPSERT,

    /**
     * 删除
     */
    DELETE;
    public static final String COLUMN_KEY = "__op";

    static StarRocksSinkOP parse(RowKind kind) {
        if (RowKind.INSERT.equals(kind) || RowKind.UPDATE_AFTER.equals(kind)) {
            return UPSERT;
        }
        if (RowKind.DELETE.equals(kind) || RowKind.UPDATE_BEFORE.equals(kind)) {
            return DELETE;
        }
        throw new RuntimeException("Unsupported row kind.");
    }
}
