package com.alibaba.otter.canal.admin.controller;

import com.alibaba.otter.canal.admin.model.BaseModel;
import com.alibaba.otter.canal.admin.model.TableStructureMapping;
import com.alibaba.otter.canal.admin.service.CanalTableMappingService;
import com.alibaba.otter.canal.protocol.SecurityUtil;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Canal Instance配置管理控制层
 *
 * @author rewerma 2019-07-13 下午05:12:16
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/consumer/{env}/heartbeat")
public class ConsumerTaskHeartController {

    private static final byte[] seeds = RandomStringUtils.random(16).getBytes();

    @Value(value = "${canal.adminUser}")
    String user;

    @Value(value = "${canal.adminPasswd}")
    String  passwd;

    @Autowired
    CanalTableMappingService canalTableMappingService;

    /**
     * 获取mapping的配置
     */
    @GetMapping(value = "/heartbeat")
    public BaseModel<String> instanceConfigPoll(@RequestHeader String user, @RequestHeader String passwd,
                                                                     @RequestParam String ip, @RequestParam Integer port) {
        if (!auth(user, passwd)) {
            throw new RuntimeException("auth :" + user + " is failed");
        }
        // 保存心跳信息
        return BaseModel.getInstance("success");
    }

    private boolean auth(String user, String passwd) {
        // 如果user/passwd密码为空,则任何用户账户都能登录
        if ((StringUtils.isEmpty(this.user) || StringUtils.equals(this.user, user))) {
            if (StringUtils.isEmpty(this.passwd)) {
                return true;
            } else if (StringUtils.isEmpty(passwd)) {
                // 如果server密码有配置,客户端密码为空,则拒绝
                return false;
            }

            try {
                // manager这里保存了原始密码，反过来和canal发送过来的进行校验
                byte[] passForClient = SecurityUtil.scramble411(this.passwd.getBytes(), seeds);
                return SecurityUtil.scrambleServerAuth(passForClient, SecurityUtil.hexStr2Bytes(passwd), seeds);
            } catch (NoSuchAlgorithmException e) {
                return false;
            }
        }

        return false;
    }
}
