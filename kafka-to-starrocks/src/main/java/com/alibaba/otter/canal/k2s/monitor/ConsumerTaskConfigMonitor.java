package com.alibaba.otter.canal.k2s.monitor;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.k2s.cache.TaskRestartCache;
import com.alibaba.otter.canal.k2s.config.ConsumerTaskConfig;
import com.alibaba.otter.canal.k2s.config.KafkaToStarrocksConfig;
import com.alibaba.otter.canal.k2s.client.AdminManageClient;
import com.alibaba.otter.canal.k2s.kafka.consumer.BinlogConsumer;
import com.alibaba.otter.canal.k2s.kafka.container.ConsumerContainer;
import com.alibaba.otter.canal.k2s.kafka.helper.KafkaHelper;
import com.alibaba.otter.canal.k2s.starrocks.config.MappingConfig;
import com.alibaba.otter.canal.k2s.starrocks.service.StarrocksSyncService;
import com.alibaba.otter.canal.k2s.starrocks.support.StarrocksTemplate;
import com.alibaba.otter.canal.k2s.utils.PasswordUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.TopicPartitionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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

    /**
     * 默认提交批量大小 500条
     */
    private static final Integer DEFAULT_COMMIT_BATCH = 500;

    /**
     * 默认提交超时实际 1秒
     */
    private static final Integer DEFAULT_COMMIT_TIMEOUT = 1000;
    @Autowired
    private KafkaHelper kafkaHelper;

    @Autowired
    private TaskRestartCache taskRestartCache;

    @Autowired
    private AdminManageClient adminManageClient;

    @Autowired
    private KafkaToStarrocksConfig kafkaToStarrocksConfig;

    private final Map<String, ConsumerTaskConfig> consumerTaskConfigCache = new HashMap<>();

    private final Map<String, List<MappingConfig>> mappingConfigCache = new HashMap<>();

    public List<String> taskIdList(){
        return new ArrayList<>(consumerTaskConfigCache.keySet());
    }

    /**
     * 合并任务的配置信息
     * @param consumerTaskConfig 配置信息
     */
    public void mergeConfig(ConsumerTaskConfig consumerTaskConfig){
        // 校验topic
        if(consumerTaskConfig.getCommitBatch() == null){
            consumerTaskConfig.setCommitBatch(DEFAULT_COMMIT_BATCH);
        }
        if(consumerTaskConfig.getCommitTimeout() == null){
            consumerTaskConfig.setCommitTimeout(DEFAULT_COMMIT_TIMEOUT);
        }
        boolean checkTopic = checkTopic(consumerTaskConfig);
        if(!checkTopic){
            return;
        }
        // 获取配置
        String taskId = consumerTaskConfig.getTaskId();
        String mappingEnv = consumerTaskConfig.getMappingEnv();
        List<MappingConfig> mappingConfigCache = getMappingConfigCache(taskId, mappingEnv);
        if(mappingConfigCache == null || mappingConfigCache.isEmpty()){
            return;
        }
        // 1.查询是否已经存在
        boolean isExists = consumerTaskConfigCache.containsKey(taskId);
        if(isExists){
            // 存在  删除原始消费者任务，重启任务
            // 获取
            ConsumerTaskConfig consumerTaskConfig1 = consumerTaskConfigCache.get(taskId);
            for (String topic : consumerTaskConfig1.getTopics()) {
                MDC.put("taskId", taskId);
                LOGGER.info("taskId：{}，停止原来的topic信息", consumerTaskConfig1.getTaskId());
                MDC.remove("taskId");
                kafkaHelper.deleteConsumer(consumerTaskConfig1.getTaskId(), topic);
            }
        }
        // 新增新的消费者
        createConsumer(consumerTaskConfig);
    }

    /**
     * 合并任务的配置信息
     * @param taskId 配置信息
     */
    public void mergeConfigByTaskId(String taskId){
        // 校验topic
        MDC.put("taskId", taskId);
        LOGGER.info("taskId：{}，任务重启", taskId);
        MDC.remove("taskId");
        ConsumerTaskConfig consumerTaskConfig = consumerTaskConfigCache.get(taskId);
        mergeConfig(consumerTaskConfig);
    }

    /**
     * 删除任务的配置信息
     * @param consumerTaskConfig 配置信息
     */
    public void deleteConfig(ConsumerTaskConfig consumerTaskConfig){
        // 1.查询是否已经存在
        boolean isExists = consumerTaskConfigCache.containsKey(consumerTaskConfig.getTaskId());
        if(isExists){
            // 存在  删除原始消费者任务，重启任务
            ConsumerTaskConfig consumerTaskConfig1 = consumerTaskConfigCache.get(consumerTaskConfig.getTaskId());
            for (String topic : consumerTaskConfig1.getTopics()) {
                MDC.put("taskId", consumerTaskConfig1.getTaskId());
                LOGGER.info("taskId：{}，停止原来的topic信息", consumerTaskConfig1.getTaskId());
                MDC.remove("taskId");
                kafkaHelper.deleteConsumer(consumerTaskConfig1.getTaskId(),topic);
            }
        }
        consumerTaskConfigCache.remove(consumerTaskConfig.getTaskId());
    }

    /**
     * 创建消费者
     * @param consumerTaskConfig 配置
     */
    private void createConsumer(ConsumerTaskConfig consumerTaskConfig){
        // 获取map的映射信息
        List<String> topics = consumerTaskConfig.getTopics();
        Map<String, List<Integer>> partitionMap = consumerTaskConfig.getPartitionMap();
        String mappingEnv = consumerTaskConfig.getMappingEnv();
        List<MappingConfig> mappingConfigList = mappingConfigCache.get(mappingEnv);
        String taskId = consumerTaskConfig.getTaskId();
        for (String topic : topics) {
            List<Integer> partitionList = null;
            if(partitionMap != null){
                partitionList = partitionMap.get(topic);
            }
            StarrocksTemplate starrocksTemplate = new StarrocksTemplate(consumerTaskConfig);
            StarrocksSyncService starrocksSyncService = new StarrocksSyncService(starrocksTemplate);
            kafkaHelper.addConsumer(taskId,topic, partitionList, consumerTaskConfig,
                    new BinlogConsumer(taskId, starrocksSyncService, mappingConfigList), taskRestartCache);
            MDC.put("taskId", taskId);
            LOGGER.info("taskId：{}，创建消费者成功，topic:{}, groupId:{}", taskId, topic, consumerTaskConfig.getGroupId());
            MDC.remove("taskId");
        }
        consumerTaskConfigCache.put(taskId, consumerTaskConfig);
    }

    private boolean checkTopic(ConsumerTaskConfig consumerTaskConfig){
        // 不存在新增
        // 校验数据是否正确
        String taskId = consumerTaskConfig.getTaskId();
        List<String> topics = consumerTaskConfig.getTopics();
        Map<String, List<Integer>> partitionMap = consumerTaskConfig.getPartitionMap();
        String kafkaBootstrap = consumerTaskConfig.getKafkaBootstrap();
        // 1.校验topic是否存在   partition是否存在
        Map<String, TopicDescription> stringTopicDescriptionMap = kafkaHelper.describeTopicList(kafkaBootstrap,taskId, topics);
        for (String topic : topics) {
            // 判断是否存在
            if(!stringTopicDescriptionMap.containsKey(topic)){
                MDC.put("taskId", taskId);
                LOGGER.error("taskId：{}，topic[{}]不存在，请检查是否配置正确，任务启动失败", taskId, topic);
                MDC.remove("taskId");
                return false;
            }
            if(partitionMap != null){
                // 校验partition
                TopicDescription topicDescription = stringTopicDescriptionMap.get(topic);
                List<Integer> realPartitions = topicDescription.partitions().stream().map(TopicPartitionInfo::partition)
                        .collect(Collectors.toList());
                List<Integer> partitions = partitionMap.get(topic);
                if(partitions != null && !realPartitions.containsAll(partitions)){
                    // 有partition不存在，结束
                    // 查询不正确的partition
                    Object collect = CollectionUtils.subtract(partitions, realPartitions).stream().map(it -> it.toString())
                            .collect(Collectors.joining(","));
                    MDC.put("taskId", taskId);
                    LOGGER.error("taskId：{}，topic[{}]中的partition[{}]不存在，请检查是否配置正确，任务启动失败", taskId,topic, collect);
                    MDC.remove("taskId");
                    return false;
                }
            }
        }
        return true;
    }

    private synchronized List<MappingConfig> getMappingConfigCache(String taskId, String envCode){
        if(StringUtils.isBlank(envCode)){
            MDC.put("taskId", taskId);
            LOGGER.error("taskId：{}，表结构映射环境参数envCode不能为空， 任务运行失败", taskId);
            MDC.remove("taskId");
            return null;
        }
        // 查询
        JSONObject jsonObject = adminManageClient.queryMappingByEnv(kafkaToStarrocksConfig.getCanalAdminManagerUrl(),
                kafkaToStarrocksConfig.getCanalAdminUser(),
                PasswordUtil.encrypt(kafkaToStarrocksConfig.getCanalAdminPassword()),
                envCode);
        if(!jsonObject.containsKey("code") || !jsonObject.get("code").equals(20000)
                || !jsonObject.containsKey("data") || jsonObject.get("data") == null){
            MDC.put("taskId", taskId);
            LOGGER.error("taskId：{}，获取环境[{}]的表映射配置失败", taskId,envCode);
            MDC.remove("taskId");
            return null;
        }
        JSONArray data = jsonObject.getJSONArray("data");
        if(data.size() == 0){
            MDC.put("taskId", taskId);
            LOGGER.error("taskId：{}，环境[{}]的表映射配置不存在", taskId,envCode);
            MDC.remove("taskId");
            return null;
        }
        for (int i = 0; i < data.size(); i++) {
            String content = data.getJSONObject(i).getString("content");
            data.getJSONObject(i).put("content", JSONObject.parseObject(content));
        }
        List<MappingConfig> mappingConfigs = JSONArray.parseArray(JSONArray.toJSONString(data), MappingConfig.class);
        mappingConfigCache.put(envCode, mappingConfigs);
        return mappingConfigs;
    }
}
