package com.alibaba.otter.canal.k2s.kafka.thread;

import com.alibaba.otter.canal.k2s.cache.TaskRestartCache;
import com.alibaba.otter.canal.k2s.kafka.model.ConsumerInfo;
import com.alibaba.otter.canal.k2s.starrocks.config.MappingConfig;
import com.alibaba.otter.canal.k2s.starrocks.service.StarrocksSyncService;
import com.alibaba.otter.canal.k2s.starrocks.support.StarRocksBufferData;
import org.apache.kafka.clients.consumer.ConsumerGroupMetadata;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.tomcat.util.buf.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * kafka消费线程
 * @author mujingjing
 * @date 2024/2/4
 **/
public class KafkaConsumerThread extends Thread{

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerThread.class);

    private final String taskId;
    private final KafkaConsumer<String, String> kafkaConsumer;
    private final TaskRestartCache taskRestartCache;
    private final StarrocksSyncService starrocksSyncService;
    private final List<MappingConfig> mappingConfigList;
    private final ReadWriteLock readWriteLock;
    /**
     * 缓存消费的消息
     */
    List<ConsumerRecord<String, String>> buffer;
    /**
     * 设置每达到多少条消息时提交偏移量
     */
    int commitBatch;
    /**
     * 设置超时时间
     */
    int commitTimeout;
    /**
     * 创建 ScheduledExecutorService 实例
     */
    ScheduledExecutorService scheduler;

    public KafkaConsumerThread(String taskId,
                               Integer commitBatch,
                               Integer commitTimeout,
                               KafkaConsumer<String, String> kafkaConsumer,
                               TaskRestartCache taskRestartCache,
                               StarrocksSyncService starrocksSyncService,
                               List<MappingConfig> mappingConfigList){
        this.kafkaConsumer = kafkaConsumer;
        this.taskId = taskId;
        this.taskRestartCache = taskRestartCache;
        this.commitBatch = commitBatch;
        this.commitTimeout = commitTimeout;
        this.mappingConfigList = mappingConfigList;
        buffer = new CopyOnWriteArrayList<>();
        scheduler = Executors.newScheduledThreadPool(1);
        this.starrocksSyncService = starrocksSyncService;
        readWriteLock = new ReentrantReadWriteLock();
    }

    @Override
    public void run() {
        try{
            // 创建缓存消费的消息处理器
            KafkaMessageHandler messageHandler = new KafkaMessageHandler(commitBatch, commitTimeout, records -> {
                // 提交数据写kafka
                LOGGER.debug("开始提交偏移量");
                final Lock lock = readWriteLock.writeLock();
                lock.lock();
                try {
                    LOGGER.debug("提交偏移量");
                    this.kafkaConsumer.commitAsync();
                } finally {
                    lock.unlock();
                }
            },starrocksSyncService, mappingConfigList,taskId);

            while (true){
                if(isInterrupted()){
                    throw new InterruptedException();
                }
                // 拉取kafka消息
                final Lock lock = readWriteLock.readLock();
                ConsumerRecords<String, String> records = null;
                lock.lock();
                try {
                    records = kafkaConsumer.poll(Duration.ofMillis(500));
                } finally {
                    lock.unlock();
                }
                for (ConsumerRecord<String, String> record : records) {
                    // 处理消息
                    messageHandler.addRecord(record);
                }
            }
        }catch (InterruptedException e){
            Set<String> subscription = kafkaConsumer.subscription();
            MDC.put("taskId", taskId);
            LOGGER.info("taskId：{}，topic:{} 已停止监听，线程停止！", taskId,StringUtils.join(subscription, ','),e);
            MDC.remove("taskId");
        }catch (Exception e){
            Set<String> subscription = kafkaConsumer.subscription();
            MDC.put("taskId", taskId);
            LOGGER.info("taskId：{}，topic:{} 消费者运行异常!", taskId, StringUtils.join(subscription, ','),e);
            MDC.remove("taskId");
            if(e.getMessage() != null && e.getMessage().contains("because of too many versions")){
                // 1分钟之后重新加入消费者
                MDC.put("taskId", taskId);
                taskRestartCache.set(taskId, taskId, 60L);
                LOGGER.info("taskId：{}，topic:{} 消费者加入重启队列，1分钟后重启!", taskId, StringUtils.join(subscription, ','));
                MDC.remove("taskId");
            }
        }finally {
            try {
                scheduler.shutdown(); // 在关闭消费者之前，关闭定时任务
                kafkaConsumer.close();
            } catch (Exception ex) {
            }
        }
    }

    public List<ConsumerInfo> getConsumerInfo(){
        List<ConsumerInfo> consumerInfoList = new ArrayList<>();
        ConsumerGroupMetadata consumerGroupMetadata = kafkaConsumer.groupMetadata();
        String groupId = consumerGroupMetadata.groupId();
        Map<String, List<PartitionInfo>> topics = kafkaConsumer.listTopics();
        for (String topic : topics.keySet()) {
            List<PartitionInfo> partitionInfos = topics.get(topic);
            List<Integer> partitionList = partitionInfos.stream().map(PartitionInfo::partition).collect(Collectors.toList());
            ConsumerInfo consumerInfo = new ConsumerInfo();
            consumerInfo.setGroupId(groupId);
            consumerInfo.setTopic(topic);
            consumerInfo.setPartitionList(partitionList);
            consumerInfoList.add(consumerInfo);
        }
        return consumerInfoList;
    }

}
