---
layout:     post
title:      使用IntelliJ+SonarQube做高效代码走查
date:       2015-04-29 19:53
#summary:    See what the different elements looks like. Your markdown has never looked better. I promise.
categories: tech
---
# 为什么要做代码走查
敏捷内建质量的要求，减少技术债(technical debt)，保持cost of changes

# 代码走查常见痛点
* 时间长
 + 需要走查代码多
 + 讨论业务细节
 + 缺乏权威意见，容易争论，无法达成一致
* 成本高
 + 团队全体成员参加，时间长，导致成本高
* 效果不明显
 + 代码走查想做的事太多，既想知识传递、提升技能，又想业务交流、消除模块壁垒，结果很可能什么效果都没有达到

# 怎样才能高效的代码走查

## 圈定走查范围
代码走查的范围策略一般有两种：

* 每日增量代码走查：只走查前一天产生的新增代码。
* 功能/故事级别的代码走查：当一个功能/故事完成后，对这些代码进行走查。

第二种方式的有两个主要缺点：

1. 走查代码的范围比较难确定：敏捷开发中，功能／故事一般会持续3-5天，等到完成后哪些代码属于这个功能／故事已经不易确定。
2. 走查出的问题，易被抵触：一旦功能／故事开发完成，再对代码提出修改意见，可能会受到较大抵触。

相比第二种方式的缺点，每日代码走查能够更好的圈定走查范围，并且功能/故事还在开发中，团队成员也更乐于接受别人的建议。

前提：精益需求、结对编程
框定范围：cleancode, tech debt
流程：
http://www.sonarqube.org/effective-code-review-with-sonar/

intellij diff:
manual code review:
http://www.sonarqube.org/sonar-2-8-in-screenshots/
action plan:
auto-assign:
scm stats: