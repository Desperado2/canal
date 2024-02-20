# 参考资料

* QuickStart : [https://github.com/alibaba/canal/wiki/QuickStart](https://github.com/alibaba/canal/wiki/QuickStart)
* AdminGuide :  [https://github.com/alibaba/canal/wiki/AdminGuide](https://github.com/alibaba/canal/wiki/AdminGuide)

# Dockerfile

Dockerfile文件：[https://github.com/alibaba/canal/blob/master/docker/Dockerfile](https://github.com/alibaba/canal/blob/master/docker/Dockerfile)
注意点：
* 基于centos6.7最小镜像进行构建，安装一些必须的工具，比如tar/dstat/nc/man等，大概400MB
* 默认安装jdk 1.8，build.sh脚本里会自动下载jdk然后copy到docker里，大概400MB
* 自带日志清理脚本，会识别硬盘超过80%时，自动清理

因此，canal整个docker镜像在900MB左右，有一定的优化空间，比如使用jre、减少一些非必须的命令等

# 获取Docker

## 远程拉取

1. 访问docker hub获取最新的版本
访问：https://hub.docker.com/r/canal/canal-server/tags/

2. 下载对应的版本，比如最新版为1.1.1
```plain
docker pull canal/canal-server:v1.1.1
```

## 本地编译

```plain
git clone git@github.com:alibaba/canal.git
cd canal/docker && sh build.sh
```

# 启动Docker

docker目录下自带了一个run.sh脚本:  [https://github.com/alibaba/canal/blob/master/docker/run.sh](https://github.com/alibaba/canal/blob/master/docker/run.sh)
```plain
sh run.sh 
Usage:
  run.sh [CONFIG]
example:
  run.sh -e canal.instance.master.address=127.0.0.1:3306 \
         -e canal.instance.dbUsername=canal \
         -e canal.instance.dbPassword=canal \
         -e canal.instance.connectionCharset=UTF-8 \
         -e canal.instance.tsdb.enable=true \
         -e canal.instance.gtidon=false \
         -e canal.instance.filter.regex=.*\\..* 
```


实际运行的例子：
```plain
# 下载脚本
wget https://raw.githubusercontent.com/alibaba/canal/master/docker/run.sh 

# 构建一个destination name为test的队列
sh run.sh -e canal.auto.scan=false \
		  -e canal.destinations=test \
		  -e canal.instance.master.address=127.0.0.1:3306  \
		  -e canal.instance.dbUsername=canal  \
		  -e canal.instance.dbPassword=canal  \
		  -e canal.instance.connectionCharset=UTF-8 \
		  -e canal.instance.tsdb.enable=true \
		  -e canal.instance.gtidon=false  \
```

注意点：
* -e参数里可以指定以前canal.properties/instance.properties里的所有配置的key和value，canal-server启动时会有限读取-e指定的变量，具体可参考[AdminGuide](AdminGuide)
* docker模式下，单docker实例只能运行一个instance，主要为配置问题。如果需要运行多instance时，可以自行制作一份docker镜像即可

## 运行效果


![image.png | left | 747x332](https://cdn.nlark.com/lark/0/2018/png/5565/1533349043367-4b756659-2a9b-440f-8969-c27f21a1bfbf.png "")


看到successful之后，就代表canal-server启动成功，可以启动canal-client链接上来进行binlog订阅了
