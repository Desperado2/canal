## 概述

本文主要介绍 canal 产品整体设计，帮助你快速地熟悉代码脉络。如果你对 canal 还一无所知，只是知道他能进行数据迁移同步，那么建议先行阅读 [[Introduction]] 和 [[AdminGuide]] 两篇文档了解。

## 产品设计

![](http://dl.iteye.com/upload/attachment/0082/5187/c896f81c-b5cd-3ad6-8046-464fd38e4d6c.jpg)

- server 代表一个 canal 运行实例，对应于一个 jvm
- instance 对应于一个数据队列 （1个 canal server 对应 1..n 个 instance )

- instance 下的子模块
  - eventParser: 数据源接入，模拟 slave 协议和 master 进行交互，协议解析
  - eventSink: Parser 和 Store 链接器，进行数据过滤，加工，分发的工作
  - eventStore: 数据存储
  - metaManager: 增量订阅 & 消费信息管理器

### 整体类图设计

![](http://dl.iteye.com/upload/attachment/0082/5192/50f52aa2-d886-33f2-a6a6-694611d869ba.jpg)

- CanalLifeCycle: 所有 canal 模块的生命周期接口
- CanalInstance: 组合 parser,sink,store 三个子模块，三个子模块的生命周期统一受 CanalInstance 管理
- CanalServer: 聚合了多个 CanalInstance

#### EventParser 设计

![](http://dl.iteye.com/upload/attachment/0082/5196/67d92e36-6ab0-33b6-8ccf-5ef3b75905d0.jpg)

- 每个EventParser都会关联两个内部组件
  - CanalLogPositionManager :  记录binlog 最后一次解析成功位置信息，主要是描述下一次canal启动的位点
  - CanalHAController: 控制 EventParser 的链接主机管理，判断当前该链接哪个mysql数据库

- 目前开源版本只支持 MySQL binlog , 默认通过 MySQL binlog dump 远程获取 binlog ,但也可以使用 LocalBinlog - 类 relay log 模式，直接消费本地文件中的 binlog

#### CanalLogPositionManager 设计

![](http://dl.iteye.com/upload/attachment/0082/5202/c031db80-ced0-364f-bd01-f6a1b5d634be.jpg)

- 如果 CanalEventStore 选择的是内存模式，可不保留解析位置，下一次 canal 启动时直接依赖 CanalMetaManager 记录的最后一次消费成功的位点即可. (最后一次ack提交的数据位点)
- 如果 CanalEventStore 选择的是持久化模式，可通过 zookeeper 记录位点信息，canal instance 发生 failover 切换到另一台机器，可通过读取 zookeeper 获取位点信息
- 可通过实现自己的 CanalLogPositionManager，比如记录位点信息到本地文件/nas 文件实现简单可用的无 HA 模式

#### CanalHAController类图设计
![](http://dl.iteye.com/upload/attachment/0082/5224/5c0e3f97-0be9-37a4-becf-a6736b331d5b.jpg)

- 失败检测常见方式可定时发送心跳语句到当前链接的数据库，超过一定次数检测失败时，尝试切换到备机
- 如果有一套数据库主备信息管理系统，当数据库主备切换或者机器下线，推送配置到各个应用节点，HAController 收到后，控制 EventParser 进行链接切换

#### EventSink类图设计和扩展

![](http://dl.iteye.com/upload/attachment/0082/5236/ca78164a-9131-3fb4-ace6-b419c1ce716e.jpg)

![](http://dl.iteye.com/upload/attachment/0082/5240/5d676908-42a7-39f2-ac37-e7e3ffe8e917.jpg)

- 常见的 sink 业务有 1:n 和 n:1 形式，目前 GroupEventSink 主要是解决 n:1 的归并类业务

### EventStore类图设计和扩展
![](http://dl.iteye.com/upload/attachment/0082/5244/07aa4089-99b1-371d-a48c-d3c5bfc0aceb.jpg)

- 抽象 CanalStoreScavenge ， 解决数据的清理，比如定时清理，满了之后清理，每次 ack 清理等
- CanalEventStore 接口，主要包含 put/get/ack/rollback 的相关接口.  put/get 操作会组成一个生产者/消费者模式，每个 store 都会有存储大小设计，存储满了，put 操作会阻塞等待 get 获取数据，所以不会无线占用存储，比如内存大小
  - EventStore 目前实现了 memory 模式，支持按照内存大小和内存记录数进行存储大小限制
  - 后续可开发基于本地文件的存储模式
  - 基于文件存储和内存存储，开发 mixed 模式，做成两级队列，内存 buffer 有空位时，将文件的数据读入到内存 buffer 中
  - mixed 模式实现可以让 canal 落地消费/订阅的模型,取 1 份binlog数据，提供多个客户端消费，消费有快有慢，各自保留消费位点

#### MetaManager类图设计和扩展

![](http://dl.iteye.com/upload/attachment/0082/5255/5314b865-fd79-384e-b770-c9a22e436c52.jpg)

- metaManager 目前支持了多种模式，最顶层 memory 和 zookeeper 模式，然后是 mixed 模式-先写内存，再写zookeeper
- 可通过实现自己的 CanalMetaManager，比如记录位点信息到本地文件/nas文件，简单可用的无 HA 模式

## 应用扩展

上面介绍了相关模块的设计，这里介绍下如何将自己的扩展代码应用到canal中.  介绍之前首先需要了解instance的配置方式，可参见： [AdminGuide](https://github.com/alibaba/canal/wiki/AdminGuide) 的 Spring 配置这一章节

- canal instance 基于 spring 的管理方式，主要由两部分组成
  - xxx.properties
  - xxx-instance.xml

- xxx-instance.xml 描述对应 instance 所使用的模块组件定义，默认的 instance 模块组件定义有
  - memory-instance.xml:选择 memory 模式的组件，速度优先，简单
  - default-instance.xml:选择 mixed/preiodmixed 模式的组件，可以提供 HA 的功能
  - group-instance.xml:提供 n:1 的 sink 模式
  
如果要应用自己的组件，就只需要定义一份自己的 instance.xml，比如 custom-intance.xml

示例:

```xml
<bean class="com.alibaba.otter.canal.instance.spring.support.PropertyPlaceholderConfigurer" lazy-init="false">
    <property name="ignoreResourceNotFound" value="true" />
    <property name="systemPropertiesModeName" value="SYSTEM_PROPERTIES_MODE_OVERRIDE"/><!-- 允许system覆盖 -->
    <property name="locationNames">
        <list>
	    <value>classpath:canal.properties</value>
	    <value>classpath:${canal.instance.destination:}/instance.properties</value> 
        </list>
     </property>
</bean>

<bean id="instance" class="com.alibaba.otter.canal.instance.spring.CanalInstanceWithSpring">
    <property name="destination" value="${canal.instance.destination}" />
    <property name="eventParser">
	<ref local="eventParser" />
    </property>
    <property name="eventSink">
	<ref local="eventSink" />
    </property>
    <property name="eventStore">
        <ref local="eventStore" />
    </property>
    <property name="metaManager">
	<ref local="metaManager" />
    </property>
    <property name="alarmHandler">
	<ref local="alarmHandler" />
    </property>
</bean>
......
```

- 一份 instance.xml 中有一份或者多份 instance 定义，优先以 destination 名字查找对应的 instance bean 定义，如果没有，则按默认的名字 “instance” 查找 instance 对象,例如 `xxxx-instance.xml` 中定义 id 分别为 `instance-1`, `instance-2` 的两个 bean. 这两个 bean 将为同名的 instance 提供自定义的 `eventParser , evnetSink , evnetStore , metaManager，alarmHandler`.如果没有自定义这些 bean, 就使用 id="instance" 的 bean 来配置 canal instance.
- 一份 instance bean 定义，需要包含 eventParser , evnetSink , evnetStore , metaManager，alarmHandler 的5个模块定义，( alarmHandler 主要是一些报警机制处理，因为简单没展开，可扩展)

```xml
<bean id="instance-1" class="com.alibaba.otter.canal.instance.spring.CanalInstanceWithSpring">
  <property name="destination" value="${canal.instance.destination}" />
  <property name="eventParser">
  	<ref local="eventParser" />
  </property>
  ......
</bean>
<bean id="instance-2" class="com.alibaba.otter.canal.instance.spring.CanalInstanceWithSpring">
  <property name="destination" value="${canal.instance.destination}" />
  .......
</bean>
```

完成 custom-instance.xml 定义后，可通过 canal.properties 配置中进行引入

```xml
<pre class="java" name="code">canal.instance.{通道名字}.spring.xml = classpath:spring/custom-instance.xml
```

到这里，就完成了扩展组件的应用，启动canal instance后，就会使用自定义的的组件 ,  just have fun . 