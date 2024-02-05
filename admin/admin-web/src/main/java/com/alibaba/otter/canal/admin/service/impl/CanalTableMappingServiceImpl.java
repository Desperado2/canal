package com.alibaba.otter.canal.admin.service.impl;


import com.alibaba.otter.canal.admin.common.exception.ServiceException;
import com.alibaba.otter.canal.admin.model.CanalConfig;
import com.alibaba.otter.canal.admin.model.CanalInstanceConfig;
import com.alibaba.otter.canal.admin.model.NodeServer;
import com.alibaba.otter.canal.admin.model.Pager;
import com.alibaba.otter.canal.admin.model.TableStructureMapping;
import com.alibaba.otter.canal.admin.model.TableStructureMappingEnv;
import com.alibaba.otter.canal.admin.service.CanalTableMappingService;
import com.alibaba.otter.canal.protocol.SecurityUtil;
import io.ebean.Query;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * 表映射服务类
 * @author mujingjing
 * @date 2024/2/2
 **/
@Service
public class CanalTableMappingServiceImpl implements CanalTableMappingService {

    @Override
    public void save(TableStructureMapping tableStructureMapping) {
        String content = tableStructureMapping.getContent();
        if(StringUtils.isNotBlank(content)){
            try {
                String contentMd5 = SecurityUtil.md5String(tableStructureMapping.getContent());
                tableStructureMapping.setContentMd5(contentMd5);
            } catch (NoSuchAlgorithmException e) {
                // ignore
            }
        }
        // 保存
        tableStructureMapping.save();
    }

    @Override
    public TableStructureMapping detail(Long id) {
        return TableStructureMapping.find.byId(id);
    }

    @Override
    public void update(TableStructureMapping tableStructureMapping) {
        int cnt = TableStructureMapping.find.query()
                .where()
                .eq("env_code", tableStructureMapping.getEnvCode())
                .eq("src_database", tableStructureMapping.getSrcDatabase())
                .eq("src_table", tableStructureMapping.getSrcTable())
                .ne("id", tableStructureMapping.getId())
                .findCount();
        if (cnt > 0) {
            throw new ServiceException("源数据表映射信息已存在");
        }
        tableStructureMapping.update("envCode", "srcDatabase", "srcTable", "dstDatabase", "dstTable");
    }

    @Override
    public void delete(Long id) {
        TableStructureMapping tableStructureMapping = TableStructureMapping.find.byId(id);
        if (tableStructureMapping != null) {
            tableStructureMapping.delete();
        }
    }

    @Override
    public void updateContent(TableStructureMapping tableStructureMapping) {
        String content = tableStructureMapping.getContent();
        if(StringUtils.isNotBlank(content)){
            try {
                String contentMd5 = SecurityUtil.md5String(tableStructureMapping.getContent());
                tableStructureMapping.setContentMd5(contentMd5);
            } catch (NoSuchAlgorithmException e) {
                // ignore
            }
        }
        // 保存
        tableStructureMapping.update("content", "contentMd5");
    }

    @Override
    public List<TableStructureMapping> findAll() {
        return TableStructureMapping.find.all();
    }

    @Override
    public List<TableStructureMapping> findByEnv(String envCode) {
        return TableStructureMapping.find.query()
                .where()
                .eq("envCode", envCode).findList();
    }

    @Override
    public Pager<TableStructureMapping> findList(Pager<TableStructureMapping> pager) {
        Query<TableStructureMapping> query = TableStructureMapping.find.query();
        int count = query.findCount();
        pager.setCount((long)count);
        List<TableStructureMapping> tableStructureMappingList = query.order().asc("id")
                .setFirstRow(pager.getOffset().intValue())
                .setMaxRows(pager.getSize())
                .findList();
        pager.setItems(tableStructureMappingList);
        return pager;
    }
}
