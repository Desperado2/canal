package com.alibaba.otter.canal.k2s.service.impl;

import com.alibaba.otter.canal.k2s.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * 日志接口
 *
 * @author mujingjing
 * @date 2024/2/5
 **/
@Service
public class LogServiceImpl implements LogService {


    @Autowired
    private ResourceLoader resourceLoader;

    @Override
    public String readNodeLog()  {
        try {
            // 加载日志文件资源
            Resource resource = resourceLoader.getResource("classpath:logs/k2s_meta.log");
            // 读取日志文件内容
            String logContent;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                logContent = reader.lines().limit(100).collect(Collectors.joining("\n"));
            }
            return logContent;
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to read log file.";
        }
    }

    @Override
    public String readTaskLog(String taskId) throws IOException {
        try {
            // 加载日志文件资源
            Resource resource = resourceLoader.getResource(String.format("classpath:logs/%s/%s.log",taskId, taskId));
            // 读取日志文件内容
            String logContent;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                logContent = reader.lines().limit(100).collect(Collectors.joining("\n"));
            }
            return logContent;
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to read log file.";
        }
    }
}
