package com.alibaba.otter.canal.k2s.kafka.client;


import com.alibaba.fastjson.JSONObject;
import com.dtflys.forest.annotation.Get;
import com.dtflys.forest.annotation.Header;
import com.dtflys.forest.annotation.Var;


/**
 * admin客户端
 * @author mujingjing
 * @date 2024/2/4
 **/
public interface AdminManageClient {

    @Get(value = "{basePath}/api/consumer/1/config/mapping_polling/{envCode}", headers = {
            "Accept-Charset: utf-8",
            "Content-Type: application/json"
    })
    JSONObject queryMappingByEnv(@Var("basePath") String basePath,
                                 @Header("user") String user,
                                 @Header("passwd") String passwd,
                                 @Var("envCode") String envCode);
}
