package com.alibaba.otter.canal.admin.service.impl;

import com.alibaba.otter.canal.admin.client.TaskConsumerClient;
import com.alibaba.otter.canal.admin.common.Threads;
import com.alibaba.otter.canal.admin.common.exception.ServiceException;
import com.alibaba.otter.canal.admin.connector.AdminConnector;
import com.alibaba.otter.canal.admin.connector.SimpleAdminConnectors;
import com.alibaba.otter.canal.admin.model.BaseModel;
import com.alibaba.otter.canal.admin.model.CanalCluster;
import com.alibaba.otter.canal.admin.model.NodeServer;
import com.alibaba.otter.canal.admin.model.Pager;
import com.alibaba.otter.canal.admin.model.TaskNodeServer;
import com.alibaba.otter.canal.admin.service.TaskNodeServerService;
import io.ebean.Query;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class TaskNodeServerServiceImpl implements TaskNodeServerService {

    @Autowired
    private TaskConsumerClient taskConsumerClient;

    @Override
    public boolean autoRegister(String ip, Integer port, String cluster, String name) {
        TaskNodeServer server = TaskNodeServer.find.query().where().eq("ip", ip).eq("port", port).findOne();
        if (server == null) {
            server = new TaskNodeServer();
            server.setName(Optional.ofNullable(name).orElse(ip));
            server.setIp(ip);
            server.setPort(port);
            if (StringUtils.isNotEmpty(cluster)) {
                CanalCluster clusterConfig = CanalCluster.find.query().where().eq("name", cluster).findOne();
                if (clusterConfig == null) {
                    throw new ServiceException("auto cluster : " + cluster + " is not found.");
                }
                server.setClusterId(clusterConfig.getId());
            }
            save(server);
        }
        return true;
    }

    @Override
    public void save(TaskNodeServer taskNodeServer) {
        int cnt = TaskNodeServer.find.query()
                .where()
                .eq("ip", taskNodeServer.getIp())
                .eq("port", taskNodeServer.getPort())
                .findCount();
        if (cnt > 0) {
            throw new ServiceException("任务节点信息已存在");
        }
        taskNodeServer.save();
    }

    @Override
    public TaskNodeServer detail(Long id) {
        return TaskNodeServer.find.byId(id);
    }

    @Override
    public void update(TaskNodeServer taskNodeServer) {
        int cnt = TaskNodeServer.find.query()
                .where()
                .eq("ip", taskNodeServer.getIp())
                .eq("port", taskNodeServer.getPort())
                .ne("id", taskNodeServer.getId())
                .findCount();
        if (cnt > 0) {
            throw new ServiceException("任务节点信息已存在");
        }
        taskNodeServer.update("name", "ip", "port", "clusterId");
    }

    @Override
    public void delete(Long id) {
        TaskNodeServer taskNodeServer = TaskNodeServer.find.byId(id);
        if (taskNodeServer != null) {
            taskNodeServer.delete();
        }
    }

    @Override
    public List<TaskNodeServer> findAll(TaskNodeServer taskNodeServer) {
        return TaskNodeServer.find.all();
    }

    @Override
    public Pager<TaskNodeServer> findList(TaskNodeServer taskNodeServer, Pager<TaskNodeServer> pager) {
        Query<TaskNodeServer> query = TaskNodeServer.find.query();
        int count = query.findCount();
        pager.setCount((long)count);
        List<TaskNodeServer> taskNodeServerList = query.order().asc("id")
                .setFirstRow(pager.getOffset().intValue())
                .setMaxRows(pager.getSize())
                .findList();

        List<Future<Boolean>> futures = new ArrayList<>(taskNodeServerList.size());
        // get all nodes status
        for (TaskNodeServer ns : taskNodeServerList) {
            futures.add(Threads.executorService.submit(() -> {
                BaseModel<String> baseModel = taskConsumerClient.health(ns.getIp() + ":" + ns.getPort());
                ns.setStatus(baseModel.getCode().equals(20000) ? "1" : "0");
                return !(baseModel.getCode().equals(20000));
            }));
        }
        for (Future<Boolean> f : futures) {
            try {
                f.get(3, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException e) {
                // ignore
            } catch (TimeoutException e) {
                break;
            }
        }
        pager.setItems(taskNodeServerList);
        return pager;
    }

    @Override
    public int remoteTaskNodeStatus(String ip, Integer port) {
        BaseModel<String> health = taskConsumerClient.health(ip + ":" + port);
        return "success".equals(health.getData()) ? 1 : 0;
    }

    @Override
    public String remoteTaskNodeLog(Long id) {
        TaskNodeServer taskNodeServer = TaskNodeServer.find.byId(id);
        if (taskNodeServer == null) {
            throw new ServiceException("任务节点信息不存在");
        }
        BaseModel<String> baseModel = taskConsumerClient.nodeLog(taskNodeServer.getIp() + ":" + taskNodeServer.getPort());
        return baseModel.getMessage();
    }
}
