package com.alibaba.otter.canal.admin.client;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.otter.canal.admin.model.BaseModel;
import com.dtflys.forest.annotation.Body;
import com.dtflys.forest.annotation.Get;
import com.dtflys.forest.annotation.Post;
import com.dtflys.forest.annotation.Var;

import java.util.List;

/**
 * 消费者客户端地址
 *
 * @author mujingjing
 * @date 2024/2/5
 **/
public interface TaskConsumerConfigClient {

    /**
     * 获取节点日志
     * @param basePath 路径
     * @return 状态
     */
    @Get(value = "http://{basePath}/api/k-2-s/v1/task/task-list", headers = {
            "Accept-Charset: utf-8",
            "Content-Type: application/json"
    })
    BaseModel<List<String>> taskList(@Var("basePath") String basePath);

    /**
     * 获取节点日志
     * @param basePath 路径
     * @return 状态
     */
    @Post(value = "http://{basePath}/api/k-2-s/v1/task/add-task", headers = {
            "Accept-Charset: utf-8",
            "Content-Type: application/json"
    })
    BaseModel<String> addTask(@Var("basePath") String basePath, @Body JSONObject consumerTaskConfig);

    /**
     * 获取节点日志
     * @param basePath 路径
     * @return 状态
     */
    @Post(value = "http://{basePath}/api/k-2-s/v1/task/update-task", headers = {
            "Accept-Charset: utf-8",
            "Content-Type: application/json"
    })
    BaseModel<String> updateTask(@Var("basePath") String basePath, @Body JSONObject consumerTaskConfig);



    /**
     * 获取节点日志
     * @param basePath 路径
     * @return 状态
     */
    @Post(value = "http://{basePath}/api/k-2-s/v1/task/delete-task", headers = {
            "Accept-Charset: utf-8",
            "Content-Type: application/json"
    })
    BaseModel<String> deleteTask(@Var("basePath") String basePath, @Body JSONObject consumerTaskConfig);
}
