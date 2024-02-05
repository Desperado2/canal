package com.alibaba.otter.canal.k2s.starrocks.service;

import com.alibaba.otter.canal.k2s.starrocks.config.MappingConfig;
import com.alibaba.otter.canal.k2s.starrocks.support.Dml;
import com.alibaba.otter.canal.k2s.starrocks.support.DmlConvertUtil;
import com.alibaba.otter.canal.k2s.starrocks.support.StarRocksBufferData;
import com.alibaba.otter.canal.k2s.starrocks.support.StarrocksTemplate;
import com.alibaba.otter.canal.k2s.utils.PatternUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.oro.text.regex.Perl5Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * StarRocks同步操作业务
 */
public class StarrocksSyncService {
    private static final Logger logger  = LoggerFactory.getLogger(StarrocksSyncService.class);

    private final StarrocksTemplate starrocksTemplate;

    private final Map<String, MappingConfig> mappingConfigCache = new HashMap<>();
    private final Map<String, Map<String, String>> columnMappingCache = new HashMap<>();

    public StarrocksSyncService(StarrocksTemplate starrocksTemplate) {
        this.starrocksTemplate = starrocksTemplate;
    }


    /**
     * 同步
     * @param mappingConfig 字段配置
     * @param dmls 语句
     */
    public void sync(Map<String, Map<String, MappingConfig>> mappingConfig, List<Dml> dmls) {
        Map<String, StarRocksBufferData> batchData = new HashMap<>();
        for (Dml dml : dmls) {
            if(dml.getIsDdl() != null && dml.getIsDdl() && StringUtils.isNotEmpty(dml.getSql())) {
                logger.debug("This is DDL event data, it will be ignored");
            } else {
                // DML
                String database = dml.getDatabase();
                String table = dml.getTable();
                String key = database + "-" + table;
                // 查询对应的数据
                MappingConfig configMap = mappingConfigCache.computeIfAbsent(key, k -> getMappingConfig(mappingConfig, database, table));
                if (configMap == null) {
                  continue;
                }
                Map<String, String> columnMappingMap = getColumnMap(key, configMap);
                if (columnMappingMap == null) {
                    continue;
                }
                StarRocksBufferData rocksBufferData = batchData.computeIfAbsent(key, k -> new StarRocksBufferData());
                rocksBufferData.setDbName(configMap.getDstDatabase());
                rocksBufferData.setMappingConfig(configMap);
                List<byte[]> bufferData = rocksBufferData.getData();
                List<byte[]> bytesData = genBatchData(dml, configMap, columnMappingMap);
                if (CollectionUtils.isEmpty(bufferData)) {
                    bufferData = new ArrayList<>();
                }
                bufferData.addAll(bytesData);
                rocksBufferData.setData(bufferData);
            }
        }
        sync(batchData);
    }


    /**
     * 批量同步
     * @param batchData 批量数据
     */
    private void sync(Map<String, StarRocksBufferData> batchData) {
        for (Map.Entry<String, StarRocksBufferData> bufferDataEntry : batchData.entrySet()) {
            starrocksTemplate.sink(bufferDataEntry.getValue());
        }
    }


    /**
     * 生成批量数据
     * @param dml dml参数
     * @param config 配置
     * @param columnMappingMap 字段映射
     * @return 转换之后的值
     */
    public List<byte[]> genBatchData(Dml dml, MappingConfig config, Map<String, String> columnMappingMap) {
        List<byte[]> batchData = new ArrayList<>() ;
        List<Map<String, Object>> data = dml.getData();
        if (data == null || data.size() == 0) {
            return null;
        }
        List<String> eventType = config.getMappingData().getNeedType().stream().map(String::toUpperCase).collect(Collectors.toList());
        String type = dml.getType();
        for (Map<String, Object> rowData : data) {
            String jsonData;
            rowData = DmlConvertUtil.transform(dml.getDatabase(), dml.getTable(), dml.getTs(), rowData, columnMappingMap);
            if ("INSERT".equalsIgnoreCase(type) && eventType.contains("INSERT")) {
                jsonData = starrocksTemplate.upsert(rowData);
            } else if ("UPDATE".equalsIgnoreCase(type) && eventType.contains("UPDATE")) {
                jsonData = starrocksTemplate.upsert(rowData);
            } else if ("DELETE".equalsIgnoreCase(type) && eventType.contains("DELETE")) {
                // 查询删除的策略
                String deleteStrategy = config.getMappingData().getDeleteStrategy();
                if ("UPDATE".equalsIgnoreCase(deleteStrategy)){
                    String deleteUpdateField = config.getMappingData().getDeleteUpdateField();
                    String deleteUpdateValue = config.getMappingData().getDeleteUpdateValue();
                    rowData.put(deleteUpdateField, deleteUpdateValue);
                    jsonData = starrocksTemplate.upsert(rowData);
                }else {
                    jsonData = starrocksTemplate.delete(rowData);
                }
            } else {
                logger.warn("Unsupport other event data");
                continue;
            }
            batchData.add(jsonData.getBytes(StandardCharsets.UTF_8));
        }
        return batchData;
    }


    /**
     * 匹配获取表名
     * @param mappingConfig 配置
     * @param database 数据库
     * @param table 数据表
     * @return 配置
     */
    private MappingConfig getMappingConfig(Map<String, Map<String, MappingConfig>> mappingConfig, String database, String table){
        // 进行perl正则匹配
        Perl5Matcher tableMatcher = new Perl5Matcher();
        for (String key : mappingConfig.keySet()) {
            if (tableMatcher.matches(database, PatternUtils.getPattern(key))) {
                Map<String, MappingConfig> stringMappingConfigMap = mappingConfig.get(key);
                for (String tableName : stringMappingConfigMap.keySet()) {
                    if (tableMatcher.matches(table, PatternUtils.getPattern(tableName))) {
                        return stringMappingConfigMap.get(tableName);
                    }
                }
            }
        }
       return null;
    }


    /**
     * 获取字段映射
     * @param key key
     * @param configMap 配置
     * @return 映射map
     */
    private Map<String, String> getColumnMap(String key, MappingConfig configMap){
        if(columnMappingCache.containsKey(key)){
            return columnMappingCache.get(key);
        }
        // 进行转换
        List<MappingConfig.MappingData.ColumnMapping> columnMappingList = configMap.getMappingData().getColumnMappingList();
        Map<String, String> columnMappingMap = new HashMap<>(columnMappingList.size());
        for (MappingConfig.MappingData.ColumnMapping columnMapping : columnMappingList) {
            columnMappingMap.put(columnMapping.getSrcField(), columnMapping.getDstField());
        }
        columnMappingCache.put(key, columnMappingMap);
        return columnMappingMap;
    }

}
