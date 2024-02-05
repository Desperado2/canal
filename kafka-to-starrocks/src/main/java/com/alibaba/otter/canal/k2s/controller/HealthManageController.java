package com.alibaba.otter.canal.k2s.controller;


import com.alibaba.otter.canal.k2s.model.BaseModel;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 任务管理控制器
 * @author mujingjing
 * @date 2024/2/5
 **/
@RestController
@RequestMapping("/api/k-2-s/v1/health")
public class HealthManageController {

    @PostMapping(value = "/check")
    public BaseModel<String> health() {
        return BaseModel.getInstance("success");
    }
}
