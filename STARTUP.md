# SmartSyncOffice 项目启动指南

## 环境要求

- JDK 17+
- Node.js 18+
- Maven 3.8+
- MySQL 8.0+
- Nacos (可选，用于服务注册发现)

## 快速启动

### 1. 初始化数据库

```bash
# 登录MySQL
mysql -u root -p

# 执行初始化脚本
source database/init.sql
```

注意：脚本中的默认密码是 `123456`，BCrypt加密后的值已经预置在数据库中。

### 2. 启动后端

```bash
cd backend

# 方式一：使用Maven启动
# 启动认证服务（端口8081）
mvn -pl smart-sync-office-auth -am spring-boot:run

# 启动用户服务（端口8082）
mvn -pl smart-sync-office-user -am spring-boot:run
```

如果没有Nacos，可以暂时注释掉Nacos相关配置。

### 3. 启动前端

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

## 默认测试账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 超级管理员 |
| employee | 123456 | 普通员工 |

## 项目结构说明

### 后端项目

```
backend/
├── smart-sync-office-api/      # 公共API模块
│   ├── result/                # 统一返回格式
│   ├── exception/           # 全局异常处理
│   ├── config/              # 配置类
│   └── dto/                 # 数据传输对象
├── smart-sync-office-auth/    # 认证服务（端口8081）
│   ├── entity/              # 实体类
│   ├── mapper/              # MyBatis Mapper
│   ├── service/             # 业务逻辑
│   ├── controller/          # Controller
│   ├── filter/            # JWT过滤器
│   ├── security/          # Security配置
│   └── utils/           # 工具类（JWT）
│   └── resources/       # 配置文件
└── smart-sync-office-user/   # 用户服务（端口8082）
```

### 前端项目

```
frontend/
├── src/
│   ├── api/                # API接口
│   ├── components/          # 公共组件
│   ├── layout/              # 布局组件
│   ├── router/              # 路由配置
│   ├── store/               # 状态管理（Pinia）
│   ├── styles/              # 全局样式
│   ├── utils/               # 工具函数
│   └── views/              # 页面视图
│       ├── login/           # 登录页面
│       ├── dashboard/       # 系统首页
│       ├── profile/         # 个人中心
│       ├── todo/            # 待办事项
│       ├── file/             # 文件管理
│       └── system/         # 系统管理
│           ├── user/           # 用户管理
│           ├── role/         # 角色管理
│           └── permission/   # 权限管理
├── index.html
├── vite.config.js
└── package.json
```

## 功能说明

### 后端功能

1. **统一返回格式** - 所有接口统一使用 `Result<T>` 格式返回
2. **全局异常处理** - 自动捕获并统一格式返回
3. **JWT认证** - 无状态Token认证机制
4. **权限控制** - 基于角色的权限控制（RBAC）
5. **服务注册** - 支持Nacos服务注册发现

### 前端功能

1. **登录页面** - 用户名密码登录
2. **首页布局** - 侧边栏菜单、顶部导航、主内容区
3. **路由守卫** - 登录认证和权限拦截
4. **状态管理** - Pinia管理用户和应用状态
5. **权限菜单** - 根据角色动态显示菜单
