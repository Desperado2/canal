# 测试环境

| 类型 | 配置 |
| --- | --- |
| MySQL A | Intel(R) Xeon(R) CPU E5-2430 0 @ 2.20GHz  (24core 96G) |
| MySQL B | Intel(R) Xeon(R) CPU E5-2430 0 @ 2.20GHz  (24core 96G)  日常业务库 |
| Canal Server | Intel(R) Xeon(R) CPU E5-2430 0 @ 2.20GHz  (24core 96G) |
| Canal Client | Intel(R) Xeon(R) CPU E5-2430 0 @ 2.20GHz  (24core 96G) |


# 测试方式

为了更加精准的描述canal的性能，会从整个流程上进行优化和分析.   
1. 整个canal流程处理binlog，分为这么5步操作(4+1)，整个优化和评测对这5步分别进行.  


![image.png | left | 747x237](https://cdn.yuque.com/lark/0/2018/png/5565/1532057042091-3ad852d4-6346-4c7d-b94b-327baba85010.png "")

2. 构造了两个测试场景，批量Insert/Update/Delete 和 普通业务DB(单条操作为主)

## 优化内容
1. 网络读取优化
    * socket receiveBuffer/sendBuffer调优
    * readBytes减少数据拷贝
    * 引入BufferedInputStream提升读取能力
2. binlog parser优化
    * 时间毫秒精度解析优化
    * 并发解析模型 (引入ringbuffer，拆分了几个阶段：网络接收、Event基本解析、DML解析和protobuf构造、加入memory store，针对瓶颈点protobuf构造采用了多线程的模式提升吞吐)
        

![image.png | left | 392x245](https://cdn.yuque.com/lark/0/2018/png/5565/1532070926759-d7c61fdc-f650-43ac-b503-80879600922d.png "")

3. 序列化和传输优化
    * 提前序列化，避免在SessionHandler里主线程里串行序列化
    * 嵌套protobuf对象序列化会产生多次byte[]数据拷贝，硬编码拉平到一个byte[]里处理，避免拷贝
    * 客户端接收数据时，做lazy解析，避免在主线程串行处理
    * 客户端ack做异步处理，避免在主线程串行处理

### 优化过程
 可参考github issue详细提交记录： [https://github.com/alibaba/canal/issues/726](https://github.com/alibaba/canal/issues/726)

# 测试数据

<div class="bi-table">
  <table>
    <colgroup>
      <col width="90px" />
      <col width="auto" />
      <col width="auto" />
      <col width="auto" />
    </colgroup>
    <tbody>
      <tr height="34px">
        <td rowspan="1" colSpan="1">
          <div data-type="p">序号</div>
        </td>
        <td rowspan="1" colSpan="1">
          <div data-type="p">阶段</div>
        </td>
        <td rowspan="1" colSpan="1">
          <div data-type="p">批量操作Insert/Update/Delete (导入业务)</div>
        </td>
        <td rowspan="1" colSpan="1">
          <div data-type="p">单条操作 (普通业务)</div>
        </td>
      </tr>
      <tr height="34px">
        <td rowspan="1" colSpan="1">
          <div data-type="p">1</div>
        </td>
        <td rowspan="1" colSpan="1">
          <div data-type="p">Binlog接收</div>
        </td>
        <td rowspan="1" colSpan="1">
          <div data-type="p">200w TPS (网络 117MB/s)</div>
        </td>
        <td rowspan="1" colSpan="1">
          <div data-type="p">71w TPS (网络 112MB/s)</div>
        </td>
      </tr>
      <tr height="34px">
        <td rowspan="1" colSpan="1">
          <div data-type="p">2</div>
        </td>
        <td rowspan="1" colSpan="1">
          <div data-type="p">Binlog Event解析</div>
        </td>
        <td rowspan="1" colSpan="1">
          <div data-type="p">200w TPS (网络 117MB/s)</div>
        </td>
        <td rowspan="1" colSpan="1">
          <div data-type="p">70w TPS (网络 110MB/s)</div>
        </td>
      </tr>
      <tr height="34px">
        <td rowspan="1" colSpan="1">
          <div data-type="p">3</div>
        </td>
        <td rowspan="1" colSpan="1">
          <div data-type="p">Insert/Update/Delete深度解析</div>
        </td>
        <td rowspan="1" colSpan="1">
          <div data-type="p">200w TPS (网络 117MB/s)</div>
        </td>
        <td rowspan="1" colSpan="1">
          <div data-type="p">65w TPS (网络 105MB/s)</div>
        </td>
      </tr>
      <tr height="34px">
        <td rowspan="1" colSpan="1">
          <div data-type="p">4</div>
        </td>
        <td rowspan="1" colSpan="1">
          <div data-type="p">生成CanalEntry (存储到memory store)</div>
        </td>
        <td rowspan="1" colSpan="1">
          <div data-type="p">130w TPS (网络 75MB/s)</div>
        </td>
        <td rowspan="1" colSpan="1">
          <div data-type="p">50w TPS (网络 90MB/s)</div>
        </td>
      </tr>
      <tr height="34px">
        <td rowspan="1" colSpan="1">
          <div data-type="p">5</div>
        </td>
        <td rowspan="1" colSpan="1">
          <div data-type="p">client接收</div>
        </td>
        <td rowspan="1" colSpan="1">
          <div data-type="p">20w TPS 1.canal server机器网络 11MB/s</div>
          <div data-type="p">2.canal client机器网络 75MB/s</div>
          <div data-type="p">
            binlog膨胀率为 1:6.8</div>
        </td>
        <td rowspan="1" colSpan="1">
          <div data-type="p">10w TPS 1.canal server网络 22MB/s</div>
          <div data-type="p">2.canal client网络 42MB/s</div>
          <div data-type="p"></div>
          <div data-type="p">binlog膨胀率为 1:1.9</div>
        </td>
      </tr>
    </tbody>
  </table>
</div>


各个阶段测试代码：
1. [FetcherPerformanceTest.java](https://github.com/alibaba/canal/blob/master/dbsync/src/test/java/com/taobao/tddl/dbsync/FetcherPerformanceTest.java) 
2. [MysqlBinlogEventPerformanceTest.java](https://github.com/alibaba/canal/blob/master/parse/src/test/java/com/alibaba/otter/canal/parse/MysqlBinlogEventPerformanceTest.java)
3. [MysqlBinlogParsePerformanceTest.java](https://github.com/alibaba/canal/blob/master/parse/src/test/java/com/alibaba/otter/canal/parse/MysqlBinlogParsePerformanceTest.java)
4. [MysqlBinlogDumpPerformanceTest.java](https://github.com/alibaba/canal/blob/master/parse/src/test/java/com/alibaba/otter/canal/parse/MysqlBinlogDumpPerformanceTest.java)
5. [SimpleCanalClientPermanceTest.java](https://github.com/alibaba/canal/blob/master/example/src/main/java/com/alibaba/otter/canal/example/SimpleCanalClientPermanceTest.java)

# 小结

   从最开始接收(跑满网络带宽)到最后client机器收到格式化的binlog数据，binlog解析的5个阶段是一个漏斗形的性能。目前整个阶段4->阶段5，性能下降比较明显主要是因为网络传输、序列化的代价影响，binlog接收为了保序采用了串行方式，所以串行里的每个代码逻辑处理都会影响最后吞吐。
   so.  如果基于canal做额外的数据扩展，比如对接到MQ系统，可以在步骤3、4阶段介入，最大化的吞吐. 

结论数据：
1. 1.0.26经过优化之后的性能，从业务binlog入库到canal client拿到数据，基本可以达到10~20w的TPS. 相比于canal 1.0.24的4w tps，提升了150%的吞吐性能.  
2. 单纯的binlog解析能力可以跑到60w ~ 200w的TPS，相当于100MB/s的解析速度