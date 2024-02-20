##  环境版本
* 操作系统：CentOS release 6.6 (Final)
* java版本: jdk1.8
* kafka 版本: kafka_2.11-1.1.1.tgz 

## 安装kafka
### 1. 下载压缩包, 复制到固定目录并解压
到官网下载压缩包
```
wget https://www.apache.org/dyn/closer.cgi?path=/kafka/1.1.1/kafka_2.11-1.1.1.tgz
mkdir  -p /usr/local/kafka
cp   kafka_2.11-1.1.1.tgz   /usr/local/kafka
tar -zxvf kafka_2.11-1.1.1.tgz
```

### 2 修改配置文件
`vim /usr/local/kafka/kafka_2.11-1.1.1/config/server.properties` 修改参数
```
zookeeper.connect=192.168.1.110:2181
listeners=PLAINTEXT://:9092
advertised.listeners=PLAINTEXT://192.168.1.117:9092 #本机ip
# ...
```

### 3 启动server 

* start脚本
```
# bin/kafka-server-start.sh  -daemon  config/server.properties &
```

* 查看所有topic
```
# bin/kafka-topics.sh --list --zookeeper 192.168.1.110:2181
```
* 查看指定topic 下面的数据
```
# bin/kafka-console-consumer.sh --bootstrap-server 192.168.1.117:9092  --from-beginning --topic example_t
Using the ConsoleConsumer with old consumer is deprecated and will be removed in a future major release. Consider using the new consumer by passing [bootstrap-server] instead of [zookeeper].
```

# 参考链接

[https://kafka.apache.org/quickstart](https://kafka.apache.org/quickstart)
