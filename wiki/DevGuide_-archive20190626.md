-------
<h1>前言</h1>
在看这篇文章之前，非常建议你先阅读一下[[Introduction]]和[[AdminGuide]]，初步了解一下canal. 

<h1>设计</h1>
<p><img src="http://dl.iteye.com/upload/attachment/0082/5187/c896f81c-b5cd-3ad6-8046-464fd38e4d6c.jpg" style="font-size: 12px; line-height: 1.5;" width="534" height="232" alt=""><br> 说明：</p>
<ul style="line-height: 1.5; color: #333333; font-family: Arial, Helvetica, FreeSans, sans-serif;">
<li>server代表一个canal运行实例，对应于一个jvm</li>
<li>instance对应于一个数据队列  （1个server对应1..n个instance)</li>
</ul>
<p>instance下的子模块：</p>
<ul style="line-height: 1.5; color: #333333; font-family: Arial, Helvetica, FreeSans, sans-serif;">
<li>eventParser (数据源接入，模拟slave协议和master进行交互，协议解析)</li>
<li>eventSink (Parser和Store链接器，进行数据过滤，加工，分发的工作)</li>
<li>eventStore (数据存储)</li>
<li>metaManager (增量订阅&amp;消费信息管理器)</li>
</ul>
<h3>整体类图设计 </h3>
<p><img src="http://dl.iteye.com/upload/attachment/0082/5192/50f52aa2-d886-33f2-a6a6-694611d869ba.jpg" width="500" height="324" alt=""></p>
<p> </p>
<p>说明：</p>
<ul>
<li>CanalLifeCycle为所有canal模块的生命周期接口</li>
<li>CanalInstance组合parser,sink,store三个子模块，三个子模块的生命周期统一受CanalInstance管理</li>
<li>CanalServer聚合了多个CanalInstance</li>
</ul>
<h3>EventParser类图设计和扩展</h3>
<p><img src="http://dl.iteye.com/upload/attachment/0082/5196/67d92e36-6ab0-33b6-8ccf-5ef3b75905d0.jpg" width="652" height="342" alt=""></p>
<p> </p>
<p>每个EventParser都会关联两个内部组件： CanalLogPositionManager ， CanalHAController  </p>
<ul>
<li>CanalLogPositionManager :  记录binlog最后一次解析成功位置信息，主要是描述下一次canal启动的位点</li>
<li>CanalHAController：控制EventParser的链接主机管理，判断当前该链接哪个mysql数据库. </li>
</ul>
<p>说明：</p>
<p>1.  目前开源版本只有支持mysql的协议(LocalBinlog就是类似于relay log的那种模式，直接根据relay log进行数据消费)</p>
<p>2.  内部版本会有OracleEventParser，获取oracle增量变更信息，因为涉及一些政治，商业和产品关系，没有随canal开源。(oracle增量解析目前为c语言开发，提供socket方式供canal接入)</p>
<p> </p>
<h4>CanalLogPositionManager类图设计</h4>
<p> <br><img src="http://dl.iteye.com/upload/attachment/0082/5202/c031db80-ced0-364f-bd01-f6a1b5d634be.jpg" width="659" height="330" alt=""></p>
<p> </p>
<p>说明： </p>
<p>1.  如果CanalEventStore选择的是内存模式，可不保留解析位置，下一次canal启动时直接依赖CanalMetaManager记录的最后一次消费成功的位点即可. (最后一次ack提交的数据位点)<br>2.  如果CanalEventStore选择的是持久化模式，可通过zookeeper记录位点信息，canal instance发生failover切换到另一台机器，可通过读取zookeeper获取位点信息.  </p>
<p> </p>
<p>可公通过实现自己的CanalLogPositionManager，比如记录位点信息到本地文件/nas文件，简单可用的无HA的模式. </p>
<p> </p>
<h4>CanalHAController类图设计</h4>
<p> <br><img src="http://dl.iteye.com/upload/attachment/0082/5224/5c0e3f97-0be9-37a4-becf-a6736b331d5b.jpg" width="151" height="191" alt=""></p>
<p>说明： </p>
<p>1. 常见的就是基于心跳语句，定时请求当前链接的数据库，超过一定次数检测失败时，尝试切换到备机.<br>2. 比如阿里内部会有一套数据库主备信息管理系统，DBA做了数据库主备切换或者机器下线，推送配置到各个应用节点，HAController收到后，控制EventParser进行链接切换. </p>
<p> </p>
<h3>EventSink类图设计和扩展</h3>
<p> <br><img src="http://dl.iteye.com/upload/attachment/0082/5236/ca78164a-9131-3fb4-ace6-b419c1ce716e.jpg" width="541" height="201" alt="">         <img src="http://dl.iteye.com/upload/attachment/0082/5240/5d676908-42a7-39f2-ac37-e7e3ffe8e917.jpg" width="192" height="252" alt=""></p>
<p>说明： </p>
<p>1.  常见的sink业务有分1:n和n:1的业务，目前GroupEventSink主要是解决n:1的归并业务</p>
<p> </p>
<p>关于1:n/n:1的介绍，可参见我的canal介绍的文章。 </p>
<p> </p>
<h3>EventStore类图设计和扩展</h3>
<p> <br><img src="http://dl.iteye.com/upload/attachment/0082/5244/07aa4089-99b1-371d-a48c-d3c5bfc0aceb.jpg" width="348" height="278" alt=""></p>
<p> </p>
<p>说明： </p>
<p>1.  抽象了CanalStoreScavenge ， 解决数据的清理，比如定时清理，满了之后清理，每次ack清理等</p>
<p>2.  CanalEventStore接口，主要包含put/get/ack/rollback的相关接口.  put/get操作会组成一个生产者/消费者模式，每个store都会有存储大小设计，存储满了，put操作会阻塞等待get获取数据，所以不会无线占用存储，比如内存大小</p>
<p>     a.  目前EventStore主要实现了memory模式，支持按照内存大小和内存记录数进行存储大小限制.  </p>
<p>     b.  后续可开发基于本地文件的存储模式</p>
<p>     c.  基于文件存储和内存存储，开发mixed模式，做成两级队列，内存buffer有空位时，将文件的数据读入到内存buffer中。</p>
<p> </p>
<p><strong>重要</strong>：实现基于mixed模式后，canal才可以说是完成真正的消费/订阅的模型  (取1份binlog数据，提供多个客户端消费，消费有快有慢，各自保留消费位点)</p>
<p> </p>
<h3>MetaManager类图设计和扩展 </h3>
<p><img src="http://dl.iteye.com/upload/attachment/0082/5255/5314b865-fd79-384e-b770-c9a22e436c52.jpg" width="447" height="326" alt=""></p>
<p> </p>
<p>说明： </p>
<p>1.  metaManager目前同样支持了多种模式，最顶层的就是memory和zookeeper的模式，还有就是mixed模式，先写内存，再写zookeeper. </p>
<p> </p>
<p>可公通过实现自己的CanalMetaManager，比如记录位点信息到本地文件/nas文件，简单可用的无HA的模式. </p>
<p> </p>
<h1>应用扩展</h1>
<p>上面介绍了相关模块的设计，这里介绍下如何将自己的扩展代码应用到canal中.  介绍之前首先需要了解instance的配置方式，可参见： <a href="https://github.com/alibaba/canal/wiki/AdminGuide">AdminGuide</a> 的spring配置这一章节</p>
<p>canal instance基于spring的管理方式，主要由两部分组成： </p>
<ol>
<li>xxx.properties</li>
<li>xxx-instance.xml </li>
</ol>
<p>xxx-instance.xml就是描述对应instance所使用的模块组件定义，比如默认的instance模块组件定义有： </p>
<ol>
<li>memory-instance.xml  (选择了memory模式的组件，速度优先，简单)</li>
<li>default-instance.xml (选择了mixed/preiodmixed模式的组件，可以提供HA的功能)</li>
<li>group-instance.xml (提供了n:1的sink模式)</li>
</ol>
<p>所以，如果要应用自己的组件，就只需要定义一份自己的instance.xml，比如custom-intance.xml</p>
<p> </p>
<pre class="xml" name="code">&lt;!-- properties --&gt;
	&lt;bean class="com.alibaba.otter.canal.instance.spring.support.PropertyPlaceholderConfigurer" lazy-init="false"&gt;
		&lt;property name="ignoreResourceNotFound" value="true" /&gt;
		&lt;property name="systemPropertiesModeName" value="SYSTEM_PROPERTIES_MODE_OVERRIDE"/&gt;&lt;!-- 允许system覆盖 --&gt;
		&lt;property name="locationNames"&gt;
			&lt;list&gt;
				&lt;value&gt;classpath:canal.properties&lt;/value&gt;
				&lt;value&gt;classpath:${canal.instance.destination:}/instance.properties&lt;/value&gt;
			&lt;/list&gt;
		&lt;/property&gt;
	&lt;/bean&gt;
	&lt;bean id="instance" class="com.alibaba.otter.canal.instance.spring.CanalInstanceWithSpring"&gt;
		&lt;property name="destination" value="${canal.instance.destination}" /&gt;
		&lt;property name="eventParser"&gt;
			&lt;ref local="eventParser" /&gt;
		&lt;/property&gt;
		&lt;property name="eventSink"&gt;
			&lt;ref local="eventSink" /&gt;
		&lt;/property&gt;
		&lt;property name="eventStore"&gt;
			&lt;ref local="eventStore" /&gt;
		&lt;/property&gt;
		&lt;property name="metaManager"&gt;
			&lt;ref local="metaManager" /&gt;
		&lt;/property&gt;
		&lt;property name="alarmHandler"&gt;
			&lt;ref local="alarmHandler" /&gt;
		&lt;/property&gt;
	&lt;/bean&gt;
......</pre>
<p> </p>
<p>instance.xml要满足一个基本元素：</p>
<p>1.  一份instance.xml中有一份或者多份instance定义，优先以destination名字查找对应的instance bean定义，如果没有，则按默认的名字“instance”查找instance对象</p>

例如 `xxxx-instance.xml`中定义 id 分别为 `instance-1`, `instance-2` 的两个bean. 
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
这两个bean将为同名的instance 提供自定义的`eventParser , evnetSink , evnetStore , metaManager，alarmHandler`.  
如果没有自定义这些bean, 就使用 id="instance" 的bean来配置canal instance.

<p>2. 一份instance bean定义，需要包含eventParser , evnetSink , evnetStore , metaManager，alarmHandler的5个模块定义，(alarmHandler主要是一些报警机制处理，因为简单没展开，可扩展)</p>
<p> </p>
<p>完成custom-instance.xml定义后，可通过canal.properties配置中进行引入：</p>
<pre class="java" name="code">canal.instance.{通道名字}.spring.xml = classpath:spring/custom-instance.xml</pre>
<p>到这里，就完成了扩展组件的应用，启动canal instance后，就会使用自定义的的组件 ,  just have fun . </p>
<p> </p>
</div>
  </div>