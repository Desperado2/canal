package com.alibaba.otter.canal.k2s.service.impl;

import com.alibaba.otter.canal.k2s.config.ConsumerTaskConfig;
import com.alibaba.otter.canal.k2s.monitor.ConsumerTaskConfigMonitor;
import com.alibaba.otter.canal.k2s.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 任务服务
 * @author mujingjing
 * @date 2024/2/4
 **/
@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private ConsumerTaskConfigMonitor consumerTaskConfigMonitor;

    @Override
    public void add(ConsumerTaskConfig consumerTaskConfig) {
        consumerTaskConfigMonitor.mergeConfig(consumerTaskConfig);
    }

    @Override
    public void delete(ConsumerTaskConfig consumerTaskConfig) {
        consumerTaskConfigMonitor.deleteConfig(consumerTaskConfig);
    }

    @Override
    public void update(ConsumerTaskConfig consumerTaskConfig) {
        consumerTaskConfigMonitor.mergeConfig(consumerTaskConfig);
    }

    @Override
    public void stop(ConsumerTaskConfig consumerTaskConfig) {
        consumerTaskConfigMonitor.deleteConfig(consumerTaskConfig);
    }

    @Override
    public void start(ConsumerTaskConfig consumerTaskConfig) {
        consumerTaskConfigMonitor.mergeConfig(consumerTaskConfig);
    }

    @Override
    public List<String> getTaskList() {
        return consumerTaskConfigMonitor.taskIdList();
    }
}
