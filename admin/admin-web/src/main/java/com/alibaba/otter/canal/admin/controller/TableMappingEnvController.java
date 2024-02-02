package com.alibaba.otter.canal.admin.controller;

import com.alibaba.otter.canal.admin.model.BaseModel;
import com.alibaba.otter.canal.admin.model.NodeServer;
import com.alibaba.otter.canal.admin.model.Pager;
import com.alibaba.otter.canal.admin.model.TableStructureMapping;
import com.alibaba.otter.canal.admin.model.TableStructureMappingEnv;
import com.alibaba.otter.canal.admin.service.CanalTableMappingEnvService;
import com.alibaba.otter.canal.admin.service.NodeServerService;
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

/**
 * 节点信息控制层
 *
 * @author rewerma 2019-07-13 下午05:12:16
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/{env}")
public class TableMappingEnvController {

    @Autowired
    CanalTableMappingEnvService canalTableMappingEnvService;

    /**
     * 获取所有信息列表
     *
     * @param pager 分页数据
     * @param env 环境变量
     * @return 节点信息列表
     */
    @GetMapping(value = "/tableMappingEnv")
    public BaseModel<Pager<TableStructureMappingEnv>> tableMappingEnv(Pager<TableStructureMappingEnv> pager,
                                                    @PathVariable String env) {
        return BaseModel.getInstance(canalTableMappingEnvService.findList(pager));
    }

    /**
     * 保存信息
     *
     * @param tableStructureMappingEnv 信息
     * @param env 环境变量
     * @return 是否成功
     */
    @PostMapping(value = "/tableMappingEnv")
    public BaseModel<String> save(@RequestBody TableStructureMappingEnv tableStructureMappingEnv, @PathVariable String env) {
        canalTableMappingEnvService.save(tableStructureMappingEnv);
        return BaseModel.getInstance("success");
    }


    /**
     * 修改信息
     *
     * @param tableStructureMappingEnv 信息
     * @param env 环境变量
     * @return 是否成功
     */
    @PutMapping(value = "/tableMappingEnv")
    public BaseModel<String> update(@RequestBody TableStructureMappingEnv tableStructureMappingEnv, @PathVariable String env) {
        canalTableMappingEnvService.update(tableStructureMappingEnv);
        return BaseModel.getInstance("success");
    }


    /**
     * 获取所有信息列表
     *
     * @param env 环境变量
     * @return 节点信息列表
     */
    @GetMapping(value = "/tableMappingEnv/list")
    public BaseModel<List<TableStructureMappingEnv>> findAll(@PathVariable String env) {
        return BaseModel.getInstance(canalTableMappingEnvService.findAll());
    }
}
