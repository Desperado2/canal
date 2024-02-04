package com.alibaba.otter.canal.k2s.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * kafka to starrocks 配置
 * @author mujingjing
 * @date 2024/2/4
 **/
@Component
@Configuration(value = "classpath:application.properties")
public class KafkaToStarrocksConfig {

    @Value("${canal.register.ip}")
    private String canalRegisterIp;

    @Value("${canal.admin.manager}")
    private String canalAdminManagerUrl;

    @Value("${canal.admin.port}")
    private String canalAdminPort;

    @Value("${canal.admin.user}")
    private String canalAdminUser;

    @Value("${canal.admin.passwd}")
    private String canalAdminPassword;

    @Value("${canal.admin.register.auto}")
    private Boolean canalAdminRegisterAuto;

    @Value("${canal.admin.register.cluster}")
    private String canalAdminRegisterCluster;

    @Value("${canal.admin.register.name}")
    private String canalAdminRegisterName;

    public String getCanalRegisterIp() {
        return canalRegisterIp;
    }

    public void setCanalRegisterIp(String canalRegisterIp) {
        this.canalRegisterIp = canalRegisterIp;
    }

    public String getCanalAdminManagerUrl() {
        return canalAdminManagerUrl;
    }

    public void setCanalAdminManagerUrl(String canalAdminManagerUrl) {
        this.canalAdminManagerUrl = canalAdminManagerUrl;
    }

    public String getCanalAdminPort() {
        return canalAdminPort;
    }

    public void setCanalAdminPort(String canalAdminPort) {
        this.canalAdminPort = canalAdminPort;
    }

    public String getCanalAdminUser() {
        return canalAdminUser;
    }

    public void setCanalAdminUser(String canalAdminUser) {
        this.canalAdminUser = canalAdminUser;
    }

    public String getCanalAdminPassword() {
        return canalAdminPassword;
    }

    public void setCanalAdminPassword(String canalAdminPassword) {
        this.canalAdminPassword = canalAdminPassword;
    }

    public Boolean getCanalAdminRegisterAuto() {
        return canalAdminRegisterAuto;
    }

    public void setCanalAdminRegisterAuto(Boolean canalAdminRegisterAuto) {
        this.canalAdminRegisterAuto = canalAdminRegisterAuto;
    }

    public String getCanalAdminRegisterCluster() {
        return canalAdminRegisterCluster;
    }

    public void setCanalAdminRegisterCluster(String canalAdminRegisterCluster) {
        this.canalAdminRegisterCluster = canalAdminRegisterCluster;
    }

    public String getCanalAdminRegisterName() {
        return canalAdminRegisterName;
    }

    public void setCanalAdminRegisterName(String canalAdminRegisterName) {
        this.canalAdminRegisterName = canalAdminRegisterName;
    }
}
