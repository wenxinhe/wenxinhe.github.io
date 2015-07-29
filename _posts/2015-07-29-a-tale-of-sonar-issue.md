---
layout:     post
title:      一个不起眼的Sonar问题引发的血案
date:       2015-07-29 14:30
#summary:    
categories: tech
---

### 前传
在jdk1.7之前，java.sql的Connection、ResultSet、Statement没有统一的close()接口。
想要统一对它们进行关闭，需要采用一种略微复杂的方法：

自已写一个DBResource，然后通过反射获取传入对象的close()方法，然后进行调用，关闭资源。

[DBResource](/images/a_tale_of_sonar_issue/DBResource.java)
[DBResourceTest](/images/a_tale_of_sonar_issue/DBResourceTest.java)

关键代码

[![](/images/a_tale_of_sonar_issue/Image.png)](/images/a_tale_of_sonar_issue/Image.png)

### SonarQube问题
上面这段代码，SonarQube检查报告提示第53行，应该对异常进行日志记录。

仔细考虑异常处理后，认为传入的resource对象不应该出现没有close()的情况，如果出现了，一定是编码的bug，因此果断将代码改成以下处理方式：对于无法找到close()时抛出的NoSuchMethodException转义成IllegalStateException继续抛出，如下：

[![](/images/a_tale_of_sonar_issue/Image [1].png)](/images/a_tale_of_sonar_issue/Image [1].png)

并增加找不到close()抛出异常的场景的测试用例：

[![](/images/a_tale_of_sonar_issue/Image [2].png)](/images/a_tale_of_sonar_issue/Image [2].png)

运行所有测试用例，OK，全部通过。

[![](/images/a_tale_of_sonar_issue/Image [3].png)](/images/a_tale_of_sonar_issue/Image [3].png)

运行了一遍Sonar本地检查，报告显示问题解决，OK，提交代码，喝咖啡。

### 第一起血案： NoSuchMethodException

喝了几天咖啡后，有其他开发人员报告服务器进程无法启动，抛以下异常：

[![](/images/a_tale_of_sonar_issue/Image [4].png)](/images/a_tale_of_sonar_issue/Image [4].png)

异常显示，在DBResources.getCloseMethod()获取传入的com.mysql.jdbc.JDBC4ResultSet的close()时，获取不到close()。

怎么会获取不到JDBC4ResultSet的close()呢？所有传入的resource都应该有close()才对，没有close()的对象是不允许被传入的。

这是由DBResource的3个构造工厂保证：

[![](/images/a_tale_of_sonar_issue/Image [5].png)](/images/a_tale_of_sonar_issue/Image [5].png)

（Connection、Statement、ResultSet三个接口都有close()）

难道有人非法调用了DBResource的构造器？

[![](/images/a_tale_of_sonar_issue/Image [6].png)](/images/a_tale_of_sonar_issue/Image [6].png)

搜索了一下，也没有任何其他代码直接引用该构造器。

所有的测试用例都是通过的，在每个通过的测试用例中，resource的close()都能够正确获取并关闭。

[![](/images/a_tale_of_sonar_issue/Image [7].png)](/images/a_tale_of_sonar_issue/Image [7].png)

为什么唯独JDBC4ResultSet的close()获取不到？难道JDBC4ResultSet没有close()？

于是，查看JDBC4ResultSet的源代码，果然，JDBC4ResultSet真的没有声明close()，它的close()继承自父类。而DBResources.getCloseMethod()使用Class.getDeclaredMethod()寻找close()，但getDeclaredMethod()只能获取到类直接声明的方法，无法反射出存在于父类中的方法。

问题找到了，代码bug：没有考虑close()从父类继承的场景。

于是，立即增加测试用例，将close()从父类继承的场景模拟出来：

[![](/images/a_tale_of_sonar_issue/Image [8].png)](/images/a_tale_of_sonar_issue/Image [8].png)

[![](/images/a_tale_of_sonar_issue/Image [9].png)](/images/a_tale_of_sonar_issue/Image [9].png)

