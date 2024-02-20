[![build status](https://travis-ci.com/alibaba/canal.svg?branch=master)](https://travis-ci.com/alibaba/canal)
[![codecov](https://codecov.io/gh/alibaba/canal/branch/master/graph/badge.svg)](https://codecov.io/gh/alibaba/canal)
![maven](https://img.shields.io/maven-central/v/com.alibaba.otter/canal.svg)
![license](https://img.shields.io/github/license/alibaba/canal.svg)
[![average time to resolve an issue](http://isitmaintained.com/badge/resolution/alibaba/canal.svg)](http://isitmaintained.com/project/alibaba/canal "average time to resolve an issue")
[![percentage of issues still open](http://isitmaintained.com/badge/open/alibaba/canal.svg)](http://isitmaintained.com/project/alibaba/canal "percentage of issues still open")

## 简介

![](https://img-blog.csdnimg.cn/20191104101735947.png)

**canal [kə'næl]**，译意为水道/管道/沟渠，主要用途是基于 MySQL 数据库增量日志解析，提供增量数据订阅和消费

**工作原理**
- canal 模拟 MySQL slave 的交互协议，伪装自己为 MySQL slave ，向 MySQL master 发送 dump 协议
- MySQL master 收到 dump 请求，开始推送 binary log 给 slave (即 canal )
- canal 解析 binary log 对象(原始为 byte 流)

## 如何参与

- 如果您在使用 canal 过程中，发现 bug 或者有新的述求，欢迎提交 issue ，请按标准模板进行填写，以便我们快速进行定位和理解，从而解决问题或者精确实现您的诉求
- 如果您希望参与到代码开发中来，欢迎提交 pull request([如何使用 pull request?](https://help.github.com/articles/using-pull-requests))，代码开发前，请在 IDE 中导入工程中的 codeformat.xml 文件 (intellij idea 请先安装 Eclipse Code Formatter 插件，再进行设置)
- 如果 canal 帮助到了您的业务，并且觉得这个产品还不错，请多多向您的朋友、同事推荐，感谢

## 交流渠道

- 先加管理员，后拉群
<img src="https://canalopensource.oss-cn-hangzhou.aliyuncs.com/system2.jpeg" height="30%" width="30%"/>

## QuickStart 
See the page for quick start: [[QuickStart]].

## ClientExample
See the page for quick start: [[ClientExample]].

## AdminGuide
See the page for admin deploy guide : [[AdminGuide]]

## Canal-Admin WebUI

See the page for admin deploy guide : [[Canal Admin Guide]]

## 重要版本更新说明

1. canal 1.1.x 版本（[release_note](https://github.com/alibaba/canal/releases)）,性能与功能层面有较大的突破,重要提升包括:

- 整体性能测试&优化,提升了150%. #726 参考: [Performance](https://github.com/alibaba/canal/wiki/Performance)
- 原生支持prometheus监控 #765 [Prometheus QuickStart](https://github.com/alibaba/canal/wiki/Prometheus-QuickStart)
- 原生支持kafka消息投递 #695 [Canal Kafka/RocketMQ QuickStart](https://github.com/alibaba/canal/wiki/Canal-Kafka-RocketMQ-QuickStart)
- 原生支持aliyun rds的binlog订阅 (解决自动主备切换/oss binlog离线解析) 参考: [Aliyun RDS QuickStart](https://github.com/alibaba/canal/wiki/aliyun-RDS-QuickStart)
- 原生支持docker镜像 #801 参考: [Docker QuickStart](https://github.com/alibaba/canal/wiki/Docker-QuickStart)

2. canal 1.1.4版本，迎来最重要的WebUI能力，引入canal-admin工程，支持面向WebUI的canal动态管理能力，支持配置、任务、日志等在线白屏运维能力，具体文档：[[Canal Admin Guide]]

## 多语言

canal 特别设计了 client-server 模式，交互协议使用 protobuf 3.0 , client 端可采用不同语言实现不同的消费逻辑，欢迎大家提交 pull request 
  
- canal java 客户端: [https://github.com/alibaba/canal/wiki/ClientExample](https://github.com/alibaba/canal/wiki/ClientExample)
- canal c# 客户端: [https://github.com/dotnetcore/CanalSharp](https://github.com/dotnetcore/CanalSharp)
- canal go客户端: [https://github.com/CanalClient/canal-go](https://github.com/CanalClient/canal-go)
- canal Python客户端: [https://github.com/haozi3156666/canal-python](https://github.com/haozi3156666/canal-python)
  
canal 作为 MySQL binlog 增量获取和解析工具，可将变更记录投递到 MQ 系统中，比如 Kafka/RocketMQ，可以借助于 MQ 的多语言能力 

- 参考文档: [Canal Kafka/RocketMQ QuickStart](https://github.com/alibaba/canal/wiki/Canal-Kafka-RocketMQ-QuickStart)

## 版本

- 推荐版本：1.1.4
- [ReleaseNotes](http://alibaba.github.io/canal/release.html)
- 下载发布包：[download](https://github.com/alibaba/canal/releases)
- maven依赖 
 
```xml
<dependency>
    <groupId>com.alibaba.otter</groupId>
    <artifactId>canal.client</artifactId>
    <version>1.1.4</version>
</dependency>
```
