package com.alibaba.otter.canal.k2s.kafka.consumer;


import com.alibaba.otter.canal.k2s.config.ConsumerTaskConfig;
import com.alibaba.otter.canal.k2s.starrocks.config.MappingConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

/**
 * binlog消费类
 * @author mujingjing
 * @date 2024/2/4
 **/
public class BinlogConsumer implements Consumer<ConsumerRecord<String, String>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BinlogConsumer.class);

    private List<MappingConfig> mappingConfigList;

    private ConsumerTaskConfig consumerTaskConfig;

    public BinlogConsumer(ConsumerTaskConfig consumerTaskConfig, List<MappingConfig> mappingConfigList) {
        this.mappingConfigList = mappingConfigList;
        this.consumerTaskConfig = consumerTaskConfig;
    }

    @Override
    public void accept(ConsumerRecord<String, String> stringStringConsumerRecord) {
        String topic = stringStringConsumerRecord.key();
        String value = stringStringConsumerRecord.value();
        // 转换
    }
}
