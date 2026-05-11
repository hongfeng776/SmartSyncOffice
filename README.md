# SmartSyncOffice - 分布式智能办公协同系统

## 项目简介

SmartSyncOffice 是一个基于微服务架构的分布式智能办公协同系统，旨在提供高效、安全、易用的企业级办公解决方案。

## 技术栈

### 后端技术栈
- Java 17
- Spring Boot 3.x
- Spring Cloud Alibaba
- Nacos (服务注册中心/配置中心)
- Spring Security + JWT (权限认证)
- MyBatis-Plus (ORM框架)
- MySQL 8.0 (数据库)
- Redis (缓存)

### 前端技术栈
- Vue 3.x
- Vite 5.x
- Element Plus (UI组件库)
- Vue Router (路由管理)
- Pinia (状态管理)
- Axios (HTTP请求)

## 项目结构

```
SmartSyncOffice/
├── backend/                    # 后端项目
│   ├── smart-sync-office-api/  # API模块
│   ├── smart-sync-office-auth/ # 认证服务
│   ├── smart-sync-office-user/ # 用户服务
│   └── pom.xml
├── frontend/                   # 前端项目
│   ├── src/
│   │   ├── api/                # API接口
│   │   ├── components/         # 公共组件
│   │   ├── layout/             # 布局组件
│   │   ├── router/             # 路由配置
│   │   ├── store/              # 状态管理
│   │   ├── utils/              # 工具函数
│   │   └── views/              # 页面视图
│   └── ...
└── database/                   # 数据库脚本
    └── init.sql
```

## 快速开始

### 后端启动
1. 启动Nacos服务
2. 执行数据库脚本 `database/init.sql`
3. 依次启动认证服务和用户服务

### 前端启动
```bash
cd frontend
npm install
npm run dev
```

## 功能模块

- 用户管理
- 角色权限管理
- 组织架构管理
- 待办事项
- 文件管理
- 协同办公
