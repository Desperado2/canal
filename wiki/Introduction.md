# Introduction

**Canal** is a high performance data synchronization system based on MySQL binary log. Canal is widely used in Alibaba group (including [https://www.taobao.com](https://www.taobao.com/)) to provide reliable low latency incremental data pipeline.

**Canal Server** is capable of parsing MySQL binlog and subscribe to the data change, while **Canal Client** can be implemented to broadcast the change to anywhere, e.g. database and [Apache Kafka](https://kafka.apache.org/).

It has following features:

1. Support all platforms.
2. Support fine-grained system monitoring, powered by [Prometheus](https://prometheus.io/).
3. Support parsing and subscription to MySQL binlog by different ways, e.g. by GTID.
4. Support high performance, real-time data synchronization. (See more at [Performance](https://github.com/alibaba/canal/wiki/Performance))
5. Both Canal Server and Canal Client support HA/Scalability, powered by [Apache ZooKeeper](https://zookeeper.apache.org/)
6. Docker Supports.

## Backgrounds

In the early days, Alibaba B2B Company needed to syncrhonize servers' data between the United States and Hangzhou, China. Previous database synchronization machenism was based on the `trigger` to obtain incremental updates. Starting from 2010, Alibaba Group began to use dataset **binary log** to get the incremental updates and synchronize data across servers, which gave birth to our incremental subscription & consumption service (available now in Alibaba Cloud) and started a new era.

## How it works

### MySQL Master-Slave Replication Implementation

![img](https://camo.githubusercontent.com/eec1605862fe9e9989b97dd24f28a4bc5d7debec/687474703a2f2f646c2e69746579652e636f6d2f75706c6f61642f6174746163686d656e742f303038302f333038362f34363863316131342d653761642d333239302d396433642d3434616335303161373232372e6a7067)



Replication follows 3-step procedure:

1. The Master server records the changes to the binlog (these records are called binlog events, which can be viewed through `show binary events`)
2. The Slave server copies master's binary log events to its relay log.
3. The Slave server redo events in the relay log will subsequently update its old data.



### How Canal works

![img](https://camo.githubusercontent.com/46c626b4cde399db43b2634a7911a04aecf273a0/687474703a2f2f646c2e69746579652e636f6d2f75706c6f61642f6174746163686d656e742f303038302f333130372f63383762363762612d333934632d333038362d393537372d3964623035626530346339352e6a7067)

The principle is simple:

1. Canal simulates MySQL the slave's interaction protocol, disguises itself as mysql slave, and sends dump protocol to MySQL Master server.
2. MySQL Master received the dump request and started pushing binary log to slave (which is canal).
3. Canal parses binary log object to its own data type (originally byte stream)