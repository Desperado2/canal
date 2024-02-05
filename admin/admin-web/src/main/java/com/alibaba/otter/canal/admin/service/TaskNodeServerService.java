package com.alibaba.otter.canal.admin.service;

import com.alibaba.otter.canal.admin.model.Pager;
import com.alibaba.otter.canal.admin.model.TaskNodeServer;

import java.util.List;

public interface TaskNodeServerService {

    boolean autoRegister(String ip, Integer port, String cluster, String name);

    void save(TaskNodeServer taskNodeServer);

    TaskNodeServer detail(Long id);

    void update(TaskNodeServer taskNodeServer);

    void delete(Long id);

    List<TaskNodeServer> findAll(TaskNodeServer taskNodeServer);

    Pager<TaskNodeServer> findList(TaskNodeServer taskNodeServer, Pager<TaskNodeServer> pager);

    int remoteTaskNodeStatus(String ip, Integer port);

    String remoteTaskNodeLog(Long id);
}
