package com.alibaba.otter.canal.k2s.kafka.consumer;


import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.k2s.starrocks.config.MappingConfig;
import com.alibaba.otter.canal.k2s.starrocks.service.StarrocksSyncService;
import com.alibaba.otter.canal.k2s.starrocks.support.Dml;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * binlog消费类
 * @author mujingjing
 * @date 2024/2/4
 **/
public class BinlogConsumer implements Consumer<List<ConsumerRecord<String, String>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BinlogConsumer.class);

    private final StarrocksSyncService starrocksSyncService;

    private final Map<String, Map<String, MappingConfig>> mappingConfig;

    private final String taskId;

    public BinlogConsumer(String taskId,StarrocksSyncService starrocksSyncService, List<MappingConfig> mappingConfigList) {
        this.taskId = taskId;
        this.starrocksSyncService = starrocksSyncService;
        this.mappingConfig = transform(mappingConfigList);
    }

    private Map<String, Map<String, MappingConfig>> transform(List<MappingConfig> mappingConfigList){
        Map<String, Map<String, MappingConfig>> mapMap = new HashMap<>();
        for (MappingConfig mappingConfig : mappingConfigList) {
            String srcDatabase = mappingConfig.getSrcDatabase();
            String srcTable = mappingConfig.getSrcTable();
            if(mapMap.containsKey(srcDatabase)){
                mapMap.get(srcDatabase).put(srcTable, mappingConfig);
            }else{
                Map<String, MappingConfig> tableMap = new HashMap<>();
                tableMap.put(srcTable, mappingConfig);
                mapMap.put(srcDatabase, tableMap);
            }
        }
        return mapMap;
    }

    @Override
    public void accept(List<ConsumerRecord<String, String>> consumerRecords) {
        List<Dml> dmlList = new ArrayList<>();
        for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
            String value = consumerRecord.value();
            dmlList.add(JSONObject.toJavaObject(JSONObject.parseObject(value), Dml.class));
        }
        // 转换
        starrocksSyncService.sync(taskId,mappingConfig, dmlList);
    }
}
