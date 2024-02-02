package com.alibaba.otter.canal.admin.service.impl;


import com.alibaba.otter.canal.admin.common.exception.ServiceException;
import com.alibaba.otter.canal.admin.model.Pager;
import com.alibaba.otter.canal.admin.model.TableStructureMappingEnv;
import com.alibaba.otter.canal.admin.service.CanalTableMappingEnvService;
import io.ebean.Query;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 表映射服务类
 * @author mujingjing
 * @date 2024/2/2
 **/
@Service
public class CanalTableMappingEnvServiceImpl implements CanalTableMappingEnvService {

    @Override
    public void save(TableStructureMappingEnv tableStructureMappingEnv) {
        int cnt = TableStructureMappingEnv.find.query().where()
                .eq("env_code", tableStructureMappingEnv.getEnvCode())
                .findCount();
        if(cnt > 0){
            throw new ServiceException("环境编码已存在");
        }
        tableStructureMappingEnv.save();
    }

    @Override
    public List<TableStructureMappingEnv> findAll() {
        return TableStructureMappingEnv.find.all();
    }

    @Override
    public void update(TableStructureMappingEnv tableStructureMappingEnv) {
        tableStructureMappingEnv.update("envName", "description");
    }

    @Override
    public Pager<TableStructureMappingEnv> findList(Pager<TableStructureMappingEnv> pager) {
        Query<TableStructureMappingEnv> query = TableStructureMappingEnv.find.query();
        int count = query.findCount();
        pager.setCount((long)count);
        List<TableStructureMappingEnv> tableStructureMappingEnvList = query.order().asc("id")
                .setFirstRow(pager.getOffset().intValue())
                .setMaxRows(pager.getSize())
                .findList();
        pager.setItems(tableStructureMappingEnvList);
        return pager;
    }

}
