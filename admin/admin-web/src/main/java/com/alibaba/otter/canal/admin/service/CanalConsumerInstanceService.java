package com.alibaba.otter.canal.admin.service;

import com.alibaba.otter.canal.admin.model.ConsumerInstanceConfig;
import com.alibaba.otter.canal.admin.model.Pager;

import java.util.List;
import java.util.Map;

/**
 * Canal实例配置信息业务层接口
 *
 * @author rewerma 2019-07-13 下午05:12:16
 * @version 1.0.0
 */
public interface CanalConsumerInstanceService {

    Pager<ConsumerInstanceConfig> findList(ConsumerInstanceConfig consumerInstanceConfig, Pager<ConsumerInstanceConfig> pager);

    void save(ConsumerInstanceConfig consumerInstanceConfig);

    ConsumerInstanceConfig detail(Long id);

    void updateContent(ConsumerInstanceConfig consumerInstanceConfig);

    void delete(Long id);

    Map<String, String> remoteInstanceLog(Long id, Long nodeId);

    boolean instanceOperation(Long id, String option);

    List<ConsumerInstanceConfig> findActiveInstanceByServerId(Long serverId);
}
