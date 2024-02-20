## 基本说明
canal 1.1.1版本之后, 默认支持将canal server接收到的binlog数据直接投递到MQ, 目前默认支持的MQ系统有:
* kafka: [https://github.com/apache/kafka](https://github.com/apache/kafka)
* RocketMQ : [https://github.com/apache/rocketmq](https://github.com/apache/rocketmq)
* RabbitMQ : [https://github.com/rabbitmq/rabbitmq-server](https://github.com/rabbitmq/rabbitmq-server)
* pulsarmq : [https://github.com/apache/pulsar](https://github.com/apache/pulsar)

##  环境版本
* 操作系统：CentOS release 6.6 (Final)
* java版本: jdk1.8
* canal 版本: 请下载最新的安装包，本文以当前v1.1.1 的canal.deployer-1.1.1.tar.gz为例
* MySQL版本 ：5.7.18
* 注意 ： 关闭所有机器的防火墙，同时注意启动可以相互telnet ip 端口 

##  一、 安装zookeeper
参考：[Zookeeper QuickStart](https://github.com/alibaba/canal/wiki/Zookeeper-QuickStart)

## 二、安装MQ

* Kafka安装参考：[Kafka QuickStart](https://github.com/alibaba/canal/wiki/Kafka-QuickStart)
* RocketMQ安装参考：[RocketMQ QuickStart](https://rocketmq.apache.org/docs/quick-start)
* RabbitMQ安装参考：[RabbitMQ QuickStart](https://www.rabbitmq.com/download.html)
* PulsarMQ安装参考：[PulsarMQ QuickStart](https://pulsar.apache.org/docs/2.2.1/getting-started-docker/)

## 三、 安装canal.server

### 3.1 下载压缩包

到官网地址([release](https://github.com/alibaba/canal/releases))下载最新压缩包,请下载 canal.deployer-`latest`.tar.gz

### 3.2 将canal.deployer 复制到固定目录并解压

```
mkdir -p /usr/local/canal
cp   canal.deployer-1.1.6.tar.gz   /usr/local/canal
tar -zxvf canal.deployer-1.1.6.tar.gz 
```

### 3.3 配置修改参数

#### a. 修改instance 配置文件 `vi conf/example/instance.properties`

```
#  按需修改成自己的数据库信息
#################################################
...
canal.instance.master.address=192.168.1.20:3306
# username/password,数据库的用户名和密码
...
canal.instance.dbUsername = canal
canal.instance.dbPassword = canal
...
# mq config
canal.mq.topic=example
# 针对库名或者表名发送动态topic
#canal.mq.dynamicTopic=mytest,.*,mytest.user,mytest\\..*,.*\\..*
canal.mq.partition=0
# hash partition config
#canal.mq.partitionsNum=3
#库名.表名: 唯一主键，多个表之间用逗号分隔
#canal.mq.partitionHash=mytest.person:id,mytest.role:id
#################################################

```
对应ip 地址的MySQL 数据库需进行相关初始化与设置, 可参考 [Canal QuickStart](https://github.com/alibaba/canal/wiki/QuickStart)

#### b. 修改canal 配置文件`vi /usr/local/canal/conf/canal.properties ` 
```
# ...
# 可选项: tcp(默认), kafka,RocketMQ,rabbitmq,pulsarmq
canal.serverMode = kafka
# ...

# Canal的batch size, 默认50K, 由于kafka最大消息体限制请勿超过1M(900K以下)
canal.mq.canalBatchSize = 50
# Canal get数据的超时时间, 单位: 毫秒, 空为不限超时
canal.mq.canalGetTimeout = 100
# 是否为flat json格式对象
canal.mq.flatMessage = false
```

## mq相关参数说明 (<=1.1.4版本)

| 参数名 | 参数说明 | 默认值 |
|-------|-----------|-------|
| canal.mq.servers | kafka为bootstrap.servers <br> rocketMQ中为nameserver列表  | 127.0.0.1:6667 |
| canal.mq.retries | 发送失败重试次数 | 0 |
| canal.mq.batchSize | kafka为`ProducerConfig.BATCH_SIZE_CONFIG` <br> rocketMQ无意义 | 16384 |
| canal.mq.maxRequestSize | kafka为`ProducerConfig.MAX_REQUEST_SIZE_CONFIG` <br> rocketMQ无意义 | 1048576 |
| canal.mq.lingerMs | kafka为`ProducerConfig.LINGER_MS_CONFIG` , 如果是flatMessage格式建议将该值调大, 如: 200<br> rocketMQ无意义 | 1 |
| canal.mq.bufferMemory | kafka为`ProducerConfig.BUFFER_MEMORY_CONFIG` <br> rocketMQ无意义 | 33554432 |
| canal.mq.acks | kafka为`ProducerConfig.ACKS_CONFIG` <br> rocketMQ无意义  | all | 
| canal.mq.kafka.kerberos.enable | kafka为`ProducerConfig.ACKS_CONFIG` <br> rocketMQ无意义  | false | 
| canal.mq.kafka.kerberos.krb5FilePath | kafka kerberos认证 <br> rocketMQ无意义  | ../conf/kerberos/krb5.conf | 
| canal.mq.kafka.kerberos.jaasFilePath | kafka kerberos认证 <br> rocketMQ无意义  | ../conf/kerberos/jaas.conf | 
| canal.mq.producerGroup | kafka无意义 <br> rocketMQ为ProducerGroup名 | Canal-Producer | 
| canal.mq.accessChannel | kafka无意义 <br> rocketMQ为channel模式，如果为aliyun则配置为cloud | local | 
| --- | --- | --- |
| canal.mq.vhost= | rabbitMQ配置 | 无 | 
| canal.mq.exchange= | rabbitMQ配置 | 无 | 
| canal.mq.username= | rabbitMQ配置 | 无 | 
| canal.mq.password= | rabbitMQ配置 | 无 | 
| canal.mq.aliyunuid= | rabbitMQ配置 | 无 | 
| --- | --- | --- |
| canal.mq.canalBatchSize | 获取canal数据的批次大小 | 50 |
| canal.mq.canalGetTimeout | 获取canal数据的超时时间 | 100 |
| canal.mq.parallelThreadSize | mq数据转换并行处理的并发度 | 8 |
| canal.mq.flatMessage | 是否为json格式 <br> 如果设置为false,对应MQ收到的消息为protobuf格式<br>需要通过CanalMessageDeserializer进行解码 | false | 
| --- | --- | --- |
| canal.mq.topic | mq里的topic名 | 无 | 
| canal.mq.dynamicTopic | mq里的动态topic规则, 1.1.3版本支持 | 无 | 
| canal.mq.partition | 单队列模式的分区下标， | 1 | 
| canal.mq.partitionsNum | 散列模式的分区数 | 无 |
| canal.mq.partitionHash | 散列规则定义 <br> 库名.表名 : 唯一主键，比如mytest.person: id <br> 1.1.3版本支持新语法，见下文 | 无 | 

## mq相关参数说明 (>=1.1.5版本)

在1.1.5版本开始，引入了[MQ Connector设计](https://github.com/alibaba/canal/pull/2562)，因此参数配置做了部分调整

| 参数名 | 参数说明 | 默认值 |
|------|-----------|-------|
| canal.aliyun.accessKey | 阿里云ak | 无 |
| canal.aliyun.secretKey | 阿里云sk | 无 |
| canal.aliyun.uid | 阿里云uid | 无 | 
| canal.mq.flatMessage | 是否为json格式 <br> 如果设置为false,对应MQ收到的消息为protobuf格式<br>需要通过CanalMessageDeserializer进行解码 | false | 
| canal.mq.canalBatchSize | 获取canal数据的批次大小 | 50 |
| canal.mq.canalGetTimeout | 获取canal数据的超时时间 | 100 |
| canal.mq.accessChannel = local | 是否为阿里云模式，可选值local/cloud | local |
| canal.mq.database.hash | 是否开启database混淆hash，确保不同库的数据可以均匀分散，如果关闭可以确保只按照业务字段做MQ分区计算 | true |
| canal.mq.send.thread.size | MQ消息发送并行度  |  30 | 
| canal.mq.build.thread.size | MQ消息构建并行度  | 8 | 
|------|-----------|-------|
| kafka.bootstrap.servers | kafka服务端地址  | 127.0.0.1:9092 |
| kafka.acks| kafka为`ProducerConfig.ACKS_CONFIG`  | all | 
| kafka.compression.type | 压缩类型 | none |
| kafka.batch.size | kafka为`ProducerConfig.BATCH_SIZE_CONFIG`  | 16384 |
| kafka.linger.ms | kafka为`ProducerConfig.LINGER_MS_CONFIG` , 如果是flatMessage格式建议将该值调大, 如: 200 |  1 |
| kafka.max.request.size | kafka为`ProducerConfig.MAX_REQUEST_SIZE_CONFIG` | 1048576 |
| kafka.buffer.memory | kafka为`ProducerConfig.BUFFER_MEMORY_CONFIG`  | 33554432 |
| kafka.max.in.flight.requests.per.connection | kafka为`ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION` | 1 |
| kafka.retries | 发送失败重试次数 | 0 |
| kafka.kerberos.enable | kerberos认证 | false |
| kafka.kerberos.krb5.file | kerberos认证  |  ../conf/kerberos/krb5.conf |
| kafka.kerberos.jaas.file | kerberos认证  | ../conf/kerberos/jaas.conf |
|------|-----------|-------|
| rocketmq.producer.group | rocketMQ为ProducerGroup名  | test |
| rocketmq.enable.message.trace | 是否开启message trace |  false | 
| rocketmq.customized.trace.topic | message trace的topic | 无 | 
| rocketmq.namespace | rocketmq的namespace | 无 | 
| rocketmq.namesrv.addr | rocketmq的namesrv地址 | 127.0.0.1:9876 |
| rocketmq.retry.times.when.send.failed | 重试次数 | 0 |
| rocketmq.vip.channel.enabled | rocketmq是否开启vip channel | false | 
| rocketmq.tag | rocketmq的tag配置 | 空值 | 
| --- | --- | --- |
| rabbitmq.host | rabbitMQ配置 | 无 | 
| rabbitmq.virtual.host | rabbitMQ配置 | 无 | 
| rabbitmq.exchange | rabbitMQ配置 | 无 | 
| rabbitmq.username | rabbitMQ配置 | 无 | 
| rabbitmq.password | rabbitMQ配置 | 无 | 
| rabbitmq.deliveryMode | rabbitMQ配置 | 无 | 
| --- | --- | --- |
| pulsarmq.serverUrl | pulsarmq配置 | 无 | 
| pulsarmq.roleToken | pulsarmq配置 | 无 | 
| pulsarmq.topicTenantPrefix | pulsarmq配置 | 无 | 
| --- | --- | --- |
| canal.mq.topic | mq里的topic名 | 无 | 
| canal.mq.dynamicTopic | mq里的动态topic规则, 1.1.3版本支持 | 无 | 
| canal.mq.partition | 单队列模式的分区下标， | 1 | 
| canal.mq.enableDynamicQueuePartition | 动态获取MQ服务端的分区数,如果设置为true之后会自动根据topic获取分区数替换canal.mq.partitionsNum的定义,目前主要适用于RocketMQ | false | 
| canal.mq.partitionsNum | 散列模式的分区数 | 无 |
| canal.mq.dynamicTopicPartitionNum | mq里的动态队列分区数,比如针对不同topic配置不同partitionsNum | 无 |
| canal.mq.partitionHash | 散列规则定义 <br> 库名.表名 : 唯一主键，比如mytest.person: id <br> 1.1.3版本支持新语法，见下文 | 无 | 

### canal.mq.dynamicTopic 表达式说明

canal 1.1.3版本之后, 支持配置格式：schema 或 schema.table，多个配置之间使用逗号或分号分隔
* 例子1：test\\\\.test 指定匹配的单表，发送到以test_test为名字的topic上
* 例子2：.*\\\\..\* 匹配所有表，则每个表都会发送到各自表名的topic上
* 例子3：test 指定匹配对应的库，一个库的所有表都会发送到库名的topic上
* 例子4：test\\\\..* 指定匹配的表达式，针对匹配的表会发送到各自表名的topic上
* 例子5：test,test1\\\\.test1，指定多个表达式，会将test库的表都发送到test的topic上，test1\\\\.test1的表发送到对应的test1_test1 topic上，其余的表发送到默认的canal.mq.topic值

为满足更大的灵活性，允许对匹配条件的规则指定发送的topic名字，配置格式：topicName:schema 或 topicName:schema.table
* 例子1: test:test\\\\.test 指定匹配的单表，发送到以test为名字的topic上
* 例子2: test:.*\\\\..\* 匹配所有表，因为有指定topic，则每个表都会发送到test的topic下
* 例子3: test:test 指定匹配对应的库，一个库的所有表都会发送到test的topic下
* 例子4：testA:test\\\\..* 指定匹配的表达式，针对匹配的表会发送到testA的topic下
* 例子5：test0:test,test1:test1\\\\.test1，指定多个表达式，会将test库的表都发送到test0的topic下，test1\\\\.test1的表发送到对应的test1的topic下，其余的表发送到默认的canal.mq.topic值

大家可以结合自己的业务需求，设置匹配规则，建议MQ开启自动创建topic的能力

### canal.mq.partitionHash 表达式说明

canal 1.1.3版本之后, 支持配置格式：schema.table:pk1^pk2，多个配置之间使用逗号分隔
* 例子1：test\\\\.test:pk1^pk2 指定匹配的单表，对应的hash字段为pk1 + pk2
* 例子2：.*\\\\..\*:id 正则匹配，指定所有正则匹配的表对应的hash字段为id
* 例子3：.*\\\\..\*:$pk$ 正则匹配，指定所有正则匹配的表对应的hash字段为表主键(自动查找)
* 例子4:  匹配规则啥都不写，则默认发到0这个partition上
* 例子5：.*\\\\..\* ，不指定pk信息的正则匹配，将所有正则匹配的表,对应的hash字段为表名
   *   按表hash: 一张表的所有数据可以发到同一个分区，不同表之间会做散列 (会有热点表分区过大问题)
* 例子6:  test\\\\.test:id,.\\\\..\* ,  针对test的表按照id散列,其余的表按照table散列

注意：大家可以结合自己的业务需求，设置匹配规则，多条匹配规则之间是按照顺序进行匹配(命中一条规则就返回)

其他详细参数可参考[Canal AdminGuide](https://github.com/alibaba/canal/wiki/AdminGuide)

## mq顺序性问题

binlog本身是有序的，写入到mq之后如何保障顺序是很多人会比较关注，在issue里也有非常多人咨询了类似的问题，这里做一个统一的解答

1. canal目前选择支持的kafka/rocketmq，本质上都是基于本地文件的方式来支持了分区级的顺序消息的能力，也就是binlog写入mq是可以有一些顺序性保障，这个取决于用户的一些参数选择
2. canal支持MQ数据的几种路由方式：单topic单分区，单topic多分区、多topic单分区、多topic多分区
  * canal.mq.dynamicTopic，主要控制是否是单topic还是多topic，针对命中条件的表可以发到表名对应的topic、库名对应的topic、默认topic name
  * canal.mq.partitionsNum、canal.mq.partitionHash，主要控制是否多分区以及分区的partition的路由计算，针对命中条件的可以做到按表级做分区、pk级做分区等
3. canal的消费顺序性，主要取决于描述2中的路由选择，举例说明：
  * 单topic单分区，可以严格保证和binlog一样的顺序性，缺点就是性能比较慢，单分区的性能写入大概在2~3k的TPS
  * 多topic单分区，可以保证表级别的顺序性，一张表或者一个库的所有数据都写入到一个topic的单分区中，可以保证有序性，针对热点表也存在写入分区的性能问题
  * 单topic、多topic的多分区，如果用户选择的是指定table的方式，那和第二部分一样，保障的是表级别的顺序性(存在热点表写入分区的性能问题)，如果用户选择的是指定pk hash的方式，那只能保障的是一个pk的多次binlog顺序性
  ** pk hash的方式需要业务权衡，这里性能会最好，但如果业务上有pk变更或者对多pk数据有顺序性依赖，就会产生业务处理错乱的情况. 如果有pk变更，pk变更前和变更后的值会落在不同的分区里，业务消费就会有先后顺序的问题，需要注意

## MQ发送性能数据
1.1.5版本可以在5k~50k左右，具体可参考：[[Canal-MQ-Performance]]

## 阿里云RocketMQ对接参数
```
# 配置ak/sk
canal.aliyun.accessKey = XXX
canal.aliyun.secretKey = XXX
# 配置topic
canal.mq.accessChannel = cloud
canal.mq.servers = 内网接入点
canal.mq.producerGroup = GID_**group（在后台创建）
canal.mq.namespace = rocketmq实例id
canal.mq.topic=（在后台创建）
```

## kafka ssl配置参数
```
# canal.properties配置文件

kafka.kerberos.enable = true
kafka.kerberos.krb5.file = "../conf/kerberos/krb5.conf"
kafka.kerberos.jaas.file = "../conf/kerberos/jaas.conf"
```

### 3.4 启动
```
cd /usr/local/canal/
sh bin/startup.sh
```
### 3.5 查看日志
a.查看 logs/canal/canal.log 
```
vi logs/canal/canal.log
```
b. 查看instance的日志：
```
vi logs/example/example.log
```
### 3.6 关闭
```
cd /usr/local/canal/
sh bin/stop.sh
```
### 3.7 MQ数据消费

canal.client下有对应的MQ数据消费的样例工程,包含数据编解码的功能
* kafka模式: [com.alibaba.otter.canal.example.kafka.CanalKafkaClientExample](https://github.com/alibaba/canal/blob/master/example/src/main/java/com/alibaba/otter/canal/example/kafka/CanalKafkaClientExample.java)
* rocketMQ模式: [com.alibaba.otter.canal.example.rocketmq.CanalRocketMQClientExample](https://github.com/alibaba/canal/blob/master/example/src/main/java/com/alibaba/otter/canal/example/rocketmq/CanalRocketMQClientExample.java)
* rocketMQ模式: [com.alibaba.otter.canal.example.rocketmq.CanalRocketMQClientExample](https://github.com/alibaba/canal/blob/master/example/src/main/java/com/alibaba/otter/canal/example/rocketmq/CanalRocketMQClientExample.java)