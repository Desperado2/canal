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
import java.util.Date;

/**
 * 节点信息实体类
 *
 * @author rewerma 2019-07-13 下午05:12:16
 * @version 1.0.0
 */
@Entity
@Table(name = "canal_task_node_server")
public class TaskNodeServer extends Model {

    public static final TaskNodeServerFinder find = new TaskNodeServerFinder();

    public static class TaskNodeServerFinder extends Finder<Long, TaskNodeServer> {
        public TaskNodeServerFinder(){
            super(TaskNodeServer.class);
        }
    }

    @Id
    private Long         id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cluster_id", updatable = false, insertable = false)
    private CanalCluster canalCluster;
    @Column(name = "cluster_id")
    private Long         clusterId;
    private String       name;
    private String       ip;
    private Integer      port;
    private String       status;
    @WhenModified
    private Date         modifiedTime;

    @Override
    public void init() {
        status = "-1";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CanalCluster getCanalCluster() {
        return canalCluster;
    }

    public void setCanalCluster(CanalCluster canalCluster) {
        this.canalCluster = canalCluster;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
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
}
