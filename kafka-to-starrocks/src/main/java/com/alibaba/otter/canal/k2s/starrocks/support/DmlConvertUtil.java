package com.alibaba.otter.canal.k2s.starrocks.support;


import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DML数据转换方法
 * @author mujingjing
 * @date 2024/2/5
 **/
public class DmlConvertUtil {

    /**
     * 数据库字段名称
     */
    private static final String DATABASE_COLUMN = "${DATABASE_NAME}";

    /**
     * 表字段名称
     */
    private static final String TABLE_COLUMN = "${TABLE_NAME}";

    /**
     * 同步时间字段名称
     */
    private static final String SYNC_TIME_COLUMN = "${SYNC_TIME}";

    /**
     * 数据库示例内容
     */
    private static final String DATABASE_INSTANCE_COLUMN = "${DATABASE_INSTANCE}";

    /**
     * 函数字段前缀
     */
    private static final String FUNC_COLUMN_PREFIX = "S_FUNC.";


    /**
     * 转换表字段
     * @param database 数据库
     * @param table 数据表
     * @param ts 同步时间
     * @param rowData 行数据
     * @param columnMappingMap 字段映射
     * @return 映射之后的值
     */
    public static Map<String, Object> transform(String instance, String database,String table, Long ts, Map<String, Object> rowData,  Map<String, String> columnMappingMap){
        // 进行转换
        Map<String, Object> newRowData = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : columnMappingMap.entrySet()) {
            String srcKey = entry.getKey();
            String destKey = entry.getValue();
            // 原始字段存在表中
            if(rowData.containsKey(srcKey)){
                newRowData.put(destKey, rowData.get(srcKey));
            }else if(DATABASE_INSTANCE_COLUMN.equalsIgnoreCase(srcKey)){
                newRowData.put(destKey, instance);
            }else if(DATABASE_COLUMN.equalsIgnoreCase(srcKey)){
                newRowData.put(destKey, database);
            }else if(TABLE_COLUMN.equalsIgnoreCase(srcKey)){
                newRowData.put(destKey, table);
            }else if(SYNC_TIME_COLUMN.equalsIgnoreCase(srcKey)){
                newRowData.put(destKey, ts);
            }else if(srcKey.startsWith(FUNC_COLUMN_PREFIX)){
                newRowData.put(destKey, srcKey.replace(FUNC_COLUMN_PREFIX, ""));
            }
        }
        return newRowData;
    }
}
