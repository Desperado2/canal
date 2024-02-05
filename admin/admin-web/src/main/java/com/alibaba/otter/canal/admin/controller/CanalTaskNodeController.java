package com.alibaba.otter.canal.admin.controller;


import com.alibaba.otter.canal.admin.model.BaseModel;
import com.alibaba.otter.canal.admin.model.Pager;
import com.alibaba.otter.canal.admin.model.TaskNodeServer;
import com.alibaba.otter.canal.admin.service.TaskNodeServerService;
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

/**
 * 任务节点控制器
 * @author mujingjing
 * @date 2024/2/5
 **/
@RestController
@RequestMapping("/api/{env}")
public class CanalTaskNodeController {
    @Autowired
    TaskNodeServerService taskNodeServerService;

    /**
     * 获取所有节点信息列表
     *
     * @param taskNodeServer 筛选条件
     * @param env 环境变量
     * @return 节点信息列表
     */
    @GetMapping(value = "/taskNodeServers")
    public BaseModel<Pager<TaskNodeServer>> nodeServers(TaskNodeServer taskNodeServer, Pager<TaskNodeServer> pager,
                                                    @PathVariable String env) {
        return BaseModel.getInstance(taskNodeServerService.findList(taskNodeServer, pager));
    }

    /**
     * 保存节点信息
     *
     * @param taskNodeServer 节点信息
     * @param env 环境变量
     * @return 是否成功
     */
    @PostMapping(value = "/taskNodeServer")
    public BaseModel<String> save(@RequestBody TaskNodeServer taskNodeServer, @PathVariable String env) {
        taskNodeServerService.save(taskNodeServer);
        return BaseModel.getInstance("success");
    }

    /**
     * 获取节点信息详情
     *
     * @param id 节点信息id
     * @param env 环境变量
     * @return 检点信息
     */
    @GetMapping(value = "/taskNodeServer/{id}")
    public BaseModel<TaskNodeServer> detail(@PathVariable Long id, @PathVariable String env) {
        return BaseModel.getInstance(taskNodeServerService.detail(id));
    }

    /**
     * 修改节点信息
     *
     * @param taskNodeServer 节点信息
     * @param env 环境变量
     * @return 是否成功
     */
    @PutMapping(value = "/taskNodeServer")
    public BaseModel<String> update(@RequestBody TaskNodeServer taskNodeServer, @PathVariable String env) {
        taskNodeServerService.update(taskNodeServer);
        return BaseModel.getInstance("success");
    }

    /**
     * 删除节点信息
     *
     * @param id 节点信息id
     * @param env 环境变量
     * @return 是否成功
     */
    @DeleteMapping(value = "/taskNodeServer/{id}")
    public BaseModel<String> delete(@PathVariable Long id, @PathVariable String env) {
        taskNodeServerService.delete(id);
        return BaseModel.getInstance("success");
    }

    /**
     * 获取远程节点运行状态
     *
     * @param ip 节点ip
     * @param port 节点端口
     * @param env 环境变量
     * @return 状态信息
     */
    @GetMapping(value = "/taskNodeServer/status")
    public BaseModel<Integer> status(@RequestParam String ip, @RequestParam Integer port, @PathVariable String env) {
        return BaseModel.getInstance(taskNodeServerService.remoteTaskNodeStatus(ip, port));
    }

    /**
     * 获取远程节点日志
     *
     * @param id 节点id
     * @param env 环境变量
     * @return 节点日志
     */
    @GetMapping(value = "/taskNodeServer/log/{id}")
    public BaseModel<String> log(@PathVariable Long id, @PathVariable String env) {
        return BaseModel.getInstance(taskNodeServerService.remoteTaskNodeLog(id));
    }
}
