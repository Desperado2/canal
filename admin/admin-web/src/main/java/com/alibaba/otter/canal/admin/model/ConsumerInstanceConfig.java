package com.alibaba.otter.canal.admin.model;

import io.ebean.Finder;
import io.ebean.annotation.WhenModified;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;

/**
 * Canal实例配置信息实体类
 *
 * @author rewerma 2019-07-13 下午05:12:16
 * @version 1.0.0
 */
@Entity
@Table(name = "canal_consumer_instance_config")
public class ConsumerInstanceConfig extends Model {

    public static final ConsumerInstanceConfigFinder find = new ConsumerInstanceConfigFinder();

    public static class ConsumerInstanceConfigFinder extends Finder<Long, ConsumerInstanceConfig> {

        /**
         * Construct using the default EbeanServer.
         */
        public ConsumerInstanceConfigFinder(){
            super(ConsumerInstanceConfig.class);
        }

    }

    @Id
    private Long         id;
    @Column(name = "cluster_id")
    private Long         clusterId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cluster_id", updatable = false, insertable = false)
    private CanalCluster canalCluster;
    @Column(name = "task_node_id")
    private Long         taskNodeId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_node_id", updatable = false, insertable = false)
    private TaskNodeServer   taskNodeServer;
    private String       taskId;
    private String       name;
    private String       content;
    private String       groupId;
    private String       contentMd5;
    private String       status;             // 1: 正常 0: 停止
    @WhenModified
    private Date         modifiedTime;
    @Transient
    private String       clusterServerId;
    @Transient
    private String       runningStatus = "0"; // 1: 运行中 0: 停止

    @Override
    public void init() {
        status = "1";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public CanalCluster getCanalCluster() {
        return canalCluster;
    }

    public void setCanalCluster(CanalCluster canalCluster) {
        this.canalCluster = canalCluster;
    }

    public Long getTaskNodeId() {
        return taskNodeId;
    }

    public void setTaskNodeId(Long taskNodeId) {
        this.taskNodeId = taskNodeId;
    }

    public TaskNodeServer getTaskNodeServer() {
        return taskNodeServer;
    }

    public void setTaskNodeServer(TaskNodeServer taskNodeServer) {
        this.taskNodeServer = taskNodeServer;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getContentMd5() {
        return contentMd5;
    }

    public void setContentMd5(String contentMd5) {
        this.contentMd5 = contentMd5;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public String getClusterServerId() {
        return clusterServerId;
    }

    public void setClusterServerId(String clusterServerId) {
        this.clusterServerId = clusterServerId;
    }

    public String getRunningStatus() {
        return runningStatus;
    }

    public void setRunningStatus(String runningStatus) {
        this.runningStatus = runningStatus;
    }
}
