---
layout:     post
title:      Shell脚本的质量保障体系
date:       2016-04-10
summary:    
categories: tech
---

现在已经有很多友好、易用的编程语言出现在我们的日常开发中，成为我们爱不释手的编程利器。但是，Shell脚本作为一个奇葩的存在，依然会或多或少的出现在我们的编辑器中，特别是在与Linux操作系统打交道的地方。

作为一门有些“过时”的语言，Shell脚本在与操作系统的交互上仍具有编写快捷、使用方便的好处，但是，很不幸，Shell脚本也很难驾驭。如果你的项目中偏偏存在一定量这样的Shell脚本，那么，晦涩的语法、杂乱无章的语句会把你搞的焦头烂额。

针对这种“过时”语言，能否有办法像现代语言一样，比如，Java*，搭建出一套质量保障体系，来提升Shell脚本的质量？

答案是肯定的。要想想，现在很多牛人还在使用Shell，可以说对Shell情有独钟，他们随便整出个什么东东，比如，...（太多了，不比如了），都好用的一塌。

（*我不是说Java很新，从某种程度上，Java也已经out了，我在这里提到Java，只是Java在下面提到的方面比较成熟。）

下面，我们尝试从以下三个方面搭建起Shell脚本*的质量保障体系：

* Shell脚本的静态检查
* Shell脚本的单元测试
* Shell脚本的编码能力

（*为了避免不必要的混淆，我们这里限定，文中所说的Shell都特指Bash）

### Shell脚本的静态检查

静态检查在脚本运行前对脚本进行解析，判断脚本中是否存在可能的bug及不友好的书写方式。它的反馈速度仅次于语法检查，能够秒级的反馈给程序员哪里有问题需要注意了。

#### ShellCheck

[ShellCheck](http://www.shellcheck.net/)是Shell脚本的静态检查工具，类似于Java的FindBugs、PMD。你可以将一段Shell脚本粘贴到ShellCheck主页的演示框中，很快，演示框会提示你脚本中有哪些潜在的问题。

![演示框会提示你脚本中有哪些潜在的问题](http://)

如果只是将Shell脚本粘贴到ShellCheck主页才能进行静态检查，那一定是我们使用的方式有问题。

除了上面演示模式，ShellCheck还可以通过包管理器将ShellCheck[安装在本地](https://github.com/koalaman/shellcheck#installing)（不支持Windows），进行本地检查。

```shellcheck [OPTIONS...] FILES...

检查结果会打印在控制台，如果控制台支持ANSI，还可以看到警告的不同级别（级别由低到高：绿色->黄色->红色）

![本地检查的结果](http://)


#### 与编辑器的集成

如果每次写完脚本，还执行一个检查命令才可以得到结果，是不是还稍显笨拙？不够爽，不够快？如果在编写脚本的过程中就能实时告诉我哪里有问题，这个是不是就更爽了？

为了进一步加快反馈，需要将ShellCheck与[脚本编辑器集成](https://github.com/koalaman/shellcheck#in-your-editor)。

在GUI环境中，可以选择[集成到神器Sublime Text](https://github.com/SublimeLinter/SublimeLinter-shellcheck)中，需要配合安装SublimeLinter和SublimeLinter-ShellCheck这两个插件。

![sublime show](http://)

在CLI环境中，可以[集成到vi](https://github.com/scrooloose/syntastic)，需要配合安装一个语法检查插件syntastic。

![SyntasticCheck](http://)

### Shell脚本的单元测试

相比于静态检查，单元测试可以理解为动态检查（即运行时检查）。静态检查检查语法错误，而运行时检查检查业务逻辑。

#### shUnit2

[shunit2](https://github.com/zandev/shunit2)是Shell脚本的单元测试框架。

![mkdir_test.sh](http://)

可以看出，shunit2是xUnit单元测试框架的Shell脚本实现，熟悉xUnit的程序猿（媛）使用起来完全没有障碍。

有了单元测试框架，我们可以玩的都东西都很多了，Shell脚本的TDD*也并不是不可能了8-）。

(当然，谁会TDD Shell脚本呢，如果业务逻辑达到需要TDD的程度，那么可以考虑使用其他语言替代了)

### 质量保障体系的建立

如果只是使用了两三个检查工具，就可以妄称建立了Shell脚本的质量保障体系么？当然没那么简单。不过，距离最终的目标也不远了。质量保障体系的建立，需要将质量保障手段无缝嵌入到整个开发流程中，而CI（持续集成）这个环节无法绕开。只有在将上面的质量保障手段嵌入CI环境中，才能够保证产出的代码是经过质量检查的。

下面是一个Shell脚本质量保障体系的实例：

程序猿（媛）使用本地编辑器实时反馈Shell脚本静态检查结果，提交CI前运行单元测试，提交CI后统一由CI环境再次进行静态检查、单元测试，如果检查结果不满足阈值要求，CI流水线停止作业，所有人停下来修复流水线，待CI流水线正常后继续提交代码。

### 一切都清净了？

Shell脚本质量保障体系建立好之后，就可以坐等Shell脚本质量提升了？

错！

#### “人与交互 胜于 工具与流程”

Shell脚本的xx工具只是Shell脚本质量提升中最简单、最容易做的一步。

人才是最关键的因素。

人的意识和人的能力。

“ShellCheck检查出来的警告有用吗？我觉得没错，不用改啊”

“Shell脚本还要写单元测试？”

#### Shell编码能力提升

意识上有了转变，但能力不足也不行？比如，ShellCheck检查出了bug，但是不会改？

[Shell编码规范](https://github.com/ymattw/shell-style-guide/blob/master/shell-style-guide-cn.md)

[Google Shell 风格指南](http://zh-google-styleguide.readthedocs.org/en/latest/google-shell-styleguide/contents/)

[Common mistakes and useful tricks](https://github.com/azet/community_bash_style_guide#common-mistakes-and-useful-tricks)

[Gallery of bad code](https://github.com/koalaman/shellcheck#gallery-of-bad-code)

[explainshell](http://www.explainshell.com/)

