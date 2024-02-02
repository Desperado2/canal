package com.alibaba.otter.canal.admin.controller;

import com.alibaba.otter.canal.admin.common.TemplateConfigLoader;
import com.alibaba.otter.canal.admin.model.BaseModel;
import com.alibaba.otter.canal.admin.model.NodeServer;
import com.alibaba.otter.canal.admin.model.Pager;
import com.alibaba.otter.canal.admin.model.TableStructureMapping;
import com.alibaba.otter.canal.admin.model.TableStructureMappingEnv;
import com.alibaba.otter.canal.admin.service.CanalTableMappingService;
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
public class TableMappingController {

    @Autowired
    CanalTableMappingService canalTableMappingService;

    /**
     * 获取所有信息列表
     *
     * @param pager 分页参数
     * @param env 环境变量
     * @return 节点信息列表
     */
    @GetMapping(value = "/tableMapping")
    public BaseModel<Pager<TableStructureMapping>> tableMapping(Pager<TableStructureMapping> pager,
                                                               @PathVariable String env) {
        return BaseModel.getInstance(canalTableMappingService.findList(pager));
    }

    /**
     * 保存节点信息
     *
     * @param tableStructureMapping 节点信息
     * @param env 环境变量
     * @return 是否成功
     */
    @PostMapping(value = "/tableMapping")
    public BaseModel<String> save(@RequestBody TableStructureMapping tableStructureMapping, @PathVariable String env) {
        canalTableMappingService.save(tableStructureMapping);
        return BaseModel.getInstance("success");
    }

    /**
     * 获取节点信息详情
     *
     * @param id 节点信息id
     * @param env 环境变量
     * @return 检点信息
     */
    @GetMapping(value = "/tableMapping/{id}")
    public BaseModel<TableStructureMapping> detail(@PathVariable Long id, @PathVariable String env) {
        return BaseModel.getInstance(canalTableMappingService.detail(id));
    }

    /**
     * 修改节点信息
     *
     * @param tableStructureMapping 节点信息
     * @param env 环境变量
     * @return 是否成功
     */
    @PutMapping(value = "/tableMapping")
    public BaseModel<String> update(@RequestBody TableStructureMapping tableStructureMapping, @PathVariable String env) {
        canalTableMappingService.update(tableStructureMapping);
        return BaseModel.getInstance("success");
    }

    /**
     * 修改节点信息
     *
     * @param tableStructureMapping 节点信息
     * @param env 环境变量
     * @return 是否成功
     */
    @PutMapping(value = "/tableMapping/content")
    public BaseModel<String> updateContent(@RequestBody TableStructureMapping tableStructureMapping, @PathVariable String env) {
        canalTableMappingService.updateContent(tableStructureMapping);
        return BaseModel.getInstance("success");
    }

    /**
     * 删除节点信息
     *
     * @param id 节点信息id
     * @param env 环境变量
     * @return 是否成功
     */
    @DeleteMapping(value = "/tableMapping/{id}")
    public BaseModel<String> delete(@PathVariable Long id, @PathVariable String env) {
        canalTableMappingService.delete(id);
        return BaseModel.getInstance("success");
    }

    /**
     * 获取所有信息列表
     *
     * @param env 环境变量
     * @return 节点信息列表
     */
    @GetMapping(value = "/tableMapping/list")
    public BaseModel<List<TableStructureMapping>> findAll(@PathVariable String env) {
        return BaseModel.getInstance(canalTableMappingService.findAll());
    }

    @GetMapping(value = "/tableMapping/template")
    public BaseModel<String> template(@PathVariable String env) {
        return BaseModel.getInstance(TemplateConfigLoader.loadTableMappingConfig());
    }
}
