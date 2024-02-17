package com.alibaba.otter.canal.k2s.service;

import java.io.IOException;

/**
 * 日志接口
 *
 * @author mujingjing
 * @date 2024/2/5
 **/
public interface LogService {


    /**
     * 获取节点日志
     * @return 日志
     */
    String readNodeLog() throws IOException;


    /**
     * 获取任务日志
     * @param taskId 任务id
     * @return 任务日志
     */
    String readTaskLog(String taskId) throws IOException;
}
