---
layout:     post
title:      搭建私有cyber-dojo服务器
date:       2015-12-16
summary:    
categories: tech
---

本文参考：

1. [setting up your own turnkey cyber-dojo server](http://blog.cyber-dojo.org/2014/09/setting-up-your-own-cyber-dojo-server.html)
2. [Detailed instructions to set up cyber-dojo server](http://coderetreat.org/group/facilitators/forum/topics/detailed-instructions-to-set-up-cyber-dojo-server)

## 下载虚机镜像文件

cyber-dojo官网要求服务器安装在TurnKey Linux Rails镜像上，因此需要先下载[镜像](https://www.turnkeylinux.org/rails)，我使用的是[TurnKey Rails version (14.0) ova格式](https://www.turnkeylinux.org/download?file=turnkey-rails-14.0-jessie-amd64.ova)

## 导入镜像创建虚机

下载完成后使用virtualbox导入ova文件，进入TurnKey Linux后系统会要求设置一些简单的配置，可以参考上面第二篇文章。

需要注意的是：网络设置根据需要选择DHCP或者自己手动设置Static IP。

## 安装cyber-dojo

虚机安装好后，进入系统，安装cyber-dojo。

安装步骤基本可以参考第一篇文章，过程如下：

```
# install the cyber-dojo github repo
$ cd /var/www 
$ git clone https://github.com/JonJagger/cyber-dojo.git

# install cyber-dojo as the default rails server, the necessary gems, and docker
$ cd /var/www/cyber-dojo/admin_scripts 
$ ./pull.sh 
$ ./setup_turnkey_server.sh

# install pre-built cyber-dojo docker-containers
$ cd /var/www/cyber-dojo/languages 
$ ./docker_list_all_images.rb
$ docker pull <repository> 
# e.g. 
$ docker pull docker pull cyberdojofoundation/java_mockito

# refresh the caches
$ cd /var/www/cyber-dojo/admin_scripts 
$ ./refresh_all_caches.sh 

# start your server
$ service apache2 restart
```

## 注意事项
因为cyber-dojo的代码一直在变化，虚机镜像版本也在变化，因此有几点操作和上面的安装文档不太一样，需要特别注意（我安装时费了很大的功夫来趟这些坑。。）。

首先，我使用的cyber-dojo的commit是dc593321b4ab0d505bcdf00439f6ef7aeb043e43，TurnKey Linux Rails的版本是14.0 jessie。

以下几点只是针对上面两个版本的注意事项，其他版本可能没这个问题，或者有其他问题。

### apt-transport-https问题

TurnKey Linux Rails 14.0镜像缺少apt-transport-https包，因此无法使用apt-get，需要在安装cyber-dojo之前先把apt-transport-https装上。

TurnKey Linux Rails 14.0基于Debian jessie发行版，所以从以下网址下载两个deb包安装上:

[apt-transport-https](https://packages.debian.org/zh-cn/jessie/apt-transport-https)及其依赖[libapt-pkg4.12](https://packages.debian.org/zh-cn/jessie/libapt-pkg4.12)

下载时，自行选择amd64还是其他版本，拷贝到虚机中（可以使用SCP）进行安装：

```
dpkg -i apt-transport-https_1.0.9.8.1_amd64.deb libapt-pkg4.12_1.0.9.8.1_amd64.deb
```

安装后通过apt-get update验证，如果能够正常更新软件源说明安装成功。

### 下载docker语言镜像后的刷新问题

在我安装的这个版本中，admin_scripts/refresh_all_caches.sh已经不存在了。

我是直接使用caches/refresh_all.sh进行docker镜像刷新的，否则在web页面create your practice session时看不到选择语言和工具的页面只能看到一堆动物头像。

### bundle install执行失败问题

在运行admin_scripts/pull.sh时会使用bundle install安装ruby的gem，但是源服务器是国外的，不稳定，我在执行前将cyber-dojo/Gemfile中定义的源换为taobao的源：

```
#source 'https://rubygems.org'
source 'https://ruby.taobao.org'
```

### pull.sh执行不能出错

admin_scripts/pull.sh执行不能出错，它会更新一些配置，并安装gem，出错就要解决，否则后面会有问题。

### 下载docker-machine失败

admin_scripts/setup_turnkey_server.sh中一个命令会下载docker-machine:

curl -L https://github.com/docker/machine/releases/download/v0.4.0/docker-machine_linux-amd64 > /usr/local/bin/docker-machine

这个下载可能会超时，因为文件在C3服务器上，如果失败先忽略，看后面能否正常安装，如果能够正常安装并运行可以忽略这个问题。



