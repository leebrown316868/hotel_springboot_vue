# 🏨 酒店民宿管理系统（Hotel Management System）

> **基于 Spring Boot + Vue 3 的前后端分离酒店民宿管理系统**
>
> 本系统是一套面向中小型酒店/民宿的全流程数字化管理解决方案，涵盖客房管理、订单管理、客户管理、财务监管、数据统计分析等核心业务功能，支持管理员、前台员工、客户三种角色的差异化操作界面。

---

## 📑 目录

- [项目概述](#项目概述)
- [技术栈](#技术栈)
- [系统架构](#系统架构)
- [功能模块](#功能模块)
- [数据库设计](#数据库设计)
- [API 接口设计](#api-接口设计)
- [项目结构](#项目结构)
- [环境要求](#环境要求)
- [快速启动](#快速启动)
- [系统截图与功能演示](#系统截图与功能演示)
- [项目亮点与创新点](#项目亮点与创新点)
- [未来展望](#未来展望)

---

## 项目概述

### 1.1 选题背景

随着旅游业和民宿行业的快速发展，传统的手工管理方式已无法满足日益增长的业务需求。酒店/民宿管理面临着客房状态实时更新困难、订单处理效率低下、客户信息管理分散、财务数据统计不及时等痛点。因此，开发一套高效、智能的酒店民宿管理系统，对于提升运营效率和客户体验具有重要的现实意义。

### 1.2 项目目标

本系统旨在实现以下核心目标：

1. **客房全生命周期管理**：实现客房状态（空闲/已入住/维修中/已清洁）的实时跟踪与更新
2. **订单全流程管理**：覆盖预订→入住→退房→支付的完整业务闭环
3. **多角色权限控制**：基于 RBAC（基于角色的访问控制）模型，实现管理员、前台员工、客户三种角色的差异化权限
4. **数据可视化分析**：通过 ECharts 图表实现营收趋势、入住率、房型分布等关键经营指标的可视化展示
5. **客户自助服务**：支持客户在线浏览房间、自助预订、订单管理及评价反馈

### 1.3 适用场景

- 中小型酒店的日常运营管理
- 连锁民宿的标准化管理
- 酒店管理相关课程的教学与实践

---

## 技术栈

### 2.1 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| **Java** | 11 | 编程语言 |
| **Spring Boot** | 2.7.18 | 应用框架，提供自动配置、快速开发能力 |
| **Spring Security** | 5.x | 安全框架，实现认证与授权 |
| **Spring Data JPA** | 2.7.x | ORM 框架，简化数据库操作 |
| **JWT (jjwt)** | 0.11.5 | JSON Web Token，实现无状态身份认证 |
| **SQLite** | 3.42.0 | 轻量级嵌入式数据库 |
| **Lombok** | 1.18.24 | Java 代码简化工具 |
| **Hibernate Validator** | 6.x | 数据校验框架 |
| **BCrypt** | — | 密码加密算法 |
| **Maven** | 3.x | 项目构建与依赖管理 |

### 2.2 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| **Vue.js** | 3.5.x | 渐进式 JavaScript 框架（Composition API） |
| **TypeScript** | 5.8.x | JavaScript 超集，提供类型安全 |
| **Vite** | 6.2.x | 新一代前端构建工具，极速热更新 |
| **Vue Router** | 5.x | 官方路由管理器 |
| **Pinia** | 3.x | 新一代状态管理工具 |
| **Element Plus** | 2.13.x | Vue 3 企业级 UI 组件库 |
| **ECharts** | 6.0 | 百度开源数据可视化图表库 |
| **Axios** | 1.13.x | HTTP 客户端，封装 API 请求 |
| **TailwindCSS** | 4.x | 原子化 CSS 框架 |

### 2.3 开发工具与环境

| 工具 | 说明 |
|------|------|
| **IntelliJ IDEA / VS Code** | 后端/前端 IDE |
| **Node.js** | 前端运行环境 |
| **Maven** | 后端依赖管理 |
| **Git** | 版本控制 |
| **Windows 11** | 开发操作系统 |

---

## 系统架构

### 3.1 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                      客户端（浏览器）                         │
│         Vue 3 + Element Plus + ECharts + TailwindCSS        │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP / REST API
                           │ (Axios + Vite Proxy)
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                    Spring Boot 后端服务                       │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              Spring Security + JWT 认证               │   │
│  │          JwtAuthenticationFilter 拦截器                │   │
│  └──────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │                  Controller 控制层                     │   │
│  │  AuthController │ RoomController │ BookingController  │   │
│  │  GuestController │ StatisticsController │ ...         │   │
│  └──────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │                   Service 业务层                       │   │
│  │  AuthService │ RoomService │ BookingService │ ...     │   │
│  └──────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │                 Repository 持久层                      │   │
│  │            Spring Data JPA + SQLiteDialect            │   │
│  └──────────────────────────────────────────────────────┘   │
└──────────────────────────┬──────────────────────────────────┘
                           │ JDBC
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                    SQLite 嵌入式数据库                        │
│                     data/hotel.db                           │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 前后端分离架构说明

本系统采用**前后端完全分离**的开发模式：

- **前端**：Vue 3 + Vite 构建的单页应用（SPA），运行在 `localhost:3000`
- **后端**：Spring Boot RESTful API 服务，运行在 `localhost:8080`
- **通信方式**：前端通过 Axios 发送 HTTP 请求，Vite 开发服务器配置反向代理将 `/api` 前缀的请求转发至后端
- **认证机制**：基于 JWT 的无状态认证，Token 存储在客户端 localStorage

### 3.3 设计模式与架构特点

| 设计模式/原则 | 应用场景 |
|:---|:---|
| **MVC 分层架构** | Controller → Service → Repository 三层解耦 |
| **DTO 模式** | Request/Response DTO 隔离内部实体与外部接口 |
| **统一响应封装** | `ApiResponse<T>` 泛型封装所有 API 响应 |
| **RBAC 权限模型** | 基于角色的访问控制（ADMIN/STAFF/CUSTOMER） |
| **无状态认证** | JWT Token + 过滤器链实现 |
| **组合模式** | `CompositeUserDetailsService` 复合用户服务 |
| **路由守卫** | 前端路由级别与后端接口级别的双重权限校验 |

---

## 功能模块

### 4.1 系统角色说明

本系统设计了三种用户角色，每种角色拥有不同的功能权限：

| 角色 | 英文标识 | 角色说明 | 登录后默认页面 |
|------|:--------:|----------|:--------------:|
| 🔑 系统管理员 | `ADMIN` | 拥有系统全部功能的最高权限 | 仪表盘（Dashboard） |
| 👨‍💼 前台员工 | `STAFF` | 负责日常客房管理和订单处理 | 订单管理（Staff Bookings） |
| 👤 客户/住客 | `CUSTOMER` | 可浏览房间、在线预订和管理个人订单 | 在线预订（Booking Wizard） |

### 4.2 前台员工模块

前台员工模块是一线操作人员的核心工作平台，包含以下子模块：

#### 4.2.1 订单管理（StaffBookings）
- 查看所有待处理订单列表
- 办理入住/退房手续
- 处理预订确认/取消
- 按状态（待确认/已确认/已入住/已退房/已取消）筛选订单
- 订单详情查看与操作

#### 4.2.2 客房管理（Rooms）
- 实时查看全部客房状态（空闲/已入住/维修中/已清洁）
- 更新客房房态与状态
- 按楼层、房型、状态筛选客房
- 客房信息详情查看
- 支持批量操作

#### 4.2.3 客户管理（Guests）
- 客户档案的创建与编辑
- 客户信息搜索与查询
- 查看客户历史入住记录
- 客户状态管理（活跃/非活跃）
- 批量删除客户功能

### 4.3 管理员模块

管理员模块面向酒店管理层，提供全局管控和数据决策支持：

#### 4.3.1 订单与财务监管（Dashboard）
- **仪表盘总览**：显示关键指标卡片（总预订数、今日入住/退房、营收总额、入住率等）
- **全量订单查看**：可查看系统中所有历史和当前订单
- **财务报表**：营收数据的统计与展示
- **订单趋势图**：基于 ECharts 的预订趋势可视化
- **最近预订列表**：快速查看最新订单动态

#### 4.3.2 数据统计与分析（Analytics）
- **营收趋势分析**：按日/周/月展示营收变化曲线
- **入住率统计**：客房入住率的时间序列分析
- **房型分布图**：各房型客房数量与占比（饼图/环形图）
- **房态分布**：空闲/已入住/维修/已清洁的状态分布
- **关键经营指标**：综合展示核心业务数据

#### 4.3.3 系统管理（Settings）
- **员工账号管理**：创建/编辑/禁用员工账户
- **酒店基本信息设置**：酒店名称、联系方式、地址、描述
- **系统参数配置**：货币类型（CNY）、时区（UTC+8）、语言（Chinese）
- **安全设置**：双因素认证开关、会话超时时间、密码过期策略
- **通知设置**：预订邮件通知、取消邮件通知、推送通知开关
- **房型管理**：房型的增删改查与价格配置

#### 4.3.4 客房资源管理（RoomsResource）
- 客房资源的全局管理
- 房型配置与价格体系维护
- 客房的新增、编辑、删除
- 客房容量与楼层管理

### 4.4 客户模块（住客端）

客户模块为住客/顾客提供自助服务功能：

#### 4.4.1 房间浏览与搜索（About / RoomDetail）
- 酒店信息展示（关于我们页面）
- 房间详情查看（房型、价格、容量、设施）
- 客房可用性实时查询

#### 4.4.2 在线预订与支付（BookingWizard）
- **向导式预订流程**：分步引导用户完成预订
  - 第一步：选择入住/退房日期、入住人数
  - 第二步：浏览可用房间并选择
  - 第三步：填写住客信息
  - 第四步：确认订单并支付
- 自动计算住宿费用
- 支付状态管理

#### 4.4.3 订单管理（MyBookings）
- 查看个人所有订单
- 订单状态跟踪（待确认→已确认→已入住→已退房）
- 取消未入住的订单
- 订单详情查看

#### 4.4.4 个人信息管理（Profile）
- 修改个人基本信息（姓名、手机号、地址）
- 修改登录密码
- 偏好设置管理

#### 4.4.5 历史记录与评价反馈（HistoryFeedback）
- 查看历史入住记录
- 对已完成的订单进行评分（1-5星）
- 撰写入住体验评价
- 查看个人评价历史

### 4.5 公共功能模块

#### 4.5.1 用户认证（Login）
- 用户登录（邮箱 + 密码）
- 新用户注册
- 登录后根据角色自动跳转至对应首页
- JWT Token 自动管理

#### 4.5.2 通知系统（NotificationDropdown）
- 实时消息通知展示
- 通知类型分类（预订通知、系统通知等）
- 标记已读/未读
- 通知优先级管理

#### 4.5.3 密码修改（ChangePasswordDialog）
- 安全的密码修改对话框
- 原密码验证 + 新密码确认

---

## 数据库设计

### 5.1 数据库选型

本系统采用 **SQLite** 作为数据库引擎，主要考虑因素：

- **轻量级部署**：无需额外安装数据库服务器，数据库文件 (`data/hotel.db`) 随项目部署
- **零配置**：无需数据库管理员维护
- **适合中小型应用**：满足中小型酒店的业务数据量需求
- **跨平台兼容**：Windows/Linux/macOS 全平台支持

### 5.2 数据库 E-R 图（实体关系）

```
┌──────────┐     1:N     ┌──────────┐     N:1     ┌──────────┐
│  users   │─────────────│ bookings │─────────────│  rooms   │
│ (用户表)  │             │ (订单表)  │             │ (客房表)  │
└──────────┘             └────┬─────┘             └──────────┘
                              │ 1:1
                              │
                         ┌────▼─────┐
                         │ reviews  │
                         │ (评价表)  │
                         └──────────┘

┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   guests     │    │ room_types   │    │system_settings│
│  (客户表)     │    │ (房型表)      │    │ (系统设置表)   │
└──────────────┘    └──────────────┘    └──────────────┘

┌──────────────┐
│notifications │
│ (通知表)      │
└──────────────┘
```

### 5.3 数据表详细设计

系统共包含 **8 张数据表**，详细设计如下：

#### 5.3.1 用户表（users）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | INTEGER | PRIMARY KEY, AUTO | 用户ID |
| email | VARCHAR | NOT NULL, UNIQUE | 邮箱（登录账号） |
| password | VARCHAR | NOT NULL | 密码（BCrypt加密） |
| name | VARCHAR | NOT NULL | 用户姓名 |
| phone | VARCHAR | — | 手机号 |
| address | TEXT | — | 地址 |
| nationality | VARCHAR | — | 国籍 |
| preferences_enabled | INTEGER | DEFAULT 1 | 偏好设置开关 |
| role | VARCHAR | NOT NULL | 角色（ADMIN/STAFF/CUSTOMER） |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL | 更新时间 |

#### 5.3.2 客房表（rooms）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | INTEGER | PRIMARY KEY, AUTO | 客房ID |
| number | VARCHAR | NOT NULL, UNIQUE | 房间号 |
| floor | VARCHAR | NOT NULL | 楼层 |
| type | VARCHAR | NOT NULL | 房型（STANDARD/DELUXE/SUITE等） |
| status | VARCHAR | NOT NULL | 房态（AVAILABLE/OCCUPIED/MAINTENANCE/CLEANED） |
| price | DECIMAL | NOT NULL | 每晚价格 |
| capacity | INTEGER | NOT NULL | 容纳人数 |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL | 更新时间 |

#### 5.3.3 客户表（guests）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | INTEGER | PRIMARY KEY, AUTO | 客户ID |
| name | VARCHAR(100) | NOT NULL | 客户姓名 |
| email | VARCHAR(100) | NOT NULL, UNIQUE | 客户邮箱 |
| phone | VARCHAR(20) | NOT NULL | 联系电话 |
| country | VARCHAR(50) | NOT NULL | 国家 |
| status | VARCHAR(20) | DEFAULT 'ACTIVE' | 状态（ACTIVE/INACTIVE） |
| total_bookings | INTEGER | DEFAULT 0 | 总预订次数 |
| last_stay | DATE | — | 最近入住日期 |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL | 更新时间 |

#### 5.3.4 订单表（bookings）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | INTEGER | PRIMARY KEY, AUTO | 订单ID |
| booking_number | VARCHAR(20) | UNIQUE, NOT NULL | 订单编号 |
| guest_id | INTEGER | FK → guests(id) | 关联客户ID |
| guest_name | VARCHAR(100) | NOT NULL | 客户姓名（冗余字段） |
| room_id | INTEGER | FK → rooms(id) | 关联客房ID |
| room_type | VARCHAR(20) | NOT NULL | 房型 |
| check_in_date | DATE | NOT NULL | 入住日期 |
| check_out_date | DATE | NOT NULL | 退房日期 |
| guest_count | INTEGER | NOT NULL, CHECK > 0 | 入住人数 |
| status | VARCHAR(20) | DEFAULT 'PENDING' | 订单状态 |
| total_amount | DECIMAL(10,2) | NOT NULL, CHECK >= 0 | 总金额 |
| payment_status | VARCHAR(20) | DEFAULT 'UNPAID' | 支付状态 |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL | 更新时间 |

> **订单状态流转**：`PENDING`（待确认）→ `CONFIRMED`（已确认）→ `CHECKED_IN`（已入住）→ `CHECKED_OUT`（已退房）/ `CANCELLED`（已取消）

#### 5.3.5 评价表（reviews）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | INTEGER | PRIMARY KEY, AUTO | 评价ID |
| booking_id | INTEGER | FK → bookings(id), UNIQUE | 关联订单（一对一） |
| user_id | INTEGER | FK → users(id) | 评价用户 |
| rating | INTEGER | CHECK 1~5 | 评分（1-5星） |
| comment | TEXT | — | 评价内容 |
| created_at | TEXT | NOT NULL | 创建时间 |

#### 5.3.6 系统设置表（system_settings）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| hotel_name | VARCHAR(100) | 酒店名称 |
| contact_email | VARCHAR(100) | 联系邮箱 |
| contact_phone | VARCHAR(20) | 联系电话 |
| address | VARCHAR(200) | 酒店地址 |
| description | VARCHAR(1000) | 酒店简介 |
| currency | VARCHAR(10) | 货币类型（默认 CNY） |
| timezone | VARCHAR(10) | 时区（默认 UTC+8） |
| language | VARCHAR(20) | 语言（默认 Chinese） |
| two_factor_enabled | INTEGER | 是否启用双因素认证 |
| session_timeout | INTEGER | 会话超时时间（分钟） |
| password_expiry | INTEGER | 密码过期天数 |
| email_notification_* | INTEGER | 邮件通知开关 |

#### 5.3.7 通知表（notifications）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INTEGER | 通知ID |
| user_id | VARCHAR(50) | 目标用户 |
| title | VARCHAR(100) | 通知标题 |
| message | VARCHAR(500) | 通知内容 |
| type | VARCHAR(50) | 通知类型 |
| is_read | INTEGER | 是否已读 |
| priority | INTEGER | 优先级 |
| action_link | VARCHAR(200) | 操作链接 |
| created_at | TIMESTAMP | 创建时间 |

#### 5.3.8 房型表（room_types）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INTEGER | 房型ID |
| code | VARCHAR | 房型代码（唯一） |
| name | VARCHAR | 房型名称 |
| capacity | INTEGER | 最大容纳人数 |
| base_price | DECIMAL | 基础价格 |
| active | INTEGER | 是否启用 |

### 5.4 索引设计

系统针对高频查询场景设计了以下索引：

```sql
-- 订单表索引
CREATE INDEX idx_booking_number ON bookings(booking_number);  -- 订单号查询
CREATE INDEX idx_booking_guest ON bookings(guest_id);          -- 按客户查询
CREATE INDEX idx_booking_room ON bookings(room_id);            -- 按房间查询
CREATE INDEX idx_booking_status ON bookings(status);           -- 按状态筛选
CREATE INDEX idx_booking_dates ON bookings(check_in_date, check_out_date); -- 日期范围查询

-- 评价表索引
CREATE INDEX idx_reviews_user ON reviews(user_id);             -- 按用户查询评价
CREATE INDEX idx_reviews_booking ON reviews(booking_id);       -- 按订单查询评价

-- 通知表索引
CREATE INDEX idx_user_unread ON notifications(user_id, is_read);    -- 用户未读通知
CREATE INDEX idx_user_created ON notifications(user_id, created_at); -- 用户通知时间线

-- 房型表索引
CREATE INDEX idx_room_types_code ON room_types(code);          -- 房型代码查询
CREATE INDEX idx_room_types_active ON room_types(active);      -- 启用状态查询
```

---

## API 接口设计

### 6.1 接口规范

- **基础路径**：`/api`
- **请求格式**：`application/json`
- **认证方式**：Bearer Token（JWT）
- **统一响应格式**：

```json
{
  "success": true,
  "data": { ... },
  "message": "操作成功"
}
```

### 6.2 接口总览

#### 认证模块（AuthController）

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/auth/login` | 用户登录 | 公开 |
| POST | `/api/auth/register` | 用户注册 | 公开 |
| POST | `/api/auth/logout` | 用户登出 | 公开 |
| GET | `/api/auth/me` | 获取当前用户信息 | 公开 |

#### 客房模块（RoomController）

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/rooms` | 获取客房列表（分页） | 认证 |
| GET | `/api/rooms/{id}` | 获取客房详情 | 公开 |
| POST | `/api/rooms` | 新增客房 | 认证 |
| PUT | `/api/rooms/{id}` | 更新客房信息 | 认证 |
| PATCH | `/api/rooms/{id}/status` | 更新客房状态 | 认证 |
| DELETE | `/api/rooms/{id}` | 删除客房 | 认证 |

#### 订单模块（BookingController）

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/bookings` | 获取订单列表 | 认证 |
| POST | `/api/bookings` | 创建预订 | 认证 |
| GET | `/api/bookings/{id}` | 获取订单详情 | 认证 |
| PATCH | `/api/bookings/{id}/status` | 更新订单状态 | 认证 |
| PATCH | `/api/bookings/{id}/payment` | 处理支付 | 认证 |
| DELETE | `/api/bookings/{id}` | 取消订单 | 认证 |
| GET | `/api/bookings/available-rooms` | 查询可用房间 | 公开 |

#### 客户模块（GuestController）

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/guests` | 获取客户列表 | 认证 |
| POST | `/api/guests` | 创建客户档案 | 认证 |
| PUT | `/api/guests/{id}` | 更新客户信息 | 认证 |
| DELETE | `/api/guests/{id}` | 删除客户 | 认证 |

#### 统计模块（StatisticsController）

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/statistics/dashboard` | 仪表盘统计数据 | 认证 |
| GET | `/api/statistics/trends` | 预订趋势数据 | 认证 |

#### 评价模块（ReviewController）

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/reviews` | 提交评价 | 认证 |
| GET | `/api/reviews/user` | 获取用户评价 | 认证 |

#### 设置模块（SettingsController）

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/settings/public` | 获取公开设置 | 公开 |
| GET | `/api/settings/system` | 获取系统设置 | 认证 |
| PUT | `/api/settings/system` | 更新系统设置 | 认证 |

#### 通知模块（NotificationController）

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/notifications` | 获取通知列表 | 认证 |
| PATCH | `/api/notifications/{id}/read` | 标记已读 | 认证 |

#### 个人中心（ProfileController）

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/profile` | 获取个人信息 | 认证 |
| PUT | `/api/profile` | 更新个人信息 | 认证 |
| PUT | `/api/profile/password` | 修改密码 | 认证 |

#### 房型模块（RoomTypeController）

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/room-types/active` | 获取启用的房型 | 公开 |
| GET | `/api/room-types` | 获取全部房型 | 认证 |
| POST | `/api/room-types` | 创建房型 | 认证 |
| PUT | `/api/room-types/{id}` | 更新房型 | 认证 |
| DELETE | `/api/room-types/{id}` | 删除房型 | 认证 |

---

## 项目结构

```
hotel/
├── 📁 src/main/                          # 后端源码
│   ├── 📁 java/com/hotel/
│   │   ├── 📄 HotelApplication.java      # Spring Boot 启动类
│   │   ├── 📁 config/                    # 配置类（8个）
│   │   │   ├── SecurityConfig.java       # Spring Security 配置
│   │   │   ├── JwtAuthenticationFilter.java  # JWT 过滤器
│   │   │   ├── JwtConfig.java            # JWT 配置
│   │   │   ├── CorsConfig.java           # 跨域配置
│   │   │   ├── DatabaseConfig.java       # 数据库配置
│   │   │   ├── SQLiteDialect.java        # SQLite 方言适配
│   │   │   └── FileUploadConfig.java     # 文件上传配置
│   │   ├── 📁 controller/               # 控制层（10个控制器）
│   │   │   ├── AuthController.java       # 认证接口
│   │   │   ├── BookingController.java    # 订单接口
│   │   │   ├── RoomController.java       # 客房接口
│   │   │   ├── GuestController.java      # 客户接口
│   │   │   ├── StatisticsController.java # 统计接口
│   │   │   ├── ReviewController.java     # 评价接口
│   │   │   ├── SettingsController.java   # 设置接口
│   │   │   ├── NotificationController.java  # 通知接口
│   │   │   ├── ProfileController.java    # 个人中心接口
│   │   │   └── RoomTypeController.java   # 房型接口
│   │   ├── 📁 service/                   # 业务逻辑层（10个服务类）
│   │   ├── 📁 repository/               # 数据访问层
│   │   ├── 📁 entity/                    # 实体类（15个，含枚举）
│   │   ├── 📁 dto/                       # 数据传输对象（30+个）
│   │   └── 📁 security/                  # 安全相关
│   │       ├── CompositeUserDetailsService.java
│   │       ├── GuestDetailsService.java
│   │       └── GuestDetailsImpl.java
│   └── 📁 resources/
│       ├── application.yml               # 主配置文件
│       ├── application-dev.yml           # 开发环境配置
│       ├── schema.sql                    # 数据库建表脚本
│       └── data.sql                      # 初始数据
│
├── 📁 frontend/                          # 前端源码
│   ├── 📁 src/
│   │   ├── 📄 main.ts                    # 应用入口
│   │   ├── 📄 App.vue                    # 根组件
│   │   ├── 📁 views/                     # 页面视图（16个）
│   │   │   ├── Login.vue                 # 登录页面
│   │   │   ├── Dashboard.vue             # 管理员仪表盘
│   │   │   ├── DashboardBookings.vue     # 订单监管
│   │   │   ├── Rooms.vue                 # 客房管理
│   │   │   ├── Guests.vue                # 客户管理
│   │   │   ├── Analytics.vue             # 数据分析
│   │   │   ├── Settings.vue              # 系统设置
│   │   │   ├── RoomsResource.vue         # 客房资源管理
│   │   │   ├── RoomTypes.vue             # 房型管理
│   │   │   ├── StaffBookings.vue         # 前台订单管理
│   │   │   ├── BookingWizard.vue         # 在线预订向导
│   │   │   ├── MyBookings.vue            # 我的订单
│   │   │   ├── Profile.vue               # 个人信息
│   │   │   ├── HistoryFeedback.vue       # 历史与评价
│   │   │   ├── RoomDetail.vue            # 房间详情
│   │   │   └── About.vue                 # 关于我们
│   │   ├── 📁 components/               # 公共组件（4个）
│   │   │   ├── Layout.vue                # 主布局（侧边栏+顶栏）
│   │   │   ├── SimpleHeader.vue          # 简单头部
│   │   │   ├── NotificationDropdown.vue  # 通知下拉
│   │   │   └── ChangePasswordDialog.vue  # 密码修改弹窗
│   │   ├── 📁 api/                       # API 请求封装（9个模块）
│   │   │   ├── booking.ts, room.ts, guest.ts
│   │   │   ├── roomType.ts, statistics.ts
│   │   │   ├── notification.ts, profile.ts
│   │   │   ├── review.ts, settings.ts
│   │   ├── 📁 router/                   # 路由配置
│   │   ├── 📁 types/                    # TypeScript 类型定义
│   │   ├── 📁 composables/              # Vue 组合式函数
│   │   └── 📁 utils/                    # 工具函数
│   ├── 📄 vite.config.ts                # Vite 构建配置
│   ├── 📄 tsconfig.json                 # TypeScript 配置
│   └── 📄 package.json                  # 前端依赖
│
├── 📁 data/
│   └── hotel.db                          # SQLite 数据库文件
├── 📁 uploads/                           # 文件上传目录
├── 📄 pom.xml                            # Maven 项目配置
└── 📄 README.md                          # 项目文档（本文件）
```

---

## 环境要求

### 7.1 开发环境

| 环境 | 要求 |
|------|------|
| **操作系统** | Windows 10/11、macOS、Linux |
| **Java** | JDK 11 及以上 |
| **Maven** | 3.6 及以上 |
| **Node.js** | 16.x 及以上（推荐 18.x+） |
| **npm** | 8.x 及以上 |

### 7.2 端口说明

| 服务 | 端口 | 说明 |
|------|:----:|------|
| 后端 API 服务 | 8080 | Spring Boot 内嵌 Tomcat |
| 前端开发服务器 | 3000 | Vite 开发服务器 |

---

## 快速启动

### 8.1 克隆项目

```bash
git clone <仓库地址>
cd hotel
```

### 8.2 启动后端服务

```bash
# 1. 确保已安装 JDK 11 和 Maven
java -version
mvn -version

# 2. 在项目根目录下启动 Spring Boot
mvn spring-boot:run
```

> 后端服务将在 `http://localhost:8080` 启动，首次启动时会自动创建 SQLite 数据库并执行建表脚本。

### 8.3 启动前端服务

```bash
# 1. 进入前端目录
cd frontend

# 2. 安装依赖
npm install

# 3. 启动开发服务器
npm run dev
```

> 前端服务将在 `http://localhost:3000` 启动，自动配置了反向代理将 API 请求转发至后端。

### 8.4 访问系统

打开浏览器访问：`http://localhost:3000`

**默认账号**（如有预设数据）：

| 角色 | 邮箱 | 密码 |
|------|------|------|
| 管理员 | admin@hotel.com | admin123 |
| 前台员工 | staff@hotel.com | staff123 |
| 客户 | guest@hotel.com | guest123 |

> ⚠️ 注意：以上为示例账号，如无预设数据请通过注册功能创建账户。

### 8.5 生产构建

```bash
# 前端构建
cd frontend
npm run build
# 构建产物输出至 frontend/dist 目录

# 后端打包
cd ..
mvn clean package -DskipTests
# JAR 包输出至 target/ 目录
```

---

## 系统截图与功能演示

> 以下为系统主要功能页面说明：

### 登录页面
- 支持邮箱+密码登录和新用户注册
- 登录后根据角色自动跳转至对应首页

### 管理员仪表盘
- 关键指标卡片（总预订数、营收、入住率等）
- ECharts 营收趋势图和订单统计图
- 最近预订列表

### 客房管理
- 客房列表展示（支持筛选与搜索）
- 房态实时更新（颜色标识不同状态）
- 新增/编辑客房弹窗

### 在线预订向导
- 分步式预订流程（日期→选房→信息→确认）
- 可用房间实时查询
- 自动计算费用

### 数据统计分析
- ECharts 图表可视化
- 多维度数据分析（营收、入住率、房型分布）

---

## 项目亮点与创新点

### 10.1 技术亮点

1. **前后端完全分离架构**
   - 前端 Vue 3 SPA 与后端 Spring Boot RESTful API 独立部署
   - Vite 反向代理解决跨域问题
   - 松耦合设计，前后端可独立迭代

2. **JWT 无状态认证机制**
   - 基于 JWT 的身份验证，无需服务端 Session
   - 自定义 `JwtAuthenticationFilter` 拦截器
   - Token 过期时间可配置（默认 30 天）
   - BCrypt 密码加密保障安全

3. **RBAC 多角色权限体系**
   - 三种角色差异化权限设计（ADMIN / STAFF / CUSTOMER）
   - 前端路由守卫 + 后端注解双重权限校验
   - 菜单根据角色动态渲染
   - `CompositeUserDetailsService` 组合模式支持多用户体系

4. **SQLite 轻量级数据库**
   - 无需安装独立数据库服务
   - 自定义 `SQLiteDialect` 适配 Hibernate
   - 应用启动自动执行 Schema 初始化

5. **Vue 3 Composition API + TypeScript**
   - 全面使用 `<script setup>` 语法糖
   - TypeScript 类型标注提升代码健壮性
   - API 层完全类型化

### 10.2 业务创新点

1. **向导式预订流程**
   - 分步引导用户完成预订（选择日期 → 选择房间 → 填写信息 → 确认支付）
   - 降低用户操作门槛，提升预订转化率

2. **实时通知系统**
   - 支持预订通知、系统公告等多种通知类型
   - 优先级分级管理
   - 未读消息实时提醒

3. **数据可视化驾驶舱**
   - 基于 ECharts 6.0 的多维数据可视化
   - 营收趋势、入住率、房型分布等经营指标一目了然
   - 辅助管理层数据驱动决策

4. **客户评价反馈闭环**
   - 住客可对已完成订单进行评分和评价
   - 构建完整的服务反馈闭环

### 10.3 工程化实践

- **DTO 模式**：30+ 个 DTO 类实现接口与内部实体的完全隔离
- **统一异常处理**：全局异常处理机制
- **参数校验**：基于 Hibernate Validator 的请求参数校验
- **数据库索引优化**：针对高频查询场景设计复合索引
- **环境配置分离**：`application.yml` + `application-dev.yml` 多环境支持
- **代码规约**：Lombok 简化样板代码

---

## 未来展望

- [ ] **微信/支付宝在线支付集成**：接入第三方支付接口实现真实支付
- [ ] **WebSocket 实时推送**：替代轮询方案，实现房态和消息的实时推送
- [ ] **多语言国际化（i18n）**：支持中英文切换
- [ ] **数据库升级至 MySQL/PostgreSQL**：支持更大规模数据
- [ ] **Docker 容器化部署**：一键部署方案
- [ ] **移动端适配（Responsive / App）**：提升移动端用户体验
- [ ] **AI 智能定价**：基于入住率和季节性因素的动态定价策略
- [ ] **客房物联网（IoT）集成**：门锁、空调等智能设备接入

---

## 致谢

感谢在本项目开发过程中提供指导和帮助的老师和同学们。

---

> 📌 **本项目为计算机专业本科毕业设计作品，仅用于学术研究和学习交流目的。**
