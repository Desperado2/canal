package com.alibaba.otter.canal.admin.service;


import com.alibaba.otter.canal.admin.model.Pager;
import com.alibaba.otter.canal.admin.model.TableStructureMapping;
import com.alibaba.otter.canal.admin.model.TableStructureMappingEnv;

import java.util.List;

/**
 * 表结构映射服务
 *
 * @author mujingjing
 * @date 2024/2/2
 **/
public interface CanalTableMappingEnvService {

    /**
     * 保存
     * @param tableStructureMappingEnv 环境
     */
    void save(TableStructureMappingEnv tableStructureMappingEnv);


    /**
     * 查询列表
     * @return  列表
     */
    List<TableStructureMappingEnv> findAll();

    /**
     * 更新
     * @param tableStructureMappingEnv 参数
     */
    void update(TableStructureMappingEnv tableStructureMappingEnv);


    /**
     * 查询分页列表
     * @param pager 分页参数
     * @return 分页列表
     */
    Pager<TableStructureMappingEnv> findList(Pager<TableStructureMappingEnv> pager);
}
