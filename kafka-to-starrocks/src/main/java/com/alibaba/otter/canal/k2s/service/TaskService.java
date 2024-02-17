package com.alibaba.otter.canal.k2s.service;

import com.alibaba.otter.canal.k2s.config.ConsumerTaskConfig;

import java.util.List;

/**
 * 任务服务
 *
 * @author mujingjing
 * @date 2024/2/4
 **/
public interface TaskService {

    /**
     * 新增任务
     * @param consumerTaskConfig 任务配置
     */
    void add(ConsumerTaskConfig consumerTaskConfig);


    /**
     * 删除任务
     * @param consumerTaskConfig 任务配置
     */
    void delete(ConsumerTaskConfig consumerTaskConfig);


    /**
     * 更新任务
     * @param consumerTaskConfig 任务配置
     */
    void update(ConsumerTaskConfig consumerTaskConfig);


    /**
     * 停止任务
     * @param consumerTaskConfig 任务配置
     */
    void stop(ConsumerTaskConfig consumerTaskConfig);


    /**
     * 启动任务
     * @param consumerTaskConfig 任务配置
     */
    void start(ConsumerTaskConfig consumerTaskConfig);


    /**
     * 获取示例列表
     * @return 示例
     */
    List<String> getTaskList();
}
