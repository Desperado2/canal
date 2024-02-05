package com.alibaba.otter.canal.k2s.controller;


import com.alibaba.otter.canal.k2s.config.ConsumerTaskConfig;
import com.alibaba.otter.canal.k2s.model.BaseModel;
import com.alibaba.otter.canal.k2s.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 任务管理控制器
 * @author mujingjing
 * @date 2024/2/5
 **/
@RestController
@RequestMapping("/api/k-2-s/v1/task")
public class TaskManageController {

    @Autowired
    private TaskService taskService;

    @PostMapping(value = "/add-task")
    public BaseModel<String> addTask(@RequestBody ConsumerTaskConfig consumerTaskConfig) {
        taskService.add(consumerTaskConfig);
        return BaseModel.getInstance("success");
    }

    @PostMapping(value = "/update-task")
    public BaseModel<String> updateTask(@RequestBody ConsumerTaskConfig consumerTaskConfig) {
        taskService.update(consumerTaskConfig);
        return BaseModel.getInstance("success");
    }

    @PostMapping(value = "/delete-task")
    public BaseModel<String> deleteTask(@RequestBody ConsumerTaskConfig consumerTaskConfig) {
        taskService.delete(consumerTaskConfig);
        return BaseModel.getInstance("success");
    }
}
