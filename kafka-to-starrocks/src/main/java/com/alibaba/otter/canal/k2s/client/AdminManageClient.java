package com.alibaba.otter.canal.k2s.client;


import com.alibaba.fastjson.JSONObject;
import com.dtflys.forest.annotation.Get;
import com.dtflys.forest.annotation.Header;
import com.dtflys.forest.annotation.Query;
import com.dtflys.forest.annotation.Var;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * admin客户端
 * @author mujingjing
 * @date 2024/2/4
 **/
public interface AdminManageClient {


    /**
     * 获取指定环境的配置信息
     * @param basePath admin地址
     * @param user 用户名
     * @param passwd 密码
     * @param envCode 环境编码
     * @return 配置信息
     */
    @Get(value = "http://{basePath}/api/consumer/1/config/mapping_polling/{envCode}", headers = {
            "Accept-Charset: utf-8",
            "Content-Type: application/json"
    })
    JSONObject queryMappingByEnv(@Var("basePath") String basePath,
                                 @Header("user") String user,
                                 @Header("passwd") String passwd,
                                 @Var("envCode") String envCode);


    /**
     * 获取指定环境的配置信息
     * @param basePath admin地址
     * @param user 用户名
     * @param passwd 密码
     * @param ip ip
     * @param port 端口
     * @param register 是否自动注册
     * @param cluster 集群
     * @param name 名称
     * @return 配置信息
     */
    @Get(value = "http://{basePath}/api/consumer/1/config/task_server_polling/{envCode}", headers = {
            "Accept-Charset: utf-8",
            "Content-Type: application/json"
    })
    JSONObject registerTaskNode(@Var("basePath") String basePath,
                                @Header("user") String user,
                                @Header("passwd") String passwd,
                                @Query("ip") String ip,
                                @Query("port") Integer port,
                                @Query("register") boolean register,
                                @Query("cluster") String cluster,
                                @Query("name") String name);


    /**
     * 心跳
     * @param basePath admin地址
     * @param user 用户名
     * @param passwd 密码
     * @param ip IP地址
     * @param port 端口
     * @return 返回值
     */
    @Get(value = "http://{basePath}/api/consumer/1/heartbeat/heartbeat", headers = {
            "Accept-Charset: utf-8",
            "Content-Type: application/json"
    })
    JSONObject heartbeat(@Var("basePath") String basePath,
                                 @Header("user") String user,
                                 @Header("passwd") String passwd,
                                 @Query("ip") String ip,
                                 @Query("port") Integer port);
}
