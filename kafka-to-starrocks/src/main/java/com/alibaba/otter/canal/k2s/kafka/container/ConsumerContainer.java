package com.alibaba.otter.canal.k2s.kafka.container;


import com.alibaba.otter.canal.k2s.cache.TaskRestartCache;
import com.alibaba.otter.canal.k2s.config.ConsumerTaskConfig;
import com.alibaba.otter.canal.k2s.kafka.helper.KafkaPropertiesHelper;
import com.alibaba.otter.canal.k2s.kafka.model.ConsumerInfo;
import com.alibaba.otter.canal.k2s.kafka.thread.KafkaConsumerThread;
import com.alibaba.otter.canal.k2s.starrocks.config.MappingConfig;
import com.alibaba.otter.canal.k2s.starrocks.service.StarrocksSyncService;
import com.alibaba.otter.canal.k2s.starrocks.support.StarRocksBufferData;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 消费者管理
 * @author mujingjing
 * @date 2024/2/4
 **/
@Component
public class ConsumerContainer {

    @Autowired
    KafkaPropertiesHelper kafkaPropertiesHelper;

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerContainer.class);

    /**
     * 使用map对象，存储kafka对应信息<TOPIC, THREAD>
     */
    private final Map<String, KafkaConsumerThread> kafkaConsumerThreadMap = new HashMap<>();


    /**
     * 创建消费者
     * @param topic topic
     * @param partitionList partition列表
     * @param consumer 消费者
     */
    public synchronized void addConsumer(String taskId, String topic, List<Integer> partitionList,
                                         ConsumerTaskConfig consumerTaskConfig,
                                         Consumer<Map<String, StarRocksBufferData>> consumer,
                                         TaskRestartCache taskRestartCache,
                                         StarrocksSyncService starrocksSyncService,
                                         List<MappingConfig> mappingConfigList){
        if(kafkaConsumerThreadMap.containsKey(topic)){
            MDC.put("taskId", taskId);
            LOGGER.warn("重复创建消费者：{}", topic);
            MDC.remove("taskId");
        }
        // 获取参数
        if(!StringUtils.hasText(consumerTaskConfig.getGroupId())){
            // 生成group_id
            consumerTaskConfig.setGroupId( "binlog_" + topic + "_" + System.currentTimeMillis());
        }
        Properties kafkaProp = kafkaPropertiesHelper.getKafkaProp(consumerTaskConfig.getKafkaBootstrap(),
                consumerTaskConfig.getGroupId(),
                consumerTaskConfig.getOffsetReset());
        KafkaConsumer<String, String> stringStringKafkaConsumer = new KafkaConsumer<>(kafkaProp);
        stringStringKafkaConsumer.subscribe(Collections.singletonList(topic));
        // 如果partition不为空，添加
        if(partitionList != null &&  !partitionList.isEmpty()){
            List<TopicPartition> partitions = new ArrayList<>();
            for (Integer partition : partitionList) {
                partitions.add(new TopicPartition(topic, partition));
            }
            stringStringKafkaConsumer.assign(partitions);
        }
        // 创建消费者线程去消费
        KafkaConsumerThread kafkaConsumerThread = new KafkaConsumerThread(taskId,
                consumerTaskConfig.getCommitBatch(),
                consumerTaskConfig.getCommitTimeout(),
                stringStringKafkaConsumer, consumer, taskRestartCache,
                starrocksSyncService, mappingConfigList);
        kafkaConsumerThread.start();
        kafkaConsumerThreadMap.put(topic, kafkaConsumerThread);
        MDC.put("taskId", taskId);
        LOGGER.info("taskId:{}，topic[{}]创建消费者成功",taskId, topic);
        MDC.remove("taskId");
    }

    public synchronized void deleteConsumer(String taskId, String topic){
        KafkaConsumerThread kafkaConsumerThread = kafkaConsumerThreadMap.get(topic);
        if (kafkaConsumerThread == null) {
            MDC.put("taskId", taskId);
            LOGGER.warn("taskId:{}，topic[{}]的消费者已经被删除",taskId,topic);
            MDC.remove("taskId");
            return;
        }
        //打断消费者线程
        kafkaConsumerThread.interrupt();
        kafkaConsumerThreadMap.remove(topic);
        MDC.put("taskId", taskId);
        LOGGER.info("taskId:{}，topic[{}]的消费者删除成功",taskId,topic);
        MDC.remove("taskId");
    }


    public Set<String> getTopicList(){
        return kafkaConsumerThreadMap.keySet();
    }

    public List<List<ConsumerInfo>> getSubscribeInfo(){
        List<List<ConsumerInfo>> consumerInfoList = new ArrayList<>();
        for (String key : kafkaConsumerThreadMap.keySet()) {
            KafkaConsumerThread kafkaConsumerThread = kafkaConsumerThreadMap.get(key);
            List<ConsumerInfo> consumerInfo = kafkaConsumerThread.getConsumerInfo();
            consumerInfoList.add(consumerInfo);
        }
        return consumerInfoList;
    }
}
