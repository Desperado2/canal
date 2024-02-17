package com.alibaba.otter.canal.admin.controller;

import com.alibaba.otter.canal.admin.common.TemplateConfigLoader;
import com.alibaba.otter.canal.admin.model.BaseModel;
import com.alibaba.otter.canal.admin.model.CanalInstanceConfig;
import com.alibaba.otter.canal.admin.model.ConsumerInstanceConfig;
import com.alibaba.otter.canal.admin.model.Pager;
import com.alibaba.otter.canal.admin.service.CanalConsumerInstanceService;
import com.alibaba.otter.canal.admin.service.CanalInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Canal Instance配置管理控制层
 *
 * @author rewerma 2019-07-13 下午05:12:16
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/{env}/canal/consumer")
public class ConsumerInstanceController {

    @Autowired
    CanalConsumerInstanceService canalConsumerInstanceService;

    /**
     * 实例配置列表
     *
     * @param consumerInstanceConfig 查询对象
     * @param env 环境变量
     * @return 实例列表
     */
    @GetMapping(value = "/instances")
    public BaseModel<Pager<ConsumerInstanceConfig>> list(ConsumerInstanceConfig consumerInstanceConfig,
                                                      Pager<ConsumerInstanceConfig> pager, @PathVariable String env) {
        return BaseModel.getInstance(canalConsumerInstanceService.findList(consumerInstanceConfig, pager));
    }

    /**
     * 保存实例配置
     *
     * @param consumerInstanceConfig 实例配置对象
     * @param env 环境变量
     * @return 是否成功
     */
    @PostMapping(value = "/instance")
    public BaseModel<String> save(@RequestBody ConsumerInstanceConfig consumerInstanceConfig, @PathVariable String env) {
        canalConsumerInstanceService.save(consumerInstanceConfig);
        return BaseModel.getInstance("success");
    }

    /**
     * 实例详情信息
     *
     * @param id 实例配置id
     * @param env 环境变量
     * @return 实例信息
     */
    @GetMapping(value = "/instance")
    public BaseModel<ConsumerInstanceConfig> config(@PathVariable Long id, @PathVariable String env) {
        return BaseModel.getInstance(canalConsumerInstanceService.detail(id));
    }

    /**
     * 实例详情信息
     *
     * @param id 实例配置id
     * @param env 环境变量
     * @return 实例信息
     */
    @GetMapping(value = "/instance/{id}")
    public BaseModel<ConsumerInstanceConfig> detail(@PathVariable Long id, @PathVariable String env) {
        return BaseModel.getInstance(canalConsumerInstanceService.detail(id));
    }

    /**
     * 修改实例配置
     *
     * @param consumerInstanceConfig 实例配置信息
     * @param env 环境变量
     * @return 是否成功
     */
    @PutMapping(value = "/instance")
    public BaseModel<String> update(@RequestBody ConsumerInstanceConfig consumerInstanceConfig, @PathVariable String env) {
        canalConsumerInstanceService.updateContent(consumerInstanceConfig);
        return BaseModel.getInstance("success");
    }

    /**
     * 删除实例配置
     *
     * @param id 实例配置id
     * @param env 环境变量
     * @return 是否成功
     */
    @DeleteMapping(value = "/instance/{id}")
    public BaseModel<String> delete(@PathVariable Long id, @PathVariable String env) {
        canalConsumerInstanceService.delete(id);
        return BaseModel.getInstance("success");
    }


    /**
     * 通过操作instance状态启动/停止远程instance
     *
     * @param id 实例配置id
     * @param option 操作类型: start/stop
     * @param env 环境变量
     * @return 是否成功
     */
    @PutMapping(value = "/instance/status/{id}")
    public BaseModel<Boolean> instanceStart(@PathVariable Long id, @RequestParam String option, @PathVariable String env) {
        return BaseModel.getInstance(canalConsumerInstanceService.instanceOperation(id, option));
    }

    /**
     * 获取远程实例运行日志
     *
     * @param id 实例配置id
     * @param nodeId 节点id
     * @param env 环境变量
     * @return 实例日志信息
     */
    @GetMapping(value = "/instance/log/{id}/{nodeId}")
    public BaseModel<Map<String, String>> instanceLog(@PathVariable Long id, @PathVariable Long nodeId,
                                                      @PathVariable String env) {
        return BaseModel.getInstance(canalConsumerInstanceService.remoteInstanceLog(id, nodeId));
    }

    /**
     * 通过Server id获取所有活动的Instance
     *
     * @param serverId 节点id
     * @param env 环境变量
     * @return 实例列表
     */
    @GetMapping(value = "/active/instances/{serverId}")
    public BaseModel<List<ConsumerInstanceConfig>> activeInstances(@PathVariable Long serverId, @PathVariable String env) {
        return BaseModel.getInstance(canalConsumerInstanceService.findActiveInstanceByServerId(serverId));
    }

     /**
    * 获取环境对应的模板配置信息
    *
    * @param env 环境名称
    * @return 返回环境对应的模板配置信息
    */
    @GetMapping(value = "/instance/template")
    public BaseModel<String> template(@PathVariable String env) {
       // 加载并返回指定环境的消费实例配置
       return BaseModel.getInstance(TemplateConfigLoader.loadConsumerInstanceConfig());
    }

}
