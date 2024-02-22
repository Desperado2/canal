package com.alibaba.otter.canal.k2s.kafka.consumer;


import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.k2s.starrocks.config.MappingConfig;
import com.alibaba.otter.canal.k2s.starrocks.service.StarrocksSyncService;
import com.alibaba.otter.canal.k2s.starrocks.support.Dml;
import com.alibaba.otter.canal.k2s.starrocks.support.StarRocksBufferData;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

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
public class BinlogConsumer implements Consumer<Map<String, StarRocksBufferData>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BinlogConsumer.class);

    private final StarrocksSyncService starrocksSyncService;

    private final String taskId;

    public BinlogConsumer(String taskId,StarrocksSyncService starrocksSyncService) {
        this.taskId = taskId;
        this.starrocksSyncService = starrocksSyncService;
    }


    @Override
    public void accept(Map<String, StarRocksBufferData> bufferDataMap) {
        MDC.put("taskId", taskId);
        LOGGER.debug("taskId：{}， 写数据到starrocks", taskId);
        MDC.remove("taskId");
        starrocksSyncService.sync(taskId, bufferDataMap);
    }
}