再运行测试用例，OK，新增用例失败，且报错信息与环境中的错误信息一致，说明环境中问题被测试用例复现出来了。

[![](/images/a_tale_of_sonar_issue/Image [10].png)](/images/a_tale_of_sonar_issue/Image [10].png)

[![](/images/a_tale_of_sonar_issue/Image [11].png)](/images/a_tale_of_sonar_issue/Image [11].png)

然后下面就是修改生产代码，让新增测试用例通过即可：

[![](/images/a_tale_of_sonar_issue/Image [12].png)](/images/a_tale_of_sonar_issue/Image [12].png)

只需将Class.getDeclaredMethod()改为Class.getMethod()即可获取从父类中继承的方法。

再次运行测试用例，OK，全部通过。

[![](/images/a_tale_of_sonar_issue/Image [13].png)](/images/a_tale_of_sonar_issue/Image [13].png)

提交变更到SVN，然后开始继续喝咖啡。

小结一下，通过这个案例我们学习到了：

1. 如何正确的修复bug。修复bug要做的第一件事情是添加一个复现bug的测试场景，然后修复bug，让测试场景通过。 
2. 善用工具暴露bug。Sonar问题不要轻视，背后可能隐藏着大bug。
3. 异常处理需谨慎。应该视情况妥善处理异常，否则会隐藏bug：
	* 不该出现异常的地方出现了异常（如上例中的NoSuchMethodException），应该果断抛出，因为可能是编码bug引起的。
	* 这种异常抛出，最好能直接引起某种较严重的后果，比如系统宕掉、服务不可用。原因是，这样能够引起足够的重视，便于提早暴露问题、进行修复。如果只是记录日志，问题可能会被忽略。
	* 另一个推论，不应该会出现null的地方不进行判null，出现了就抛出NullPointorException，以便检查是哪里代码写错了引入了null。
	* 不该出现异常的地方出现了异常，应该抛出。抛出的最合适异常类型应该是运行时异常，因为，此种场景下的异常不需要调用者关注，对调用者是透明的。
	* 如果上述这种情况下抛出的异常不是运行时异常，可如上例中的方式转义成运行时异常。
	* 绝大多数JDK异常，除了文件、网络、数据库等IO异常，都适合上述做法。


### 第二起血案： IllegalAccessException

好时光总是短暂，咖啡没喝几天，又有人报告：

[![](/images/a_tale_of_sonar_issue/Image [14].png)](/images/a_tale_of_sonar_issue/Image [14].png)

这次，根据异常信息，初步判断，怀疑可能是synchronized方法无法被反射调用。

OK，如法炮制，先用测试用例模拟了一个public synchronized close()：

[![](/images/a_tale_of_sonar_issue/Image [15].png)](/images/a_tale_of_sonar_issue/Image [15].png)

[![](/images/a_tale_of_sonar_issue/Image [16].png)](/images/a_tale_of_sonar_issue/Image [16].png)

WWWHAT？？！！这个测试用例竟然直接通过了！！

[![](/images/a_tale_of_sonar_issue/Image [17].png)](/images/a_tale_of_sonar_issue/Image [17].png)

看样不是public synchronized搞的鬼，还得继续追究原因。

插播一个小问题：新增的这个测试用例既然没有帮助我们复现bug，那个用例要不要删除？还是留着？

我个人的倾向是，针对这个例子，既然已经写了一个public synchronized场景的测试用例，且能够证明DBResource具有对public synchronized方法操作的能力，那不妨也把这个用例留着，作为我们具备能力的说明文档，同时也能起到保护这个能力的作用，况且后续的维护成本不大。总体，利大于弊。

看来，无源码无真相，继续翻看源码：

[![](/images/a_tale_of_sonar_issue/Image [18].png)](/images/a_tale_of_sonar_issue/Image [18].png)

[![](/images/a_tale_of_sonar_issue/Image [19].png)](/images/a_tale_of_sonar_issue/Image [19].png)

太坑die了，close()的确没问题，public synchronized我们也能支持，但是PhysicalConnection竟然是package可见，怪不得DBResource无法访问到，而且jdk的异常信息也有bug，没有针对这种情况的提示。

