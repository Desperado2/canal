# Prometheus监控

Canal server 性能指标监控基于prometheus的实现。

关于prometheus，参见[官网](https://prometheus.io/)

##### 效果示意图
![image.png | left | 747x413](https://cdn.nlark.com/lark/0/2018/png/5565/1534741669833-f1a30fce-ca72-450a-9857-0ce8f8377d82.png "")

## Quick start

1. 安装并部署对应平台的prometheus，参见[官方guide](https://prometheus.io/docs/introduction/first_steps/)

2. 配置prometheus.yml，添加canal的job，示例：
```
  - job_name: 'canal'
    static_configs:
    - targets: ['localhost:11112'] //端口配置即为canal.properties中的canal.metrics.pull.port
```

3. 启动prometheus与canal server

4. 安装与部署[grafana](http://docs.grafana.org/)，推荐使用新版本(5.2)。

5. 启动grafana-server，使用用户admin与密码admin登录localhost:3000 (默认配置下)。

6. 配置[prometheus datasource](http://docs.grafana.org/features/datasources/prometheus/#adding-the-data-source-to-grafana).

7. 导入模板([canal/conf/metrics/Canal_instances_tmpl.json](https://raw.githubusercontent.com/alibaba/canal/master/deployer/src/main/resources/metrics/Canal_instances_tmpl.json))，参考[这里](http://docs.grafana.org/reference/export_import/#importing-a-dashboard)。

8. 进入dashboard 'Canal instances', 在'datasource'下拉框中选择刚才配置的prometheus datasource, 然后'destination'下拉框中就可以切换instance了(如果没出现instances列表就刷新下页面),  just enjoy it.

***
## canal监控相关原始指标列表：

|  指标                         | 说明            |  单位  | 精度 |
| :----                        | :----           | ----: |----: |
|canal_instance_transactions |instance接收transactions计数|-|-|
|canal_instance |instance基本信息|-|-|
|canal_instance_subscriptions |instance订阅数量|-|-|
|canal_instance_publish_blocking_time |instance dump线程提交到异步解析队列过程中的阻塞时间(仅parallel解析模式)|ms|ns|
|canal_instance_received_binlog_bytes |instance接收binlog字节数|byte|-|
|canal_instance_parser_mode |instance解析模式(是否开启parallel解析)|-|-|
|canal_instance_client_packets |instance client请求次数的计数|-|-|
|canal_instance_client_bytes|向instance client发送数据包字节计数|byte|-|
|canal_instance_client_empty_batches|向instance client发送get接口的空结果计数|-|-|
|canal_instance_client_request_error|instance client请求失败计数|-|-|
|canal_instance_client_request_latency |instance client请求的响应时间概况|-|-|
|canal_instance_sink_blocking_time |instance sink线程put数据至store的阻塞时间|ms|ns|
|canal_instance_store_produce_seq |instance store接收到的events sequence number|-|-|
|canal_instance_store_consume_seq |instance store成功消费的events sequence number|-|-|
|canal_instance_store |instance store基本信息|-|-|
|canal_instance_store_produce_mem |instance store接收到的所有events占用内存总量|byte|-|
|canal_instance_store_consume_mem |instance store成功消费的所有events占用内存总量|byte|-|
|canal_instance_put_rows|store put操作完成的table rows|-|-|
|canal_instance_get_rows|client get请求返回的table rows|-|-|
|canal_instance_ack_rows|client ack操作释放的table rows|-|-|
|canal_instance_traffic_delay |server与MySQL master的延时|ms|ms|
|canal_instance_put_delay|store put操作events的延时|ms|ms|
|canal_instance_get_delay|client get请求返回events的延时|ms|ms|
|canal_instance_ack_delay|client ack操作释放events的延时|ms|ms|

## 监控展示指标
|  指标                         | 简述            |多指标|
| :----                        | :----           | :----: |
|[Basic](https://github.com/alibaba/canal/wiki/Canal-prometheus#%E7%8A%B6%E6%80%81%E4%BF%A1%E6%81%AF)|Canal instance 基本信息。|是|
|[Network bandwith](https://github.com/alibaba/canal/wiki/Canal-prometheus#%E7%BD%91%E7%BB%9C%E5%B8%A6%E5%AE%BDkbs)|网络带宽。包含inbound(canal server读取binlog的网络带宽)和outbound(canal server返回给canal client的网络带宽)|是|
|[Delay](https://github.com/alibaba/canal/wiki/Canal-prometheus#delayseconds)|Canal server与master延时；store 的put, get, ack操作对应的延时。|是|
|[Blocking](https://github.com/alibaba/canal/wiki/Canal-prometheus#blocking)|sink线程blocking占比；dump线程blocking占比(仅parallel mode)。|是|
|[TPS(transaction)](https://github.com/alibaba/canal/wiki/Canal-prometheus#tpsmysql-transaction)|Canal instance 处理binlog的TPS，以MySQL transaction为单位计算。|否|
|[TPS(tableRows)](https://github.com/alibaba/canal/wiki/Canal-prometheus#tpstable-row)|分别对应store的put, get, ack操作针对数据表变更行的TPS|是|
|[Client requests](https://github.com/alibaba/canal/wiki/Canal-prometheus#client-requests)|Canal client请求server的请求数统计，结果按请求类型分类(比如get/ack/sub/rollback等)。|否|
|[Response time](https://github.com/alibaba/canal/wiki/Canal-prometheus#response-time)|Canal client请求server的响应时间统计。|否|
|[Empty packets](https://github.com/alibaba/canal/wiki/Canal-prometheus#empty-packets)|Canal client请求server返回空结果的统计。|是|
|[Store remain events](https://github.com/alibaba/canal/wiki/Canal-prometheus#event-store%E5%8D%A0%E7%94%A8)|Canal instance ringbuffer中堆积的events数量。|否|
|[Store remain mem](https://github.com/alibaba/canal/wiki/Canal-prometheus#event-store-memory%E5%8D%A0%E7%94%A8kb-%E4%BB%85memory-mode)|Canal instance ringbuffer中堆积的events内存使用量。|否|
|[Client QPS](https://github.com/alibaba/canal/wiki/Canal-prometheus#client-qps)|client发送请求的QPS，按GET与CLIENTACK分类统计|是|

## JVM 相关信息
> The Java client includes collectors for garbage collection, memory pools, JMX, classloading, and thread counts. These can be added individually or just use the DefaultExports to conveniently register them.
> >DefaultExports.initialize();

详见：[prometheus/client_java](https://github.com/prometheus/client_java)

## 监控指标详述与应用场景

##### Blocking
![Image text](https://raw.githubusercontent.com/lcybo/canal/master/images/idle.PNG)
```
clamp_max(rate(canal_instance_sink_blocking_time{destination="example"}[2m]), 1000) / 10
```
**sink线程blocking时间片比例(向store中put events时)。若idle占比很高，则store总体上处于满的状态，client的consume速度低于server的produce速度**
```
clamp_max(rate(canal_instance_publish_blocking_time{destination="example"}[2m]), 1000) / 10
```
**dump线程blocking时间片比例(仅parallel mode, dump线程向disruptor发布event时)。若idle占比较高：**

**1. Sinking blocking ratio也很高，则瓶颈是因为client的consume速度相对较慢。**

**2. Sinking blocking ratio较低，那么server端parser是性能瓶颈，可参考[Performance](https://github.com/alibaba/canal/wiki/Performance)进行tuning.**
***

##### Delay(seconds)
![Image text](https://raw.githubusercontent.com/lcybo/canal/master/images/delay.PNG)
```
canal_instance_traffic_delay{destination="example"} / 1000
```
**Server与MySQL master之间的延时。**
```
canal_instance_put_delay{destination="example"} / 1000
```
**Store put操作时间点的延时。**
```
canal_instance_get_delay{destination="example"} / 1000
```
**Client get操作时间点的延时。**
```
canal_instance_ack_delay{destination="example"} / 1000
```
**Client ack操作时间点的延时。**

**Note: delay的准确度依赖于master与canal server间的ntp同步。当binlog execTime超过canal server当前时间戳，则delay为0.**
***

##### 网络带宽(KB/s)
![Image text](https://github.com/lcybo/canal/blob/master/images/network.PNG?raw=true)
```
rate(canal_instance_received_binlog_bytes{destination="example"}[2m]) / 1024
```
**Dump线程读取binlog所占用带宽。当'Sink线程空闲比'与'Dump线程空闲比'都很低，delay却比较高的情况，请查看binlog接收速率是否符合预期。**
```
rate(canal_instance_client_bytes{destination="example"}[2m]) / 1024
```
**向Instance client发送格式化binlog所占用的带宽。MySQL低负载时，client get所返回的空包同样会占用不少的带宽。**
***

##### TPS(MySQL transaction)
![Image text](https://github.com/lcybo/canal/blob/master/images/transactions.PNG?raw=true)
```
rate(canal_instance_transactions{destination="example"}[2m])
```
**Canal instance处理transaction的TPS，以TRANSACTION_END事件为基准。**
***

##### TPS(Table row)
![Image text](https://github.com/lcybo/canal/blob/master/images/rows.PNG?raw=true)
```
rate(canal_instance_put_rows{destination="example"}[2m])
```
**对应store put操作的tableRows TPS.**
```
rate(canal_instance_get_rows{destination="example"}[2m])
```
**对应client get操作的tableRows TPS.**
```
rate(canal_instance_ack_rows{destination="example"}[2m])
```
**对应client ack操作的tableRows TPS.**
***

##### Client requests
![Image test](https://github.com/lcybo/canal/blob/master/images/reqs.PNG?raw=true)
```
canal_instance_client_packets{destination="example"}
```
**Netty server处理的client requests，以packetType为label分类统计。**
***

##### Empty packets
![Image text](https://github.com/lcybo/canal/blob/master/images/empty.PNG?raw=true)
```
rate(canal_instance_client_empty_batches{destination="example"}[2m])
```
**client get返回每秒空包量。如果正常traffic下，该值很大，考虑使用connector的timeout机制，节省资源。**
```
rate(canal_instance_client_packets{destination="example", packetType="GET"}[2m])
```
**nonempty, 作为empty rate的参照。**
***

##### Response time
![Image text](https://github.com/lcybo/canal/blob/master/images/latency.PNG?raw=true)
```
canal_instance_client_request_latency_bucket{destination="example"}
```
**Histogram, client请求响应时间统计。关于[histogram](https://prometheus.io/docs/concepts/metric_types/#histogram).**
***

##### Event store占用
![Image text](https://github.com/lcybo/canal/blob/master/images/remain_events.PNG?raw=true)
```
canal_instance_store_produce_seq{destination="example"} - canal_instance_store_consume_seq{destination="example"}
```
**Event store内未ack的events数量，实时性受scrape_interval影响。**
***

##### Event store memory占用(KB, 仅memory mode)
![Image text](https://github.com/lcybo/canal/blob/master/images/remain_mem.PNG?raw=true)
```
(canal_instance_store_produce_mem{destination="example"} - canal_instance_store_consume_mem{destination="example"}) / 1024
```
**Event store内未ack的events所占用内存大小，实时性受scrape_interval影响。**
***

##### Client QPS
![Image text](https://github.com/lcybo/canal/blob/master/images/QPS.PNG?raw=true)
```
rate(canal_instance_client_packets{destination="example",packetType="GET"}[2m])
```
GET类型QPS.
```
rate(canal_instance_client_packets{destination="example",packetType="CLIENTACK"}[2m])
```
CLIENTACK类型QPS.
***

##### 状态信息
![Image text](https://github.com/lcybo/canal/blob/master/images/instance.PNG?raw=true)
```
canal_instance{destination="example"}
canal_instance_parser_mode{destination="example"}
canal_instance_store{destination="example"}
```
**通过labels展示状态信息。**