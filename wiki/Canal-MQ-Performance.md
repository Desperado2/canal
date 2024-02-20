# 测试环境

| 类型 | 配置 |
| --- | --- |
| MySQL A + Canal Server | Intel(R) Xeon(R) CPU E5-2430 0 @ 2.20GHz  (24core 96G) |
| MQ(kafka/RocketMQ) | Intel(R) Xeon(R) CPU E5-2430 0 @ 2.20GHz  (24core 96G) |

## 测试方式

1.  混合的DML场景测试 (模拟业务场景，小事务)
	* 混合2张表的insert、update、delete，包含最大10个bantch的insert、单条update和随机单条delete
2.  batch insert场景 (模拟批量场景，大事务)

MQ服务配置层面基本采用默认参数，比如:
1.  kafka  https://kafka.apache.org/quickstart
2.  RocketMQ   https://rocketmq.apache.org/docs/quick-start/

Canal的Perf性能数据监控
1.  prothemues，https://github.com/alibaba/canal/wiki/Prometheus-QuickStart
2.  linux主机监控，比如dstat/top等，关注网络包和cpu利用率

## 测试总结

### 性能数据
1. 混合DML场景，同步效率在15k ~ 30k rps (每秒的事务数大概在5k~10k的tps，一个事务会包含至少3条binlog)
2. batch操作场景，同步效率在50k ~ 60k rps
3. rocketmq相比于kafka，大概有30%的性能提升
4. 非flatMessage模式相比于flatMessage模式，大概有30%~60%的性能提升 (调整了默认参数为flatMessage=false) 

### 优化思路
canal-server发送MQ消息，为了保障binlog顺序性考虑，会有串行的代码路径，需要尽可能减少串行路径上的代价，比如序列化、分区散列、flatMessage对象转化等

代码优化点：
1.  针对不开启flatMessage，默认设置canal.instance.memory.rawEntry=true，确保在发送kafka消息阶段不需要做bytes序列化。针对单队列的情况，可以有50%的性能提升
2. 针对开启flatMessage，引入多线程进行protobuf对象的转化到flat对象的过程，减少串行路径，提升会比较明显，可以有近10倍
3. 针对多topic的发送，引入多线程发送机制，减少串行路径，随着topic数量分布可以有倍数上的提升
4. 减少kafka发送端的flush频率，一个getBatchSize在最后进行一次flush阻塞 (之前在flatMessage上每条消息都做了flush)

小tips:
1.  单topic单分区 > 多topic单分区，kafka模式下估计是大batch拆分为了小batch，同时有了proto对象的反序列化开销
2.  flatMessage模式，性能普遍比不开启flatMessage模式慢，主要就是proto对象的反序列化开销和小batch的发送成本，后续可以通过更多的topic+分区追回

### 优化参数

影响性能的几个参数：
1.   canal.instance.memory.rawEntry = true (表示是否需要提前做序列化，非flatMessage场景需要设置为true)
2.  canal.mq.flatMessage = false (false代表二进制协议，true代表使用json格式，二进制协议有更好的性能)
3.  canal.mq.dynamicTopic (动态topic配置定义，可以针对不同表设置不同的topic，在flatMessage模式下可以提升并行效率)
4. canal.mq.partitionsNum/canal.mq.partitionHash (分区配置，对写入性能有反作用，不过可以提升消费端的吞吐)

# 测试数据

测试的对比项：
1.  flatMessage  开启 vs 不开启 (是否涉及数据个是转换，会有一定的转换成本和网络放大问题)
2.  单topic vs  多topic  (不同表路由到不同的topic)
3.  单分区 vs  多分区 (表的不同记录路由到不同分区)

名词解读：
1.  rps，canal store里消费entry的数量，会包含事务的begin/end事件 +  DML事件
2.  tps，事务为单位的消费速度
## 优化前 (1.1.4版本)

### 混合DML场景测试
|  场景 |  1个topic + 单分区 | 1个topic+3分区|  2个topic+1分区 | 2个topic+3分区 | 
| -------- | -------- | -------- | -------- | -------- |
| 不开启flatMessage |   20k rps <br> (7.50k tps)   |  15k rps <br>(5.7k tps)   |  11.5k rps <br>(4.56k tps) |  10.8k rps <br>(4.01k tps)  |
| 开启flatMessage  |  0.6k rps <br>(0.2k tps)  |   0.7k rps <br>(0.2k tps)  | 0.8k rps <br>(0.2k tps) |  0.9k rps <br>(0.2k tps) | 

## 优化后 (1.1.5版本)

### Kafka + 混合DML场景测试  
|  场景 |  1个topic + 单分区 | 1个topic+3分区|  2个topic+1分区 | 2个topic+3分区 | 
| -------- | -------- | -------- | -------- | -------- |
| 不开启flatMessage |   29.6k rps <br> (9.71k tps) |  17.54k rps <br> (6.53k tps) |  21.6k rps <br> (7.9k tps) |  16.8k rps <br> (5.71k tps)  |
| 开启flatMessage  |  11.79k rps  <br> (4.36k tps)  |   15.97 rps <br> (5.94k tps)  | 11.91k rps <br> (4.45k tps) |  16.96k rps <br> (6.26k tps) | 

### Kafka + 单表的batch insert场景测试
|  场景 |  1个topic + 单分区 | 1个topic+3分区|
| -------- | -------- | -------- | 
| 不开启flatMessage |   59.6k rps | 45.1k rps |
| 开启flatMessage  | 51.3k rps |  49.6k rps  |

----

### RocketMQ + 混合DML场景测试  
|  场景 |  1个topic + 单分区 | 1个topic+3分区|  2个topic+1分区 | 2个topic+3分区 | 
| -------- | -------- | -------- | -------- | -------- |
| 不开启flatMessage |   29.6k rps <br> (10.71k tps) |  23.3k rps <br> (8.59k tps) |  26.7k rps <br> (9.46k tps) |  21.7k rps <br> (7.66k tps)  |
| 开启flatMessage  |  16.75k rps <br> (6.17k tps)   |  14.96k rps <br> (5.55k tps)  |17.83k rps <br> (6.63k tps)  |  16.93k rps <br> (6.26k tps)  | 

### RocketMQ + 单表的batch insert场景测试
|  场景 |  1个topic + 单分区 | 1个topic+3分区|
| -------- | -------- | -------- | 
| 不开启flatMessage |   81.2k rps | 51.3k rps |
| 开启flatMessage  | 62.6k rps |  57.9k rps  |