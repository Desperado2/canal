package com.alibaba.otter.canal.k2s.sheduler;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.k2s.client.AdminManageClient;
import com.alibaba.otter.canal.k2s.config.ApplicationStatus;
import com.alibaba.otter.canal.k2s.config.KafkaToStarrocksConfig;
import com.alibaba.otter.canal.k2s.utils.AddressUtils;
import com.alibaba.otter.canal.k2s.utils.PasswordUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 心跳任务
 * @author mujingjing
 * @date 2024/2/5
 **/
@Service
public class HeartbeatService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeartbeatService.class);

    @Autowired
    private AdminManageClient adminManageClient;

    @Autowired
    private KafkaToStarrocksConfig kafkaToStarrocksConfig;


    /**
     * 定时任务每隔5秒执行一次
     *
     * 心跳到admin
     */
    @Scheduled(fixedRate = 5000)
    public void sendHeartbeat() {
        String registerIp = kafkaToStarrocksConfig.getCanalRegisterIp();
        if (StringUtils.isBlank(registerIp)) {
            registerIp = AddressUtils.getHostIp();
        }
        JSONObject heartbeat = adminManageClient.heartbeat(kafkaToStarrocksConfig.getCanalAdminManagerUrl(),
                kafkaToStarrocksConfig.getCanalAdminUser(),
                PasswordUtil.encrypt(kafkaToStarrocksConfig.getCanalAdminPassword()),
                registerIp,
                kafkaToStarrocksConfig.getServerPort());
        if(!heartbeat.getInteger("code").equals(20000)){
            LOGGER.error("心跳失败，连接不到admin, adminUri:{}",kafkaToStarrocksConfig.getCanalAdminManagerUrl());
        }
    }


    /**
     * 定时任务每隔5秒执行一次
     * 注册节点到admin
     */
    @Scheduled(fixedRate = 5000)
    public void register() {
        ApplicationStatus applicationStatus = ApplicationStatus.getApplicationStatus();
        if(applicationStatus.isRegister()){
            return;
        }
        String registerIp = kafkaToStarrocksConfig.getCanalRegisterIp();
        if (StringUtils.isBlank(registerIp)) {
            registerIp = AddressUtils.getHostIp();
        }
        JSONObject heartbeat = adminManageClient.registerTaskNode(kafkaToStarrocksConfig.getCanalAdminManagerUrl(),
                kafkaToStarrocksConfig.getCanalAdminUser(),
                PasswordUtil.encrypt(kafkaToStarrocksConfig.getCanalAdminPassword()),
                registerIp,
                kafkaToStarrocksConfig.getServerPort(),
                kafkaToStarrocksConfig.getCanalAdminRegisterAuto(),
                kafkaToStarrocksConfig.getCanalAdminRegisterCluster(),
                kafkaToStarrocksConfig.getCanalAdminRegisterName()
        );
        if(!heartbeat.getInteger("code").equals(20000)){
            LOGGER.error("注册任务节点失败，连接不到admin, adminUri:{}",kafkaToStarrocksConfig.getCanalAdminManagerUrl());
        }else{
            applicationStatus.setRegister(true);
        }
    }
}
