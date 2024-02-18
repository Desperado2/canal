package com.alibaba.otter.canal.k2s.service.impl;

import com.alibaba.otter.canal.k2s.service.LogService;
import com.alibaba.otter.canal.k2s.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${logging.file.path:/}")
    private String logFilePath;

    @Override
    public String readNodeLog()  {
        return FileUtils.readFileFromOffset(logFilePath + "k2s_meta.log", 100, "UTF-8");
    }

    @Override
    public String readTaskLog(String taskId) throws IOException {
        return FileUtils.readFileFromOffset(logFilePath + taskId + "/" + taskId + ".log", 100, "UTF-8");
    }
}
