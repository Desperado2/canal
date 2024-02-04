package com.alibaba.otter.canal.k2s.kafka.helper;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.k2s.kafka.container.ConsumerContainer;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
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

    @Value("${spring.kafka.num.partitions:4}")
    private Integer partitions;

    @Value("${spring.kafka.num.replica.fetchers:1}")
    private short fetchers;

    @Autowired
    private AdminClient adminClient;

    @Autowired
    private ConsumerContainer consumerContainer;

    /**
     * 创建topic
     *
     * @param name topic名称
     * @return 是否成功
     */
    public boolean createTopic(String name) {
        LOGGER.info("kafka创建topic：{}", name);
        NewTopic topic = new NewTopic(name, partitions, fetchers);
        CreateTopicsResult topics = adminClient.createTopics(Arrays.asList(topic));
        try {
            topics.all().get();
        } catch (Exception e) {
            LOGGER.error("kafka创建topic失败", e);
            return false;
        }
        return true;
    }

    /**
     * 查询所有Topic
     *
     * @return topic列表
     */
    public List<String> list() {
        ListTopicsResult listTopicsResult = adminClient.listTopics();
        Set<String> names = new HashSet<>();
        try {
            names = listTopicsResult.names().get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("kafka获取topic列表失败", e);
        }
        return new ArrayList<>(names);
    }

    /**
     * 删除topic
     *
     * @param name topic名称
     * @return 是否成功
     */
    public boolean deleteTopic(String name) {
        LOGGER.info("kafka删除topic：{}", name);
        DeleteTopicsResult deleteTopicsResult = adminClient.deleteTopics(Arrays.asList(name));
        try {
            deleteTopicsResult.all().get();
        } catch (Exception e) {
            LOGGER.error("kafka删除topic失败", e);
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
    public TopicDescription describeTopic(String name) {
        DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(Collections.singletonList(name));
        try {
            Map<String, TopicDescription> stringTopicDescriptionMap = describeTopicsResult.all().get();
            if (stringTopicDescriptionMap.get(name) != null) {
                return stringTopicDescriptionMap.get(name);
            }
        } catch (Exception e) {
            LOGGER.error(" 获取topic详情异常：", e);
        }
        return null;
    }

    /**
     * 获取topic详情
     *
     * @param topicList topic名称列表
     * @return 是否成功
     */
    public Map<String, TopicDescription> describeTopicList(List<String> topicList) {
        Map<String, TopicDescription> topicDescriptionMap = new HashMap<>();
        DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(topicList);
        try {
            Map<String, TopicDescription> stringTopicDescriptionMap = describeTopicsResult.all().get();
            for (String topic : topicList) {
                if(stringTopicDescriptionMap.containsKey(topic)){
                    topicDescriptionMap.put(topic, stringTopicDescriptionMap.get(topic));
                }
            }
        } catch (Exception e) {
            LOGGER.error(" 获取topic详情异常：", e);
        }
        return topicDescriptionMap;
    }

    /**
     * 添加消费者
     *
     * @param topic topic名称
     * @param consumer 消费者
     */
    public void addConsumer(String topic, List<Integer> partitionList, String groupId, Consumer<ConsumerRecord<String, String>> consumer) {
        LOGGER.info("将为topic：[{}] 创建消费者, 消费者groupId:[{}]", topic, groupId);
        consumerContainer.addConsumer(topic, partitionList, groupId, consumer);
    }

    /**
     * 删除消费者
     *
     * @param topic topic名称
     */
    public void deleteConsumer(String topic) {
        LOGGER.info("将删除topic：{} 消费者", topic);
        consumerContainer.deleteConsumer(topic);
    }

}
