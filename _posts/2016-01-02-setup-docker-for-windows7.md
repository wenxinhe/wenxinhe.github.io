---
layout:     post
title:      Windows7上安装docker的坑
date:       2016-01-02
summary:    
categories: tech
---

Windows7上安装docker主要参考官方文档：

[Install Docker for Windows](https://docs.docker.com/windows/step_one/)

但是过程破费周折，主要遇到以下几个问题：

* Docker Toolbox只支持64位操作系统
* Docker启动下载boot2docker.iso失败
* Docker启动虚机失败： **"Failed to open a session for the virtual machine，Unable to load R3 module xxxx/VBoxDD.DLL(VBoxDD)，GetLastError=126，(VERR_MODULE_NOT_FOUND)"**
* Docker启动虚机失败： **"Error relaunching VirtualBox VM process: 5"**

### Docker Toolbox只支持64位操作系统

Docker Toolbox只支持64位操作系统，如果你是32位系统，只能下载[64位windows7](http://msft.digitalrivercontent.net/win/X17-24395.iso)，[U盘重装](http://jingyan.baidu.com/article/b0b63dbfd3c38d4a4830703f.html)，或者[ghost重放一个windows7](http://www.windows7en.com/Win7/20244.html)，但是ghost出的非纯净版windows7会有其他问题，后面会提到。所以还是建议安装一个纯净版windows7。

### Docker启动下载boot2docker.iso失败

Docker Toolbox安装好只是“万里长征”的第一步。启动Docker Quickstart Terminal会遇到第一个问题：boot2docker.iso在S3上下载不了。照例翻墙下载，我下载的boot2docker.iso已经放到[github](https://github.com/wenxinhe/DevToolsForWindows/raw/master/docker/boot2docker.iso)上，[官方版本v1.9.1](https://github.com/boot2docker/boot2docker/releases/download/v1.9.1/boot2docker.iso)。手动下载的boot2docker放到${DOCKER_TOOLBOX_INSTALL}/下，docker启动时会使用。

###　Docker启动虚机失败： **"Failed to open a session for the virtual machine，Unable to load R3 module xxxx/VBoxDD.DLL(VBoxDD)

![报错信息](/images/VirtualBox - Unable to load R3 module.png)

这个问题最坑爹，就是因为前面使用了非纯净版的windows7_x64，uxtheme.dll被修改，所以virtualbox加载dll时校验没有通过，需要将修改后的dll还原为原版dll。我已经把原版的dll上传到[github](https://github.com/wenxinhe/DevToolsForWindows/raw/master/docker/theme_win7_x64.zip)，按照[说明](http://blog.sina.com.cn/s/blog_4dc988240102vj8a.html)恢复为原版dll即可。

此问题的官方bug记录:[Ticket #13504](https://www.virtualbox.org/ticket/13504)

### Docker启动虚机失败： **"Error relaunching VirtualBox VM process: 5"**

![报错信息](/images/VirtualBox - Error relaunching VirtualBox VM process 5.png)

这个问题也很坑爹，virtualbox受杀毒软件的影响无法启动虚机，需要还原为4.3.12版本。Docker Toolbox自带的virtualbox都存在这个bug，需要手动安装4.3.12版本。手动安装virtualbox的路径需要与安装docker toolbox时携带安装的virtualbox的地址一致，这样docker才可以识别调用到，一般地址为C:\Program Files\Oracle\VirtualBox，也可以通过系统环境变量${VBOX_INSTALL_PATH}改变docker寻找virtualbox的路径。

此问题的官方bug记录:[Ticket #14269](https://www.virtualbox.org/ticket/14269)

### Tools

* [theme_win7_x64.zip](https://github.com/wenxinhe/DevToolsForWindows/raw/master/docker/theme_win7_x64.zip)： 还原导致virtualbox启动虚拟失败的dll
* [Sigcheck.zip](https://github.com/wenxinhe/DevToolsForWindows/raw/master/docker/Sigcheck.zip)： microsoft的sigcheck工具，检查dll是否被修改了。http://technet.microsoft.com/en-us/sysinternals/bb897441.aspx

运行命令
sigcheck -i -a -h c:\windows\system32\uxtheme.dll

显示结果第一行是 Verified:Unigned 说明是破解的
显示结果第一行是 Verified:Signed 说明是原版的