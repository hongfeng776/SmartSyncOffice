# SmartSyncOffice 项目启动指南

## 项目简介

SmartSyncOffice 是一个分布式智能办公协同系统，采用前后端分离架构，包含完整的用户认证、权限管理、服务注册等功能。

## 技术栈

### 后端
- **JDK**: 17+
- **Spring Boot**: 3.2.0
- **Spring Cloud**: 2023.0.0
- **Spring Cloud Alibaba**: 2023.0.0.0-RC1 (Nacos服务注册)
- **Spring Security**: 安全认证
- **MyBatis Plus**: 3.5.5 (ORM框架)
- **MySQL**: 8.0+
- **JWT**: 无状态Token认证
- **Hutool**: 工具类库
- **FastJSON2**: JSON处理

### 前端
- **Vue**: 3.4.0
- **Vue Router**: 4.2.5
- **Pinia**: 2.1.7 (状态管理)
- **Element Plus**: 2.4.4 (UI组件库)
- **Axios**: 1.6.2 (HTTP客户端)
- **Vite**: 5.0.8 (构建工具)
- **Sass**: CSS预处理器

## 环境要求

- JDK 17+
- Node.js 18+ (推荐 18.17.0+)
- Maven 3.8+
- MySQL 8.0+
- Nacos (可选，用于服务注册发现，如不需要可忽略)

## 快速启动

### 1. 初始化数据库

#### 方式一：使用完整初始化脚本
```bash
# 登录MySQL
mysql -u root -p

# 执行初始化脚本
source /path/to/SmartSyncOffice/database/init.sql
```

#### 方式二：使用简化版脚本（推荐）
```bash
# 登录MySQL
mysql -u root -p

# 执行简化版初始化脚本
source /path/to/SmartSyncOffice/database/init-simple.sql
```

**注意**：
- 数据库脚本会自动创建 `smart_sync_office` 数据库
- 启动后端服务时，会自动更新用户密码为 `123456`
- 默认数据库配置：用户名 `root`，密码 `root`

如果你的MySQL密码不同，请修改以下配置文件：
- `backend/smart-sync-office-auth/src/main/resources/application-dev.yml`
- `backend/smart-sync-office-user/src/main/resources/application-dev.yml`

### 2. 启动后端服务

#### 方式一：使用Maven命令启动

```bash
cd SmartSyncOffice/backend

# 编译并安装所有模块
mvn clean install -DskipTests

# 启动认证服务（端口8081）
mvn -pl smart-sync-office-auth -am spring-boot:run
```

#### 方式二：使用IDE启动

1. 用IDE（IntelliJ IDEA推荐）打开 `backend` 目录
2. 等待Maven依赖下载完成
3. 运行 `smart-sync-office-auth` 模块的 `AuthApplication` 类

#### 方式三：打包后运行

```bash
cd backend
mvn clean package -DskipTests

# 运行认证服务
java -jar smart-sync-office-auth/target/smart-sync-office-auth-1.0.0-SNAPSHOT.jar
```

启动成功后，控制台会输出类似：
```
Started AuthApplication in X.XXX seconds
数据库初始化完成！
管理员账号创建成功: admin / 123456
员工账号创建成功: employee / 123456
```

### 3. 启动前端服务

```bash
cd SmartSyncOffice/frontend

# 安装依赖（首次运行需要）
npm install

# 启动开发服务器
npm run dev
```

启动成功后，浏览器会自动打开 `http://localhost:3000`

## 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端 | http://localhost:3000 | 主应用入口 |
| 认证服务 | http://localhost:8081/auth | 登录、权限等API |
| 用户服务 | http://localhost:8082/user | 用户管理等API（可选） |

## 默认测试账号

| 用户名 | 密码 | 角色 | 权限 |
|--------|------|------|------|
| admin | 123456 | 超级管理员 | 全部权限（含系统管理） |
| employee | 123456 | 普通员工 | 基础权限（无系统管理） |

## 项目结构说明

### 后端项目结构

