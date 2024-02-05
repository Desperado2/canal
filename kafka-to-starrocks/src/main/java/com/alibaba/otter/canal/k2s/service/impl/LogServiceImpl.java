package com.alibaba.otter.canal.k2s.service.impl;

import com.alibaba.otter.canal.k2s.service.LogService;
import org.springframework.stereotype.Service;

/**
 * 日志接口
 *
 * @author mujingjing
 * @date 2024/2/5
 **/
@Service
public class LogServiceImpl implements LogService {


    @Override
    public String readNodeLog() {
        return null;
    }

    @Override
    public String readTaskLog(String taskId) {
        return null;
    }
}
