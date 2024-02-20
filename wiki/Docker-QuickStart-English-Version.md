## Dockerfile

You can find the [Dockerfile](https://github.com/alibaba/canal/blob/master/docker/Dockerfile) here.

Note:
- Docker image is based on minimal CentOS 6.7. It needs some common tools like tar/dstat/nc/man, which will take up about 400MB.
- By default, JDK 1.8 is installed. `build.sh` will automatically download jdk and copy it to docker. JDK will take up about 400MB.
- Docker comes with a log cleaning script. When logs occupy over 80% docker space, script will be executed to clean the redundant logs.

Canal docker will be around 900MB in total. We welcome pull requests for docker optimization.

## Get Docker
### Docker pull