现在可以通过测试用例复现这种场景了，这种场景的复现还比较需要点小技巧。

首先，需要在测试代码目录的一个单独package example.util.invisible中定义一个package可见的InvisibleCloseable，用来模拟PhysicalConnection。

然后，在package example.util.invisible中再生成一个InvisibleCloseableFactory用来实例化InvisibleCloseable，并暴露给DBResourceTest使用。

这样，就复现了该问题场景：

[![](/images/a_tale_of_sonar_issue/Image [20].png)](/images/a_tale_of_sonar_issue/Image [20].png)

[![](/images/a_tale_of_sonar_issue/Image [21].png)](/images/a_tale_of_sonar_issue/Image [21].png)

[![](/images/a_tale_of_sonar_issue/Image [22].png)](/images/a_tale_of_sonar_issue/Image [22].png)

并且，错误提示信息与报告的一致：

[![](/images/a_tale_of_sonar_issue/Image [23].png)](/images/a_tale_of_sonar_issue/Image [23].png)

[![](/images/a_tale_of_sonar_issue/Image [24].png)](/images/a_tale_of_sonar_issue/Image [24].png)

OK，现在终于可以开工修改生产代码了。

其实，生产代码只需要添加一行Method.setAssesible(true);就可以让测试用例通过：

[![](/images/a_tale_of_sonar_issue/Image [25].png)](/images/a_tale_of_sonar_issue/Image [25].png)

所有测试用例执行通过。

[![](/images/a_tale_of_sonar_issue/Image [26].png)](/images/a_tale_of_sonar_issue/Image [26].png)

OK，提交代码，喝咖啡。

小结，通过第二个血案，我们学到了：

1. 如何写出强健的代码。
	* 受知识、经验的限制，有时我们无法一次写出尽善尽美、考虑周全的代码。
	* 而且出于简单设计的考虑，我们也不应该一开始就期望写出"过分"强健的代码，除非有确凿的证据和较大的可能性表明某些场景需要考虑。
	* 我们只要保证我们的代码能够及时暴露bug、不隐藏bug，并在bug暴露后，通过测试用例修复bug，代码就会越来越合理程度的健壮。


### 后传

汇总之前的小结：

1. 如何正确的修复bug。修复bug要做的第一件事情是添加一个复现bug的测试场景，然后修复bug，让测试场景通过。
2. 善用工具暴露bug。Sonar问题不要轻视，背后可能隐藏着大bug。
3. 异常处理需谨慎。应该视情况妥善处理异常，否则会隐藏bug：
	* 不该出现异常的地方出现了异常（如上例中的NoSuchMethodException），应该果断抛出，因为可能是编码bug引起的。
	* 这种异常抛出，最好能直接引起某种较严重的后果，比如系统宕掉、服务不可用。原因是，这样能够引起足够的重视，便于提早暴露问题、进行修复。如果只是记录日志，问题可能会被忽略。
	* 另一个推论，不应该会出现null的地方不进行判null，出现了就抛出NullPointorException，以便检查是哪里代码写错了引入了null。
	* 不该出现异常的地方出现了异常，应该抛出。抛出的最合适异常类型应该是运行时异常，因为，此种场景下的异常不需要调用者关注，对调用者是透明的。
	* 如果上述这种情况下抛出的异常不是运行时异常，可如上例中的方式转义成运行时异常。
	* 绝大多数JDK异常，除了文件、网络、数据库等IO异常，都适合上述做法。
4. 如何写出强健的代码。
	* 受知识、经验的限制，有时我们无法一次写出尽善尽美、考虑周全的代码。
	* 而且出于简单设计的考虑，我们也不应该一开始就期望写出"过分"强健的代码，除非有确凿的证据和较大的可能性表明某些场景需要考虑。
	* 我们只要保证我们的代码能够及时暴露bug、不隐藏bug，并在bug暴露后，通过测试用例修复bug，代码就会越来越合理程度的健壮。


整个代码的完整演化过程见[Github](https://github.com/wenxinhe/DBResource)。


