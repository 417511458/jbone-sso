## 介绍
jbone sso是基于cas框架实现的单点登录服务。并在cas原有基础上做了扩展。既支持CAS内置所有功能，又完美集成Spring Cloud套件和其他自定义功能。

jbone sso既是jbone项目群中的一员，也完全可以独立部署运行，只需要简单的配置就可以实现微服务项目群的单点登录。

## 功能清单

### SSO服务
功能点 | 完成状态
---|---
单点登录 |  ✔️
单点登出 |  ✔️ 
MVC项目接入 |  ✔️ 
前后端分离项目接入 | ✔️ 
第三方登录 - github |  ✔️ 
第三方登录 - 微信扫码登录 | 开发中
第三方登录 - 微信登录 |  开发中
第三方登录 - 更多... |  开发中
自定义登录异常提示 |  ✔️
自定义皮肤 | 开发中
更多功能... | 计划中

### SSO后台管理
功能点 | 功能描述 |  完成状态
---|--- | ---
服务管理 | 服务的新增、删除、修改、全文检索等 | ✔️
服务版本控制 | 通过对服务的版本控制，可有效控制服务发布流程。将发布流程改为：修改->提交->发布，而不是直接发布服务。 | ✔️
管理后台登录权限控制 | 支持集成SSO Server、静态用户等 | ✔️
委托用户控制 | 相当于github中的PR用户，需要通过PR来变更服务 | ✔️ 
委托用户管理服务 | 管理流程：修改 -> 提交PR | ✔️ 
管理员接受或拒绝委托用户的服务变更 | 处理委托用户的PR | ✔️

## jbone
![Jbone logo](doc/images/logo-text.png)

jbone是基于Spring Cloud开发的项目群，是对Spring Cloud微服务架构的综合应用实践和基础开发框架

[jbone.cn](http://jbone.cn)

**功能架构图**

![Jbone功能架构图](doc/images/features.png)

**项目成员**

项目名 | 简介 | 项目地址
---- | ------ | ----
jbone-sso | 单点登录模块 | [github](https://github.com/417511458/jbone-sso) , [码云](https://gitee.com/majunwei2017/jbone-sso)
jbone-service-management | 服务管理模块(包含注册中心、服务网关、服务监控、调用链追踪等)  |  [github](https://github.com/417511458/jbone-service-management) , [码云](https://gitee.com/majunwei2017/jbone-service-management)
jbone-system | 系统管理模块,通用权限管理等 | [github](https://github.com/417511458/jbone-system) , [码云](https://gitee.com/majunwei2017/jbone-system)
jbone-system-admin | 系统管理后台 | [github](https://github.com/417511458/jbone-system-admin) , [码云](https://gitee.com/majunwei2017/jbone-system-admin)
jbone-fs | 文件系统，用于存储和管理文件、图片等 | [github](https://github.com/417511458/jbone-fs) , [码云](https://gitee.com/majunwei2017/jbone-fs)
jbone-cms | 多站点内容管理系统 | [github](https://github.com/417511458/jbone-cms) , [码云](https://gitee.com/majunwei2017/jbone-cms)
jbone-common | 公共模块，封装工具类等 | [github](https://github.com/417511458/jbone) , [码云](https://gitee.com/majunwei2017/jbone)
jbone-banner | jbone通用艺术banner | [github](https://github.com/417511458/jbone) , [码云](https://gitee.com/majunwei2017/jbone)
jbone-configuration | 公共配置模块 | [github](https://github.com/417511458/jbone) , [码云](https://gitee.com/majunwei2017/jbone)
jbone-ui | 以webjars形式管理前端静态资源，所有包含页面的工程需要依赖此模块。 | [github](https://github.com/417511458/jbone) , [码云](https://gitee.com/majunwei2017/jbone)
jbone-b2b2c | 多店铺电商平台 (未完成) | [github](https://github.com/417511458/jbone-b2b2c) , [码云](https://gitee.com/majunwei2017/jbone-b2b2c)
jbone-bpm | 工作流模块(未完成) | [github](https://github.com/417511458/jbone-bpm) , [码云](https://gitee.com/majunwei2017/jbone-bpm)


