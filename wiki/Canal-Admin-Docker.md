# 参考资料

* Canal Admin QuickStart : [[Canal Admin QuickStart]]
* Canal Admin Guide :  [[Canal Admin Guide]]
* Canal Admin ServerGuide :  [[Canal Admin ServerGuide]]

# Dockerfile

canal-admin Dockerfile文件：[https://github.com/alibaba/canal/blob/master/docker/Dockerfile_admin](https://github.com/alibaba/canal/blob/master/docker/Dockerfile_admin)
注意点：
* 和canal-server共享了基础镜像，可参考: [[Docker QuickStart]]

# 获取Docker

## 远程拉取

1. 访问docker hub获取最新的版本
访问：https://hub.docker.com/r/canal/canal-admin/tags/

2. 下载对应的版本，比如最新版为1.1.5
```plain
docker pull canal/canal-admin:v1.1.5
```

## 本地编译

```plain
git clone git@github.com:alibaba/canal.git
cd canal/docker && sh build.sh admin
```

# 启动Docker
docker目录下自带了一个run_admin.sh脚本:  [https://github.com/alibaba/canal/blob/master/docker/run_admin.sh](https://github.com/alibaba/canal/blob/master/docker/run_admin.sh)
```plain
Usage:
  run_admin.sh [CONFIG]
example :
  run_admin.sh -e server.port=8089 \
         -e canal.adminUser=admin \
         -e canal.adminPasswd=admin
```


实际运行的例子：
```plain
# 下载脚本
wget https://raw.githubusercontent.com/alibaba/canal/master/docker/run_admin.sh 

# 以8089端口启动canal-admin
sh  run_admin.sh -e server.port=8089 \
         -e canal.adminUser=admin \
         -e canal.adminPasswd=admin

# 指定外部的mysql作为admin的库
sh  run_admin.sh -e server.port=8089 \
         -e spring.datasource.address=xxx \
         -e spring.datasource.database=xx \
         -e spring.datasource.username=xx 
         -e spring.datasource.password=xx
```


注意点：
* -e参数里可以指定以前application.yml里所有配置的key和value，springboot启动时会读取-e指定的变量

## 运行效果

![](http://dl2.iteye.com/upload/attachment/0132/2337/5d49eaac-92d4-3684-b142-2aa897022531.png)

看到successful之后，就代表canal-admin启动成功，可以访问 http://127.0.0.1:8089/ ，默认账号密码: admin/123456

# 配套启动Canal-Server Docker

首先请参考：Canal-Server的Docker启动方式 [[Docker-QuickStart]]
```plain
# 下载脚本
wget https://raw.githubusercontent.com/alibaba/canal/master/docker/run.sh 

# 以单机模式启动
run.sh -e canal.admin.manager=127.0.0.1:8089 \
         -e canal.admin.port=11110 \
         -e canal.admin.user=admin \
         -e canal.admin.passwd=4ACFE3202A5FF5CF467898FC58AAB1D615029441

# 自动加入test集群
run.sh -e canal.admin.manager=127.0.0.1:8089 \
         -e canal.admin.port=11110 \
         -e canal.admin.user=admin \
         -e canal.admin.passwd=4ACFE3202A5FF5CF467898FC58AAB1D615029441 
         -e canal.admin.register.cluster=test
```
注意点：
1. canal.admin.manager 代表需要链接的canal-admin地址
2. canal.admin.user/passwd/port 请参考canal-admin的配置指导文档
3. canal.admin.register.cluster 表示默认注册的集群

看到successful之后，就代表canal-server启动成功，然后就可以在canal-admin上进行任务分配了