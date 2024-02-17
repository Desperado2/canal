package com.alibaba.otter.canal.k2s.controller;


import com.alibaba.otter.canal.k2s.model.BaseModel;
import com.alibaba.otter.canal.k2s.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 任务管理控制器
 * @author mujingjing
 * @date 2024/2/5
 **/
@RestController
@RequestMapping("/api/k-2-s/v1/log")
public class LogManageController {

    @Autowired
    private LogService logService;


    @GetMapping(value = "/node")
    public BaseModel<String> nodeLog() throws IOException {
        return BaseModel.getInstance(logService.readNodeLog());
    }


    @GetMapping(value = "/task/{taskId}")
    public BaseModel<String> taskLog(@PathVariable("taskId") String taskId) throws IOException {
        return BaseModel.getInstance(logService.readTaskLog(taskId));
    }
}
