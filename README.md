## 简介

![](https://img-blog.csdnimg.cn/20191104101735947.png)

**canal [kə'næl]**，译意为水道/管道/沟渠，主要用途是基于 MySQL 数据库增量日志解析，提供增量数据订阅和消费

早期阿里巴巴因为杭州和美国双机房部署，存在跨机房同步的业务需求，实现方式主要是基于业务 trigger 获取增量变更。从 2010 年开始，业务逐步尝试数据库日志解析获取增量变更进行同步，由此衍生出了大量的数据库增量订阅和消费业务。

基于日志增量订阅和消费的业务包括
- 数据库镜像
- 数据库实时备份
- 索引构建和实时维护(拆分异构索引、倒排索引等)
- 业务 cache 刷新
- 带业务逻辑的增量数据处理

当前的 canal 支持源端 MySQL 版本包括 5.1.x , 5.5.x , 5.6.x , 5.7.x , 8.0.x

## 工作原理

#### MySQL主备复制原理
![](http://dl.iteye.com/upload/attachment/0080/3086/468c1a14-e7ad-3290-9d3d-44ac501a7227.jpg)

- MySQL master 将数据变更写入二进制日志( binary log, 其中记录叫做二进制日志事件binary log events，可以通过 show binlog events 进行查看)
- MySQL slave 将 master 的 binary log events 拷贝到它的中继日志(relay log)
- MySQL slave 重放 relay log 中事件，将数据变更反映它自己的数据

#### canal 工作原理

- canal 模拟 MySQL slave 的交互协议，伪装自己为 MySQL slave ，向 MySQL master 发送dump 协议
- MySQL master 收到 dump 请求，开始推送 binary log 给 slave (即 canal )
- canal 解析 binary log 对象(原始为 byte 流)

## 文档

- [Home](wiki/Home.md)
- [Introduction](wiki/Introduction.md)
- [QuickStart](wiki/QuickStart.md)
  - [Docker QuickStart](wiki/Docker-QuickStart.md)
  - [Canal Kafka/RocketMQ QuickStart](wiki/Canal-Kafka-RocketMQ-QuickStart.md)
  - [Aliyun RDS for MySQL QuickStart](wiki/aliyun-RDS-QuickStart.md)
  - [Prometheus QuickStart](wiki/Prometheus-QuickStart.md)
- Canal Admin
  - [Canal Admin QuickStart](wiki/Canal-Admin-QuickStart.md)
  - [Canal Admin Guide](wiki/Canal-Admin-Guide.md)
  - [Canal Admin ServerGuide](wiki/Canal-Admin-ServerGuide.md)
  - [Canal Admin Docker](wiki/Canal-Admin-Docker.md)
- [AdminGuide](wiki/AdminGuide.md)
- [ClientExample](wiki/ClientExample.md)
- [ClientAPI](wiki/ClientAPI.md)
- [Performance](wiki/Performance.md)
- [DevGuide](wiki/DevGuide.md)
- [BinlogChange(MySQL 5.6)](wiki/BinlogChange%28mysql5.6%29.md)
- [BinlogChange(MariaDB)](wiki/BinlogChange%28MariaDB%29.md)
- [TableMetaTSDB](wiki/TableMetaTSDB.md)
- [FAQ](wiki/FAQ.md)

## 自我构建
```text
mvn clean install -Denv=release
```
#### 构建生成文件说明
构建完成之后，文件在项目根目录中的target/目录下面，生成的文件说明如下：
canal.admin-1.1.8-SNAPSHOT.tar.gz   admin管理端  
canal.deployer-1.1.8-SNAPSHOT.tar.gz    canal数据服务  
canal.adapter-1.1.8-SNAPSHOT.tar.gz     canal数据同步服务  
canal.example-1.1.8-SNAPSHOT.tar.gz     canal示例  
canal.kafka-to-starrocks-1.1.8-SNAPSHOT.tar.gz  kafka同步数据到StarRcoks服务

