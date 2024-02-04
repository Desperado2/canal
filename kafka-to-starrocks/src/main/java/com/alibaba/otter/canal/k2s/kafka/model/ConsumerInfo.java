package com.alibaba.otter.canal.k2s.kafka.model;


import java.util.List;

/**
 * 消费者信息
 * @author mujingjing
 * @date 2024/2/4
 **/
public class ConsumerInfo {
    private String topic;
    private String groupId;
    private List<Integer> partitionList;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public List<Integer> getPartitionList() {
        return partitionList;
    }

    public void setPartitionList(List<Integer> partitionList) {
        this.partitionList = partitionList;
    }
}
