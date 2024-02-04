package com.alibaba.otter.canal.k2s.starrocks.service;

import com.alibaba.otter.canal.k2s.starrocks.config.MappingConfig;
import com.alibaba.otter.canal.k2s.starrocks.support.Dml;
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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * StarRocks同步操作业务
 */
@Service
public class StarrocksSyncService {
    private static final Logger logger  = LoggerFactory.getLogger(StarrocksSyncService.class);

    private final StarrocksTemplate starrocksTemplate;

    private final Map<String, MappingConfig> mappingConfigCache = new HashMap<>();

    public StarrocksSyncService(StarrocksTemplate starrocksTemplate) {
        this.starrocksTemplate = starrocksTemplate;
    }

    public void sync(Map<String, Map<String, MappingConfig>> mappingConfig, List<Dml> dmls) {
        Map<String, StarRocksBufferData> batchData = new HashMap<>();
        for (Dml dml : dmls) {
            if(dml.getIsDdl() != null && dml.getIsDdl() && StringUtils.isNotEmpty(dml.getSql())) {
                logger.info("This is DDL event data, it will be ignored");
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
                StarRocksBufferData rocksBufferData = batchData.computeIfAbsent(key, k -> new StarRocksBufferData());
                rocksBufferData.setDbName(configMap.getDstDatabase());
                rocksBufferData.setMappingConfig(configMap);
                List<byte[]> bufferData = rocksBufferData.getData();
                List<byte[]> bytesData = genBatchData(dml, configMap);
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

    public List<byte[]> genBatchData(Dml dml, MappingConfig config) {
        List<byte[]> batchData = new ArrayList<>() ;
        List<Map<String, Object>> data = dml.getData();
        if (data == null || data.size() == 0) {
            return null;
        }

        List<String> eventType = config.getMappingData().getNeedType().stream().map(String::toUpperCase).collect(Collectors.toList());
        String type = dml.getType();
        for (Map<String, Object> rowData : data) {
            String jsonData;
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
}