```
backend/
├── pom.xml                          # 父POM，管理依赖版本
├── smart-sync-office-api/          # 公共API模块
│   ├── src/main/java/com/smartsync/api/
│   │   ├── config/
│   │   │   └── WebConfig.java      # 跨域配置
│   │   ├── dto/
│   │   │   ├── LoginDTO.java       # 登录请求DTO
│   │   │   └── UserDTO.java        # 用户信息DTO
│   │   ├── exception/
│   │   │   ├── BusinessException.java   # 业务异常
│   │   │   └── GlobalExceptionHandler.java # 全局异常处理
│   │   └── result/
│   │       ├── Result.java         # 统一返回格式
│   │       └── ResultCode.java     # 返回码枚举
│   └── pom.xml
│
├── smart-sync-office-auth/         # 认证服务（核心服务，端口8081）
│   ├── src/main/java/com/smartsync/auth/
│   │   ├── AuthApplication.java    # 启动类
│   │   ├── config/
│   │   │   ├── DataInitializer.java     # 数据初始化
│   │   │   ├── MybatisPlusConfig.java   # MyBatis Plus配置
│   │   │   └── SecurityConfig.java      # Spring Security配置
│   │   ├── controller/
│   │   │   ├── AuthController.java      # 认证相关API
│   │   │   ├── PermissionController.java # 权限管理API
│   │   │   └── UserController.java      # 用户管理API
│   │   ├── entity/
│   │   │   ├── SysUser.java         # 用户实体
│   │   │   ├── SysRole.java         # 角色实体
│   │   │   └── SysPermission.java   # 权限实体
│   │   ├── filter/
│   │   │   └── JwtAuthenticationFilter.java # JWT认证过滤器
│   │   ├── mapper/
│   │   │   ├── SysUserMapper.java
│   │   │   ├── SysRoleMapper.java
│   │   │   └── SysPermissionMapper.java
│   │   ├── security/
│   │   │   └── CustomUserDetailsService.java # 自定义用户详情服务
│   │   ├── service/
│   │   │   ├── AuthService.java     # 认证服务
│   │   │   └── UserService.java     # 用户服务
│   │   └── utils/
│   │       ├── JwtUtil.java         # JWT工具类
│   │       └── PasswordGenerator.java # 密码生成工具
│   └── src/main/resources/
│       ├── application.yml          # 主配置
│       └── application-dev.yml      # 开发环境配置
│
└── smart-sync-office-user/         # 用户服务（可选扩展，端口8082）
    └── src/main/
        └── resources/
            ├── application.yml
            └── application-dev.yml
```

### 前端项目结构

```
frontend/
├── index.html                      # HTML入口
├── package.json                    # 依赖配置
├── vite.config.js                  # Vite配置（含代理）
└── src/
    ├── main.js                     # 应用入口
    ├── App.vue                     # 根组件
    ├── api/
    │   └── auth.js                 # 认证API封装
    ├── layout/
    │   ├── index.vue               # 主布局
    │   └── components/
    │       ├── Header.vue          # 顶部导航栏
    │       ├── Sidebar.vue         # 侧边栏菜单
    │       └── SidebarItem.vue     # 菜单项组件
    ├── router/
    │   └── index.js                # 路由配置（含权限守卫）
    ├── store/
    │   ├── index.js                # Pinia导出
    │   └── modules/
    │       ├── app.js              # 应用状态
    │       └── user.js             # 用户状态
    ├── styles/
    │   └── index.scss              # 全局样式
    ├── utils/
    │   └── request.js              # Axios封装（含拦截器）
    └── views/
        ├── login/
        │   └── index.vue           # 登录页
        ├── dashboard/
        │   └── index.vue           # 系统首页
        ├── profile/
        │   └── index.vue           # 个人中心
        ├── todo/
        │   └── index.vue           # 待办事项
        ├── file/
        │   └── index.vue           # 文件管理
        └── system/
            ├── user/
            │   └── index.vue       # 用户管理
            ├── role/
            │   └── index.vue       # 角色管理
            └── permission/
                └── index.vue       # 权限管理
```

## 核心功能说明

### 后端功能

