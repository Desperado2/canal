package com.alibaba.otter.canal.k2s.kafka.thread;

import com.alibaba.otter.canal.k2s.kafka.model.ConsumerInfo;
import org.apache.kafka.clients.consumer.ConsumerGroupMetadata;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.tomcat.util.buf.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final Consumer<ConsumerRecords<String, String>> consumer;

    public KafkaConsumerThread(String taskId, KafkaConsumer<String, String> kafkaConsumer,Consumer<ConsumerRecords<String, String>> consumer){
        this.kafkaConsumer = kafkaConsumer;
        this.consumer = consumer;
        this.taskId = taskId;
    }

    @Override
    public void run() {
        try{
            while (true){
                if(isInterrupted()){
                    throw new InterruptedException();
                }
                // 拉取kafka消息
                ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(1000));
                consumer.accept(records);
                // 手动提交ack确认
                kafkaConsumer.commitSync();
            }
        }catch (InterruptedException e){
            Set<String> subscription = kafkaConsumer.subscription();
            LOGGER.info("taskId：{}，topic:{} 已停止监听，线程停止！", taskId,StringUtils.join(subscription, ','),e);
        }catch (Exception e){
            Set<String> subscription = kafkaConsumer.subscription();
            LOGGER.info("taskId：{}，topic:{} 消费者运行异常!", taskId, StringUtils.join(subscription, ','),e);
        }finally {
            //关闭消费者
            try {
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
