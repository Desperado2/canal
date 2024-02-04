package com.alibaba.otter.canal.k2s.monitor;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.k2s.config.ConsumerTaskConfig;
import com.alibaba.otter.canal.k2s.config.KafkaToStarrocksConfig;
import com.alibaba.otter.canal.k2s.kafka.client.AdminManageClient;
import com.alibaba.otter.canal.k2s.kafka.consumer.BinlogConsumer;
import com.alibaba.otter.canal.k2s.kafka.container.ConsumerContainer;
import com.alibaba.otter.canal.k2s.kafka.helper.KafkaHelper;
import com.alibaba.otter.canal.k2s.starrocks.config.MappingConfig;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.TopicPartitionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 消费者服务监控类
 * @author mujingjing
 * @date 2024/2/4
 **/
@Component
public class ConsumerTaskConfigMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerContainer.class);

    @Autowired
    private KafkaHelper kafkaHelper;

    @Autowired
    private AdminManageClient adminManageClient;

    @Autowired
    private KafkaToStarrocksConfig kafkaToStarrocksConfig;

    private final Map<String, ConsumerTaskConfig> consumerTaskConfigCache = new HashMap<>();

    private final Map<String, List<MappingConfig>> mappingConfigCache = new HashMap<>();

    /**
     * 合并任务的配置信息
     * @param consumerTaskConfig 配置信息
     */
    public void mergeConfig(ConsumerTaskConfig consumerTaskConfig){
        // 1.查询是否已经存在
        boolean isExists = consumerTaskConfigCache.containsKey(consumerTaskConfig.getTaskId());
        // 校验topic
        boolean checkTopic = checkTopic(consumerTaskConfig);
        if(!checkTopic){
            return;
        }
        // 获取配置
        String mappingEnv = consumerTaskConfig.getMappingEnv();
        List<MappingConfig> mappingConfigCache = getMappingConfigCache(mappingEnv);
        if(mappingConfigCache == null || mappingConfigCache.isEmpty()){
            return;
        }
        // 检查通过 创建消费
        createConsumer(consumerTaskConfig);
    }


    private boolean createConsumer(ConsumerTaskConfig consumerTaskConfig){
        // 获取map的映射信息
        List<String> topics = consumerTaskConfig.getTopics();
        Map<String, List<Integer>> partitionMap = consumerTaskConfig.getPartitionMap();
        String mappingEnv = consumerTaskConfig.getMappingEnv();
        List<MappingConfig> mappingConfigList = mappingConfigCache.get(mappingEnv);
        for (String topic : topics) {
            List<Integer> partitionList = partitionMap.get(topic);
            kafkaHelper.addConsumer(topic, partitionList, "", new BinlogConsumer(consumerTaskConfig,mappingConfigList));
        }
        consumerTaskConfigCache.put(consumerTaskConfig.getTaskId(), consumerTaskConfig);
        return true;
    }

    private boolean checkTopic(ConsumerTaskConfig consumerTaskConfig){
        // 不存在新增
        // 校验数据是否正确
        List<String> topics = consumerTaskConfig.getTopics();
        Map<String, List<Integer>> partitionMap = consumerTaskConfig.getPartitionMap();
        // 1.校验topic是否存在   partition是否存在
        Map<String, TopicDescription> stringTopicDescriptionMap = kafkaHelper.describeTopicList(topics);
        for (String topic : topics) {
            List<Integer> partitions = partitionMap.get(topic);
            // 判断是否存在
            if(stringTopicDescriptionMap.containsKey(topic)){
                LOGGER.error("topic[{}]不存在，请检查是否配置正确，任务启动失败", topic);
                return false;
            }
            // 校验partition
            TopicDescription topicDescription = stringTopicDescriptionMap.get(topic);
            List<Integer> realPartitions = topicDescription.partitions().stream().map(TopicPartitionInfo::partition)
                    .collect(Collectors.toList());
            if(!realPartitions.containsAll(partitions)){
                // 有partition不存在，结束
                // 查询不正确的partition
                Object collect = CollectionUtils.subtract(partitions, realPartitions).stream()
                        .collect(Collectors.joining(","));
                LOGGER.error("topic[{}]中的partition[{}]不存在，请检查是否配置正确，任务启动失败", topic, collect);
                return false;
            }
        }
        return true;
    }

    private synchronized List<MappingConfig> getMappingConfigCache(String envCode){
        if(StringUtils.isBlank(envCode)){
            LOGGER.error("表结构映射环境参数envCode不能为空， 任务运行失败");
            return null;
        }
        if(mappingConfigCache.containsKey(envCode)){
            return mappingConfigCache.get(envCode);
        }
        // 查询
        JSONObject jsonObject = adminManageClient.queryMappingByEnv(kafkaToStarrocksConfig.getCanalAdminManagerUrl(),
                kafkaToStarrocksConfig.getCanalAdminUser(),
                kafkaToStarrocksConfig.getCanalAdminPassword(),
                envCode);
        if(!jsonObject.containsKey("code") || jsonObject.get("code").equals(20000)
                || !jsonObject.containsKey("data") || jsonObject.get("data") == null){
            LOGGER.error("获取环境[{}]的表映射配置失败", envCode);
            return null;
        }
        JSONArray data = jsonObject.getJSONArray("data");
        if(data.size() == 0){
            LOGGER.error("环境[{}]的表映射配置不存在", envCode);
            return null;
        }
        List<MappingConfig> mappingConfigs = JSONArray.parseArray(JSONArray.toJSONString(data), MappingConfig.class);
        mappingConfigCache.put(envCode, mappingConfigs);
        return mappingConfigs;
    }
}
