---
layout:     post
title:      一个不起眼的Sonar问题引发的血案
date:       2015-07-29 14:30
#summary:    
categories: tech
---

## 前传
在jdk1.7之前，java.sql的Connection、ResultSet、Statement没有统一的close()接口。
想要统一对它们进行关闭，需要采用一种略微复杂的方法：

自已写一个DBResource，然后通过反射获取传入对象的close()方法，然后进行调用，关闭资源。

[DBResource](/images/a_tale_of_sonar_issue/DBResource.java)
[DBResourceTest](/images/a_tale_of_sonar_issue/DBResourceTest.java)

关键代码

![关键代码](/images/a_tale_of_sonar_issue/Image.png)

## SonarQube问题
上面这段代码，SonarQube检查报告提示第53行，应该对异常进行日志记录。

仔细考虑异常处理后，认为传入的resource对象不应该出现没有close()的情况，如果出现了，一定是编码的bug，因此果断将代码改成以下处理方式：对于无法找到close()时抛出的NoSuchMethodException转义成IllegalStateException继续抛出，如下：

![Image1](/images/a_tale_of_sonar_issue/Image[1].png)