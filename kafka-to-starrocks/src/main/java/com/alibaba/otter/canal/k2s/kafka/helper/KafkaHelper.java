package com.alibaba.otter.canal.k2s.kafka.helper;

import com.alibaba.otter.canal.k2s.config.ConsumerTaskConfig;
import com.alibaba.otter.canal.k2s.kafka.container.ConsumerContainer;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * kafka 助手类
 *
 * @author mujingjing
 * @date 2024/2/4
 **/
@Component
public class KafkaHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaHelper.class);

    @Value("${spring.kafka.num.partitions:3}")
    private Integer partitions;

    @Value("${spring.kafka.num.replica.fetchers:1}")
    private short fetchers;

    @Autowired
    private KafkaPropertiesHelper kafkaPropertiesHelper;

    @Autowired
    private ConsumerContainer consumerContainer;

    /**
     * 创建topic
     *
     * @param name topic名称
     * @return 是否成功
     */
    public boolean createTopic(String bootstrap, String taskId, String name) {
        LOGGER.info("taskId:{}，kafka创建topic：{}", taskId, name);
        NewTopic topic = new NewTopic(name, partitions, fetchers);
        CreateTopicsResult topics = kafkaPropertiesHelper.getAdminClient(bootstrap).createTopics(Collections.singletonList(topic));
        try {
            topics.all().get();
        } catch (Exception e) {
            LOGGER.error("taskId:{}，kafka创建topic失败",taskId, e);
            return false;
        }
        return true;
    }

    /**
     * 查询所有Topic
     *
     * @return topic列表
     */
    public List<String> list(String bootstrap, String taskId) {
        ListTopicsResult listTopicsResult = kafkaPropertiesHelper.getAdminClient(bootstrap).listTopics();
        Set<String> names = new HashSet<>();
        try {
            names = listTopicsResult.names().get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("taskId:{}，kafka获取topic列表失败", taskId, e);
        }
        return new ArrayList<>(names);
    }

    /**
     * 删除topic
     *
     * @param name topic名称
     * @return 是否成功
     */
    public boolean deleteTopic(String bootstrap, String taskId, String name) {
        LOGGER.info("taskId:{}，kafka删除topic：{}", taskId,name);
        DeleteTopicsResult deleteTopicsResult = kafkaPropertiesHelper.getAdminClient(bootstrap).deleteTopics(Collections.singletonList(name));
        try {
            deleteTopicsResult.all().get();
        } catch (Exception e) {
            LOGGER.error("taskId:{}，kafka删除topic失败",taskId,e);
            return false;
        }
        return true;
    }

    /**
     * 获取topic详情
     *
     * @param name topic名称
     * @return 是否成功
     */
    public TopicDescription describeTopic(String bootstrap, String taskId, String name) {
        DescribeTopicsResult describeTopicsResult = kafkaPropertiesHelper.getAdminClient(bootstrap).describeTopics(Collections.singletonList(name));
        try {
            Map<String, TopicDescription> stringTopicDescriptionMap = describeTopicsResult.all().get();
            if (stringTopicDescriptionMap.get(name) != null) {
                return stringTopicDescriptionMap.get(name);
            }
        } catch (Exception e) {
            LOGGER.error("taskId:{}，获取topic详情异常：",taskId, e);
        }
        return null;
    }

    /**
     * 获取topic详情
     *
     * @param topicList topic名称列表
     * @return 是否成功
     */
    public Map<String, TopicDescription> describeTopicList(String bootstrap, String taskId, List<String> topicList) {
        Map<String, TopicDescription> topicDescriptionMap = new HashMap<>();
        DescribeTopicsResult describeTopicsResult = kafkaPropertiesHelper.getAdminClient(bootstrap).describeTopics(topicList);
        try {
            Map<String, TopicDescription> stringTopicDescriptionMap = describeTopicsResult.all().get();
            for (String topic : topicList) {
                if(stringTopicDescriptionMap.containsKey(topic)){
                    topicDescriptionMap.put(topic, stringTopicDescriptionMap.get(topic));
                }
            }
        } catch (Exception e) {
            LOGGER.error("taskId:{}，获取topic详情异常：", taskId, e);
        }
        return topicDescriptionMap;
    }

    /**
     * 添加消费者
     *
     * @param topic topic名称
     * @param consumer 消费者
     */
    public void addConsumer(String taskId, String topic, List<Integer> partitionList, ConsumerTaskConfig consumerTaskConfig, Consumer<ConsumerRecords<String, String>> consumer) {
        LOGGER.info("taskId:{}，将为topic：[{}] 创建消费者, 消费者groupId:[{}]",taskId, topic, consumerTaskConfig.getGroupId());
        consumerContainer.addConsumer(taskId, topic, partitionList, consumerTaskConfig, consumer);
    }

    /**
     * 删除消费者
     *
     * @param topic topic名称
     */
    public void deleteConsumer(String taskId,String topic) {
        LOGGER.info("taskId:{}，将删除topic：{} 的消费者", taskId,topic);
        consumerContainer.deleteConsumer(taskId, topic);
    }

}
