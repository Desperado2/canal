package com.alibaba.otter.canal.admin.service;


import com.alibaba.otter.canal.admin.model.CanalInstanceConfig;
import com.alibaba.otter.canal.admin.model.Pager;
import com.alibaba.otter.canal.admin.model.TableStructureMapping;

import java.util.List;

/**
 * 表结构映射服务
 *
 * @author mujingjing
 * @date 2024/2/2
 **/
public interface CanalTableMappingService {

    /**
     * 保存
     * @param tableStructureMapping 环境
     */
    void save(TableStructureMapping tableStructureMapping);

    /**
     * 查询详情
     * @param id
     * @return
     */
    TableStructureMapping detail(Long id);


    /**
     * 更新表映射
     * @param tableStructureMapping 映射参数
     */
    void update(TableStructureMapping tableStructureMapping);


    /**
     * 删除
     * @param id id
     */
    void delete(Long id);


    /**
     * 更新表结构映射内容
     * @param tableStructureMapping 参数
     */
    void updateContent(TableStructureMapping tableStructureMapping);

    /**
     * 查询列表
     * @return  列表
     */
    List<TableStructureMapping> findAll();


    /**
     * 查询分页列表
     * @param pager 分页参数
     * @return 分页列表
     */
    Pager<TableStructureMapping> findList(Pager<TableStructureMapping> pager);


}
