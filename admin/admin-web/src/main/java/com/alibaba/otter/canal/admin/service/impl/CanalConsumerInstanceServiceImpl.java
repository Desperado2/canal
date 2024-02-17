package com.alibaba.otter.canal.admin.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.otter.canal.admin.client.TaskConsumerClient;
import com.alibaba.otter.canal.admin.client.TaskConsumerConfigClient;
import com.alibaba.otter.canal.admin.common.Threads;
import com.alibaba.otter.canal.admin.common.exception.ServiceException;
import com.alibaba.otter.canal.admin.model.BaseModel;
import com.alibaba.otter.canal.admin.model.ConsumerInstanceConfig;
import com.alibaba.otter.canal.admin.model.Pager;
import com.alibaba.otter.canal.admin.model.TaskNodeServer;
import com.alibaba.otter.canal.admin.service.CanalConsumerInstanceService;
import com.alibaba.otter.canal.protocol.SecurityUtil;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import io.ebean.Query;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 *
 * @author mujingjing
 * @date 2024/2/17
 **/
@Service
public class CanalConsumerInstanceServiceImpl implements CanalConsumerInstanceService {

    @Autowired
    private TaskConsumerClient taskConsumerClient;

    @Autowired
    private TaskConsumerConfigClient taskConsumerConfigClient;