#### 1. 统一返回格式
所有API统一使用 `Result<T>` 格式返回：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1715000000000
}
```

#### 2. 全局异常处理
自动捕获并处理各类异常，统一返回格式：
- 业务异常
- 参数校验异常
- 请求方法不支持
- 系统异常

#### 3. JWT认证机制
- 无状态Token认证
- Token过期自动提示
- 支持Token刷新（可扩展）

#### 4. 权限控制
- 基于角色的权限控制（RBAC）
- 角色与权限多对多关联
- 接口级别权限拦截

#### 5. 分布式架构
- 模块化设计（api/auth/user）
- 支持Nacos服务注册发现
- 可独立部署、水平扩展

### 前端功能

#### 1. 登录页面
- 用户名/密码登录
- 测试账号提示
- 表单验证

#### 2. 系统首页布局
- 可折叠侧边栏菜单
- 顶部导航栏（用户信息、面包屑）
- 主内容区（带路由过渡动画）
- 页面缓存（KeepAlive）

#### 3. 路由与权限
- 动态路由（根据角色加载）
- 路由守卫（登录认证、权限验证）
- 白名单页面

#### 4. 状态管理
- Pinia管理用户和应用状态
- 持久化存储（localStorage）

#### 5. 权限菜单
- 根据用户角色动态显示菜单
- 管理员可看到"系统管理"菜单
- 普通员工只能看到基础菜单

## API接口说明

### 认证相关

| 方法 | 路径 | 说明 | 是否需要认证 |
|------|------|------|-------------|
| POST | /api/login | 用户登录 | 否 |
| GET | /api/user/info | 获取用户信息 | 是 |
| POST | /api/logout | 退出登录 | 是 |

### 用户管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/users | 分页查询用户列表 |
| GET | /api/users/{id} | 根据ID查询用户 |
| POST | /api/users | 创建用户 |
| PUT | /api/users | 更新用户 |
| DELETE | /api/users/{id} | 删除用户 |
| PUT | /api/users/{id}/status?status=0/1 | 更新用户状态 |
| PUT | /api/users/{id}/reset-password | 重置密码为123456 |
| GET | /api/users/{id}/roles | 获取用户角色 |

### 角色和权限

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/roles | 获取所有角色 |
| GET | /api/permissions | 获取所有权限 |

## 数据库表结构

### 核心表

#### 1. sys_user - 用户表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| username | VARCHAR(50) | 用户名（唯一） |
| password | VARCHAR(200) | 密码（BCrypt加密） |
| real_name | VARCHAR(50) | 真实姓名 |
| email | VARCHAR(100) | 邮箱 |
| phone | VARCHAR(20) | 手机号 |
| avatar | VARCHAR(255) | 头像URL |
| status | TINYINT | 状态（1:启用 0:禁用） |
| last_login_time | DATETIME | 最后登录时间 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| is_deleted | TINYINT | 逻辑删除 |

#### 2. sys_role - 角色表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| role_name | VARCHAR(50) | 角色名称 |
| role_code | VARCHAR(50) | 角色编码（唯一） |
| description | VARCHAR(200) | 角色描述 |
| status | TINYINT | 状态 |

#### 3. sys_permission - 权限表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| permission_name | VARCHAR(100) | 权限名称 |
| permission_code | VARCHAR(100) | 权限编码（唯一） |
| resource_type | VARCHAR(20) | 资源类型（menu/button） |
| parent_id | BIGINT | 父级权限ID |
| path | VARCHAR(200) | 路由路径 |
| icon | VARCHAR(50) | 图标 |
| sort_order | INT | 排序 |
| status | TINYINT | 状态 |

#### 4. sys_user_role - 用户角色关联表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 用户ID |
| role_id | BIGINT | 角色ID |

#### 5. sys_role_permission - 角色权限关联表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| role_id | BIGINT | 角色ID |
| permission_id | BIGINT | 权限ID |

## 常见问题

### 1. 数据库连接失败
检查 MySQL 是否启动，用户名密码是否正确，修改 `application-dev.yml` 中的配置。

### 2. 后端启动报错找不到类
执行 `mvn clean install` 重新编译安装模块。

### 3. 前端启动报错缺少依赖
删除 `node_modules` 文件夹和 `package-lock.json`，重新执行 `npm install`。

### 4. 登录提示密码错误
确保数据库初始化后，后端服务已启动，启动时会自动重置密码为 `123456`。

### 5. 跨域问题
前端已配置 Vite 代理，后端已配置 CORS，正常情况下不会出现跨域问题。

## 下一步扩展建议

1. **用户管理完善**
   - 用户角色分配
   - 用户权限查看
   - 用户导入导出

2. **角色管理完善**
   - 角色增删改查
   - 角色权限分配
   - 角色复制功能

3. **权限管理完善**
   - 权限树形结构
   - 权限资源管理
   - 按钮级别权限控制

4. **其他功能模块**
   - 待办事项管理
   - 文件上传下载
   - 消息通知
   - 日程管理
   - 部门管理
   - 日志审计

## 开发规范

### 后端
- 使用 Lombok 简化代码
- 遵循 RESTful API 设计规范
- 统一使用 Result 返回格式
- 业务异常使用 BusinessException
- 参数校验使用 @Valid + Validation

### 前端
- 使用 Vue3 Composition API
- 使用 Pinia 进行状态管理
- 页面组件放在 views 目录
- 公共组件放在 components 目录
- API 接口统一在 api 目录管理

## License

MIT License
