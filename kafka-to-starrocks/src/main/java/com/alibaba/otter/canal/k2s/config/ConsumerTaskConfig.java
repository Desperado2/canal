package com.alibaba.otter.canal.k2s.config;

import java.util.List;
import java.util.Map;

/**
 * 消费者任务配置
 *
 * @author mujingjing
 * @date 2024/2/4
 **/
public class ConsumerTaskConfig {


    /**
     * 唯一的任务id
     */
    private String taskId;

    /**
     * 消费组id
     */
    private String groupId;

    /**
     * kafka的地址
     */
    private String kafkaBootstrap;

    /**
     * 任务的topic列表
     */
    private List<String> topics;

    /**
     * topic的partition映射   可为空
     */
    private Map<String, List<Integer>> partitionMap;

    /**
     * 表映射的环境
     */
    private String mappingEnv;

    /**
     * jdbc的url
     */
    private String jdbcUrl;

    /**
     * 指定 StarRocks 集群中 FE 的 IP 地址。
     */
    private String feHost;

    /**
     * 指定 StarRocks 集群中 FE 的 HTTP 端口号。 默认端口号为 8030。
     */
    private Integer feHttpPort;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String passWord;

    /**
     * 消费策略
     */

    private String offsetReset;


    /**
     * 提交批次大小
     */

    private Integer commitBatch;


    /**
     * 提交超时时间
     */

    private Integer commitTimeout;

    private Boolean isRunning = false;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getKafkaBootstrap() {
        return kafkaBootstrap;
    }

    public void setKafkaBootstrap(String kafkaBootstrap) {
        this.kafkaBootstrap = kafkaBootstrap;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public Map<String, List<Integer>> getPartitionMap() {
        return partitionMap;
    }

    public void setPartitionMap(Map<String, List<Integer>> partitionMap) {
        this.partitionMap = partitionMap;
    }

    public String getMappingEnv() {
        return mappingEnv;
    }

    public void setMappingEnv(String mappingEnv) {
        this.mappingEnv = mappingEnv;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getFeHost() {
        return feHost;
    }

    public void setFeHost(String feHost) {
        this.feHost = feHost;
    }

    public Integer getFeHttpPort() {
        return feHttpPort;
    }

    public void setFeHttpPort(Integer feHttpPort) {
        this.feHttpPort = feHttpPort;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public Boolean getRunning() {
        return isRunning;
    }

    public void setRunning(Boolean running) {
        isRunning = running;
    }

    public String getOffsetReset() {
        return offsetReset;
    }

    public void setOffsetReset(String offsetReset) {
        this.offsetReset = offsetReset;
    }

    public Integer getCommitBatch() {
        return commitBatch;
    }

    public void setCommitBatch(Integer commitBatch) {
        this.commitBatch = commitBatch;
    }

    public Integer getCommitTimeout() {
        return commitTimeout;
    }

    public void setCommitTimeout(Integer commitTimeout) {
        this.commitTimeout = commitTimeout;
    }
}
