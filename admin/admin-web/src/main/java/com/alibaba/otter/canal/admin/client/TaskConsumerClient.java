package com.alibaba.otter.canal.admin.client;

import com.alibaba.otter.canal.admin.model.BaseModel;
import com.dtflys.forest.annotation.Get;
import com.dtflys.forest.annotation.Var;

import java.util.List;

/**
 * 消费者客户端地址
 *
 * @author mujingjing
 * @date 2024/2/5
 **/
public interface TaskConsumerClient {

    /**
     * 获取状态
     * @param basePath 路径
     * @return 状态
     */
    @Get(value = "http://{basePath}/api/k-2-s/v1/health/check", headers = {
            "Accept-Charset: utf-8",
            "Content-Type: application/json"
    })
    BaseModel<String> health(@Var("basePath") String basePath);


    /**
     * 获取节点日志
     * @param basePath 路径
     * @return 状态
     */
    @Get(value = "http://{basePath}/api/k-2-s/v1/log/node", headers = {
            "Accept-Charset: utf-8",
            "Content-Type: application/json"
    })
    BaseModel<String> nodeLog(@Var("basePath") String basePath);


    /**
     * 获取节点日志
     * @param basePath 路径
     * @return 状态
     */
    @Get(value = "http://{basePath}/api/k-2-s/v1/log//task/{taskId}", headers = {
            "Accept-Charset: utf-8",
            "Content-Type: application/json"
    })
    BaseModel<String> taskLog(@Var("basePath") String basePath,@Var("taskId") String taskId);



}
