---
layout:     post
title:      Ubuntu上安装Wordpress
date:       2015-05-17 14:24
#summary:    See what the different elements looks like. Your markdown has never looked better. I promise.
categories: tech
---
## 安装Apache2

### 安装
``` sudo apt-get install apache2 ```

### 验证
执行以下命令启动server，可以通过访问浏览器访问以下页面http://127.0.0.1

``` sudo /etc/init.d/apache2 restart``` 

## 安装php

### 安装

``` sudo apt-get install libapache2-mod-php5 php5```

### 验证
安装完后，重新启动Apache，让它加载PHP模块：

``` sudo /etc/init.d/apache2 restart``` 

接下来，我们就在Web目录下面新建一个test.php文件来测试PHP是否能正常的运行，命令：

``` sudo gedit /var/www/html/test.php``` 

然后输入:

```<?php echo "hello,world!!"?>``` 

接着保存文件,在浏览器里输入http://127.0.0.1/test.php，如果在网页中显示

hello,world!!

，那就说明PHP已经正常运行了。

## 安装mysql数据库

``` sudo apt-get install mysql-server mysql-client``` 

在安装的最后，它会要求设置数据库的root密码，可以不设置。

### 验证
使用以下命令进入mysql命令行

``` mysql -u root``` 

### 修改root用户名
使用以下命令修改root用户名（如果没有设置过的话）

``` mysql> GRANT ALL PRIVILEGES ON *.* TO root@localhost IDENTIFIED BY "123456";``` 

### 为wordpress创建数据库及管理员用户（如果后面安装失败报没有数据库错误可执行此步骤）

``` mysql>CREATE DATABASE wordpress；``` 

``` mysql>GRANT ALL PRIVILEGES ON wordpress.* TO wordpress_root@localhost IDENTIFIED BY "123456";``` 

创建了一个叫wordpress数据库，并创建管理员wordpress_root，密码123456

## 安装phpmyadmin-Mysql数据库管理

``` sudo apt-get install phpmyadmin``` 

安装过程做如下设置：
在安装过程中会要求选择Web server：apache2或lighttpd，使用空格键选定apache2，按tab键然后确定。
然后会要求输入设置的Mysql数据库密码连接密码。

## 下载wordpress

下载wordpress并解压到/var/www/html/下

设置/var/www目录的文件执行读写权限
``` sudo chmod 777 /var/www``` 

## 安装wordpress

如果apache2没有启动，启动apache，然后访问http://127.0.0.1/wordpress/wp-admin/install.php，按照要求一步步输入需要的信息（数据库名wordpress，数据库用户名wordpress_root、密码123456）,系统会自动创建好wp-config.php,然后启动，进入管理后台。

![管理后台](/images/wordpress_admin_page.png)

![主页](/images/wordpress_home_page.png)