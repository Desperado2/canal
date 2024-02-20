# 背景

<span data-type="color" style="color:rgb(38, 38, 38)"><span data-type="background" style="background-color:rgb(255, 255, 255)">可参考github issue描述： </span></span>[https://github.com/alibaba/canal/issues/727](https://github.com/alibaba/canal/issues/727)

如果用户使用binlog解析工具，链接aliyun RDS需要解决几个方面的问题：

1. 账号权限问题
    1. aliyun RDS早期控制台创建的账号，默认没有binlog dump需要的权限，目前创建的账号默认自带了权限，不需要做任何额外的处理，是否包含必须的权限，也可以直接查询show grants
    

![image.png | left | 494x111](https://cdn.nlark.com/lark/0/2018/png/5565/1534140544510-74295b15-79cb-463f-9af2-c84e46831c82.png "")

2. binlog被删除的问题
    1. aliyun RDS有自己的binlog日志清理策略，这个策略相比于用户自建mysql会更加激进，默认应该是18小时就会清理binlog并上传到oss上，可以在页面上进行调整，或者业务可以通过oss下载更早的binlog
        

![image.png | left | 377x376](https://cdn.nlark.com/lark/0/2018/png/5565/1534140664725-58e6ab67-5b28-4d2c-b8ba-1a59109521b7.png "")

3. 主备切换导致的问题
    1. 一般云MySQL的主备方案都采用了vip模式，屏蔽了后端物理节点之间的主备切换，所以对于binlog dump来说你不知道连的是哪个后端mysql节点，需要自适应后端的主备切换过程

以上3条，基本对于所有的云模式的MySQL都适用，都需要binlog dump的程序进行支持或者适应.  目前canal 1.1.x版本之后都比较好的支持了aliyun RDS的binlog dump. 

# 使用

```plain
vi conf/example/instance.properties
```

关注参数：

<div class="bi-table">
  <table>
    <colgroup>
      <col width="auto" />
      <col width="auto" />
      <col width="auto" />
    </colgroup>
    <tbody>
      <tr height="34px">
        <td rowspan="1" colSpan="1">
          <div data-type="p">参数名字</div>
        </td>
        <td rowspan="1" colSpan="1">
          <div data-type="p">参数说明</div>
        </td>
        <td rowspan="1" colSpan="1">
          <div data-type="p">默认值</div>
        </td>
      </tr>
      <tr height="34px">
        <td rowspan="1" colSpan="1">
          <div data-type="p">canal.aliyun.accessKey</div>
        </td>
        <td rowspan="1" colSpan="1">
          <div data-type="p">aliyun账号的ak信息 (如果不需要在本地binlog超过18小时被清理后自动下载oss上的binlog，可以忽略该值)。注意：如果是1.1.0的老版本对应的参数名为canal.instance.rds.accesskey
          </div>
        </td>
        <td rowspan="1" colSpan="1">
          <div data-type="p">无</div>
        </td>
      </tr>
      <tr height="34px">
        <td rowspan="1" colSpan="1">
          <div data-type="p">canal.aliyun.secretKey</div>
        </td>
        <td rowspan="1" colSpan="1">
          <div data-type="p">aliyun账号的sk信息</div>
          <div data-type="p">(如果不需要在本地binlog超过18小时被清理后自动下载oss上的binlog，可以忽略该值。注意：如果是1.1.0的老版本对应的参数名为canal.instance.rds.secretkey)</div>
        </td>
        <td rowspan="1" colSpan="1">
          <div data-type="p">无</div>
        </td>
      </tr>
      <tr height="34px">
        <td rowspan="1" colSpan="1">
          <div data-type="p">canal.instance.rds.instanceId</div>
        </td>
        <td rowspan="1" colSpan="1">
          <div data-type="p">aliyun rds对应的实例id信息</div>
          <div data-type="p">(如果不需要在本地binlog超过18小时被清理后自动下载oss上的binlog，可以忽略该值)</div>
        </td>
        <td rowspan="1" colSpan="1">
          <div data-type="p">无</div>
        </td>
      </tr>
    </tbody>
  </table>
</div>

实际例子:
```plain
#################################################
#enable gtid use true/false
canal.instance.gtidon=false

#position info
canal.instance.master.address=127.0.0.1:3306
canal.instance.master.journal.name=
canal.instance.master.position=
canal.instance.master.timestamp=
canal.instance.master.gtid=

#rds oss binlog
canal.instance.rds.accesskey=2zA12sL23cX230pS
canal.instance.rds.secretkey=iEl12120i341jv326ud23reULeIOSG
canal.instance.rds.instanceId=rm-bp1u38330lqe7989f

#table meta tsdb info
canal.instance.tsdb.enable=true

#username/password
canal.instance.dbUsername=canal
canal.instance.dbPassword=canal
canal.instance.connectionCharset=UTF-8

# table regex
canal.instance.filter.regex=.*\\..*
# table black regex
canal.instance.filter.black.regex=
#################################################
```

注意点：
1. 相比于普通的mysql配置，多了rds oss binlog所需要的aliyun ak/sk/实例id等相关信息(如果不需要在本地binlog超过18小时被清理后自动下载oss上的binlog，可以忽略该值)