    @Override
    public Pager<ConsumerInstanceConfig> findList(ConsumerInstanceConfig consumerInstanceConfig, Pager<ConsumerInstanceConfig> pager) {
        Query<ConsumerInstanceConfig> query = ConsumerInstanceConfig.find.query()
                .setDisableLazyLoading(true)
                .select("clusterId, taskNodeId, name, modifiedTime, taskId")
                .fetch("canalCluster", "name")
                .fetch("taskNodeServer", "name, ip, port");
        if (consumerInstanceConfig != null) {
            if (StringUtils.isNotEmpty(consumerInstanceConfig.getName())) {
                query.where().like("name", "%" + consumerInstanceConfig.getName() + "%");
            }
            if (StringUtils.isNotEmpty(consumerInstanceConfig.getClusterServerId())) {
                if (consumerInstanceConfig.getClusterServerId().startsWith("cluster:")) {
                    query.where()
                            .eq("clusterId", Long.parseLong(consumerInstanceConfig.getClusterServerId().substring(8)));
                } else if (consumerInstanceConfig.getClusterServerId().startsWith("server:")) {
                    query.where().eq("serverId", Long.parseLong(consumerInstanceConfig.getClusterServerId().substring(7)));
                }
            }
        }

        Query<ConsumerInstanceConfig> queryCnt = query.copy();
        int count = queryCnt.findCount();
        pager.setCount((long) count);

        query.setFirstRow(pager.getOffset().intValue()).setMaxRows(pager.getSize()).order().asc("id");
        List<ConsumerInstanceConfig> canalInstanceConfigs = query.findList();
        pager.setItems(canalInstanceConfigs);

        if (canalInstanceConfigs.isEmpty()) {
            return pager;
        }

        // check all canal instances running status
        List<Future<Void>> futures = new ArrayList<>(canalInstanceConfigs.size());
        for (ConsumerInstanceConfig canalInstanceConfig1 : canalInstanceConfigs) {
            futures.add(Threads.executorService.submit(() -> {
                List<TaskNodeServer> nodeServers;
                if (canalInstanceConfig1.getClusterId() != null) { // 集群模式
                    nodeServers = TaskNodeServer.find.query()
                            .where()
                            .eq("clusterId", canalInstanceConfig1.getClusterId())
                            .findList();
                } else if (canalInstanceConfig1.getTaskNodeId() != null) { // 单机模式
                    nodeServers = Collections.singletonList(canalInstanceConfig1.getTaskNodeServer());
                } else {
                    return null;
                }

                for (TaskNodeServer nodeServer : nodeServers) {
                    BaseModel<List<String>> listBaseModel = taskConsumerConfigClient.taskList(nodeServer.getIp() + ":" + nodeServer.getPort());
                    if (listBaseModel == null) {
                        continue;
                    }
                    for (String taskId : listBaseModel.getData()) {
                        if (taskId.equals(canalInstanceConfig1.getTaskId())) {
                            // 集群模式下server对象为空
                            if (canalInstanceConfig1.getTaskNodeServer() == null) {
                                canalInstanceConfig1.setTaskNodeServer(nodeServer);
                            }
                            canalInstanceConfig1.setRunningStatus("1");
                            break;
                        }
                    }
                }
                return null;
            }));
        }

        for (Future<Void> f : futures) {
            try {
                f.get(3, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException e) {
                // ignore
            } catch (TimeoutException e) {
                break;
            }
        }
        return pager;
    }

    @Override
    public void save(ConsumerInstanceConfig consumerInstanceConfig) {
        if (StringUtils.isEmpty(consumerInstanceConfig.getClusterServerId())) {
            throw new ServiceException("empty cluster or server id");
        }
        if (consumerInstanceConfig.getClusterServerId().startsWith("cluster:")) {
            Long clusterId = Long.parseLong(consumerInstanceConfig.getClusterServerId().substring(8));
            consumerInstanceConfig.setClusterId(clusterId);
        } else if (consumerInstanceConfig.getClusterServerId().startsWith("server:")) {
            Long serverId = Long.parseLong(consumerInstanceConfig.getClusterServerId().substring(7));
            consumerInstanceConfig.setTaskNodeId(serverId);
        }
        try {
            String contentMd5 = SecurityUtil.md5String(consumerInstanceConfig.getContent());
            consumerInstanceConfig.setContentMd5(contentMd5);
        } catch (NoSuchAlgorithmException e) {
            // ignore
        }
        consumerInstanceConfig.setTaskId("task_" + NanoIdUtils.randomNanoId());
        consumerInstanceConfig.insert();
    }

    @Override
    public ConsumerInstanceConfig detail(Long id) {
        ConsumerInstanceConfig consumerInstanceConfig = ConsumerInstanceConfig.find.byId(id);
        if (consumerInstanceConfig != null) {
            if (consumerInstanceConfig.getClusterId() != null) {
                consumerInstanceConfig.setClusterServerId("cluster:" + consumerInstanceConfig.getClusterId());
            } else if (consumerInstanceConfig.getTaskNodeId() != null) {
                consumerInstanceConfig.setClusterServerId("server:" + consumerInstanceConfig.getTaskNodeId());
            }
        }
        return consumerInstanceConfig;
    }

    @Override
    public void updateContent(ConsumerInstanceConfig consumerInstanceConfig) {
        if (StringUtils.isEmpty(consumerInstanceConfig.getClusterServerId())) {
            throw new ServiceException("empty cluster or server id");
        }
        if (consumerInstanceConfig.getClusterServerId().startsWith("cluster:")) {
            Long clusterId = Long.parseLong(consumerInstanceConfig.getClusterServerId().substring(8));
            consumerInstanceConfig.setClusterId(clusterId);
            consumerInstanceConfig.setTaskNodeId(null);
        } else if (consumerInstanceConfig.getClusterServerId().startsWith("server:")) {
            Long serverId = Long.parseLong(consumerInstanceConfig.getClusterServerId().substring(7));
            consumerInstanceConfig.setTaskNodeId(serverId);
            consumerInstanceConfig.setClusterId(null);
        }

        try {
            String contentMd5 = SecurityUtil.md5String(consumerInstanceConfig.getContent());
            consumerInstanceConfig.setContentMd5(contentMd5);
        } catch (NoSuchAlgorithmException e) {
            // ignore
        }
        consumerInstanceConfig.update("content", "contentMd5", "clusterId", "taskNodeId");
    }

    @Override
    public void delete(Long id) {
        ConsumerInstanceConfig consumerInstanceConfig = ConsumerInstanceConfig.find.byId(id);
        if (consumerInstanceConfig != null) {
            consumerInstanceConfig.delete();
        }
    }

    @Override
    public Map<String, String> remoteInstanceLog(Long id, Long nodeId) {
        Map<String, String> result = new HashMap<>();

        TaskNodeServer nodeServer = TaskNodeServer.find.byId(nodeId);
        if (nodeServer == null) {
            return result;
        }
        ConsumerInstanceConfig consumerInstanceConfig = ConsumerInstanceConfig.find.byId(id);
        if (consumerInstanceConfig == null) {
            return result;
        }

        String taskId = consumerInstanceConfig.getTaskId();
        BaseModel<String> baseModel = taskConsumerClient.taskLog(nodeServer.getIp() + ":" +
                nodeServer.getPort(), taskId);
        result.put("instance", consumerInstanceConfig.getName());
        result.put("log", baseModel.getData());
        return result;
    }


    @Override
    public boolean instanceOperation(Long id, String option) {
        ConsumerInstanceConfig consumerInstanceConfig = ConsumerInstanceConfig.find.byId(id);
        if (consumerInstanceConfig == null) {
            return false;
        }
        if ("stop".equals(option)) {
            TaskNodeServer nodeServer = TaskNodeServer.find.byId(consumerInstanceConfig.getTaskNodeId());
            assert nodeServer != null;
            BaseModel<String> baseModel = taskConsumerConfigClient
                    .deleteTask(nodeServer.getIp() + ":" + nodeServer.getPort(),
                            JSONObject.parseObject(consumerInstanceConfig.getContent()));
            consumerInstanceConfig.setStatus("0");
            consumerInstanceConfig.update("status");
        } else if ("start".equals(option)) {
            TaskNodeServer taskNodeServer = toRunTask(consumerInstanceConfig);
            if(taskNodeServer != null){
                consumerInstanceConfig.setTaskNodeId(taskNodeServer.getId());
                consumerInstanceConfig.setStatus("1");
                consumerInstanceConfig.update("status", "taskNodeId", "groupId");
            }
        } else {
            return false;
        }
        return true;
    }

    @Override
    public List<ConsumerInstanceConfig> findActiveInstanceByServerId(Long serverId) {
        TaskNodeServer taskNodeServer = TaskNodeServer.find.byId(serverId);
        if (taskNodeServer == null) {
            return null;
        }

        BaseModel<List<String>> listBaseModel = taskConsumerConfigClient.taskList(taskNodeServer.getIp() + ":" + taskNodeServer.getPort());
        if (listBaseModel == null) {
            return null;
        }
        List<String> data = listBaseModel.getData();
        // 单机模式和集群模式区分处理
        if (taskNodeServer.getClusterId() != null) { // 集群模式
            List<ConsumerInstanceConfig> list = ConsumerInstanceConfig.find.query()
                    .setDisableLazyLoading(true)
                    .select("clusterId, serverId, name, modifiedTime")
                    .where()
                    // 暂停的实例也显示 .eq("status", "1")
                    .in("taskId", data)
                    .findList();
            list.forEach(config -> config.setRunningStatus("1"));
            return list; // 集群模式直接返回当前运行的Instances
        } else { // 单机模式
            // 当前Server所配置的所有Instance
            List<ConsumerInstanceConfig> list = ConsumerInstanceConfig.find.query()
                    .setDisableLazyLoading(true)
                    .select("clusterId, serverId, name, modifiedTime")
                    .where()
                    // 暂停的实例也显示 .eq("status", "1")
                    .eq("serverId", serverId)
                    .findList();
            list.forEach(config -> {
                if (data.contains(config.getTaskId())) {
                    config.setRunningStatus("1");
                }
            });
            return list;
        }
    }


    private TaskNodeServer toRunTask(ConsumerInstanceConfig consumerInstanceConfig){
        if (consumerInstanceConfig == null) {
            return null;
        }
        // 查询所有节点
        List<TaskNodeServer> taskNodeServers = TaskNodeServer.find.all();
        // 判断节点的状态，找到可用的节点列表
        List<TaskNodeServer> availableNode = new ArrayList<>();
        for (TaskNodeServer taskNodeServer : taskNodeServers) {
            BaseModel<String> health = taskConsumerClient.health(taskNodeServer.getIp() + ":" + taskNodeServer.getPort());
            if("success".equals(health.getData())){
                availableNode.add(taskNodeServer);
            }
        }
        // 计算各个节点的任务数量
        List<Long> taskNodeIds = taskNodeServers.stream().map(TaskNodeServer::getId).collect(Collectors.toList());
        List<ConsumerInstanceConfig> list = ConsumerInstanceConfig.find.query()
                .setDisableLazyLoading(true)
                .select("taskNodeId, name")
                .where()
                .in("task_node_id", taskNodeIds)
                .findList();
        Map<Long, List<ConsumerInstanceConfig>> collect = list.stream().collect(Collectors.groupingBy(ConsumerInstanceConfig::getTaskNodeId));

        // 将其余的插入
        for (TaskNodeServer taskNodeServer : availableNode) {
            if(!collect.containsKey(taskNodeServer.getId())){
                collect.put(taskNodeServer.getId(), new ArrayList<>());
            }
        }
        // 按照任务数量进行排序
        LinkedHashMap<Long, List<ConsumerInstanceConfig>> linkedHashMap = collect.entrySet().stream()
                // 按照值的 List 大小进行升序排序
                .sorted(Comparator.comparingInt(entry -> entry.getValue().size()))
                // 将排序后的结果收集为一个新的 Map
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, LinkedHashMap::new));
        // 排序完毕，进行任务的分发
        for (Long taskNodeId : linkedHashMap.keySet()) {
            Optional<TaskNodeServer> any = availableNode.stream().filter(it -> it.getId().equals(taskNodeId)).findAny();
            if(any.isPresent()){
                TaskNodeServer taskNodeServer = any.get();
                // 根据状态获取新值
                BaseModel<String> baseModel = taskConsumerConfigClient.addTask(taskNodeServer.getIp() + ":" + taskNodeServer.getPort(),
                        transformConf(consumerInstanceConfig) );
                if("success".equals(baseModel.getData())){
                    return taskNodeServer;
                }
            }
        }
        return null;
    }

    private JSONObject transformConf(ConsumerInstanceConfig consumerInstanceConfig){
        String groupId = consumerInstanceConfig.getGroupId();
        if(StringUtils.isBlank(groupId)){
            // 生成新的groupId
            groupId = "k2s_" + NanoIdUtils.randomNanoId();
        }
        JSONObject jsonObject = JSONObject.parseObject(consumerInstanceConfig.getContent());
        // 插入值
        jsonObject.put("taskId", consumerInstanceConfig.getTaskId());
        // 获取消费策略
        String consumerPolicy = jsonObject.getString("consumerPolicy").toUpperCase();
        switch (consumerPolicy){
            case "L-C":
                jsonObject.put("offsetReset", "latest");
                jsonObject.put("groupId", groupId);
                break;
            case "E-B":
                groupId = "k2s_" + NanoIdUtils.randomNanoId();
                jsonObject.put("offsetReset", "earliest");
                jsonObject.put("groupId", groupId);
                break;
            case "L-B":
                groupId = "k2s_" + NanoIdUtils.randomNanoId();
                jsonObject.put("offsetReset", "latest");
                jsonObject.put("groupId", groupId);
                break;
            default:
                jsonObject.put("offsetReset", "earliest");
                jsonObject.put("groupId", groupId);
                break;
        }
        consumerInstanceConfig.setGroupId(groupId);
        return jsonObject;
    }
}
