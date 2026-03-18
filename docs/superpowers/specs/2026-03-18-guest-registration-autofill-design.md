# 用户注册与预订自动填充功能设计文档

**日期**: 2026-03-18
**状态**: 设计阶段
**关联需求**:
1. 设计注册界面，对接 guests 表
2. 预订页面客人信息自动填充用户注册信息

---

## 一、概述

### 1.1 背景与目标

**当前问题**:
- 登录页面已预留注册接口，但未实现注册界面
- 预订时需要用户手动填写客人信息，重复劳动
- User 表和 Guest 表数据冗余，存在一致性风险

**解决方案**:
- 实现完整的用户注册界面
- 使用 guests 表统一管理用户认证和客人信息
- 预订时自动填充已登录用户的注册信息

### 1.2 核心决策

| 决策点 | 选择 | 理由 |
|--------|------|------|
| 用户表设计 | 使用 guests 表统一管理 | 避免数据冗余，简化数据模型 |
| 注册信息收集 | 完整信息（姓名、邮箱、密码、电话、国家） | 一次性完成，预订时无需补充 |
| 预订信息修改 | 允许修改，仅影响本次预订 | 灵活处理特殊情况（如帮他人预订） |
| 未登录访问 | 自动跳转登录 | 简化流程，确保数据完整性 |

---

## 二、架构设计

### 2.1 数据模型变更

#### Guest 实体扩展

```java
@Entity
@Table(name = "guests")
public class Guest {
    // 现有字段
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String country;
    private GuestStatus status;
    private Integer totalBookings;
    private LocalDate lastStay;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 新增字段
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.CUSTOMER;
}
```

**数据库迁移**:
- 添加 `password` 字段 (VARCHAR(255), NOT NULL)
- 添加 `role` 字段 (VARCHAR(20), NOT NULL, DEFAULT 'CUSTOMER')

#### 枚举定义

```java
public enum UserRole {
    CUSTOMER,
    ADMIN,
    STAFF
}
```

### 2.2 后端 API 变更

#### 认证服务改造

| 组件 | 变更 |
|------|------|
| AuthService | 从 UserRepository 改为 GuestRepository |
| AuthController | 保持接口不变，内部逻辑切换到 Guest |
| SecurityConfig | 登录逻辑查询 Guest 表 |
| GuestController | 保持不变，供后台管理使用 |

#### API 端点

```
POST /api/auth/register
请求体: {
  "name": "张三",
  "email": "zhangsan@example.com",
  "password": "password123",
  "phone": "+86 138 0000 0000",
  "country": "中国"
}
响应: {
  "token": "jwt_token",
  "user": { ... }
}
```

### 2.3 前端组件设计

#### 注册界面 (Login.vue 扩展)

**UI 结构**:
```
┌─────────────────────────────────────┐
│     酒店管理系统                      │
│                                     │
│   [登录] [注册]  ← 标签页切换          │
│                                     │
│   注册表单:                           │
│   ┌─────────────────────────────┐   │
│   │ 姓名                        │   │
│   └─────────────────────────────┘   │
│   ┌─────────────────────────────┐   │
│   │ 电子邮箱                    │   │
│   └─────────────────────────────┘   │
│   ┌─────────────────────────────┐   │
│   │ 密码                        │   │
│   └─────────────────────────────┘   │
│   ┌─────────────────────────────┐   │
│   │ 确认密码                    │   │
│   └─────────────────────────────┘   │
│   ┌─────────────────────────────┐   │
│   │ 联系电话                    │   │
│   └─────────────────────────────┘   │
│   ┌─────────────────────────────┐   │
│   │ 国家/地区                   │   │
│   └─────────────────────────────┘   │
│                                     │
│   [注册]                             │
│                                     │
│   已有账户？[登录]                    │
└─────────────────────────────────────┘
```

#### 预订页面自动填充 (BookingWizard.vue)

**状态管理**:
```typescript
// 从 localStorage 读取当前用户
const user = ref<User | null>(getUser())

// 客人信息表单
const guestForm = reactive({
  name: user.value?.name || '',
  phone: user.value?.phone || '',
  email: user.value?.email || '',
  notes: ''
})
```

---

## 三、用户流程

### 3.1 注册流程

```
用户访问登录页
    ↓
点击"注册新账户"
    ↓
填写注册表单（姓名、邮箱、密码、电话、国家）
    ↓
提交注册
    ↓
后端创建 Guest 记录（role=CUSTOMER）
    ↓
自动登录（返回 JWT token）
    ↓
跳转到浏览房间页面
```

### 3.2 预订流程

```
用户浏览房间
    ↓
点击"立即预订"
    ↓
检测登录状态
    ├─ 已登录 → 进入预订页面
    └─ 未登录 → 跳转登录页
        ↓
        登录/注册后返回预订页
    ↓
选择房间
    ↓
进入"客人信息"步骤
    ↓
自动填充：姓名、电话、邮箱
    ↓
用户可修改（仅影响本次预订）
    ↓
继续支付
```

---

## 四、实施清单

### 4.1 后端任务

- [ ] Guest 实体添加 password 和 role 字段
- [ ] 创建 UserRole 枚举
- [ ] 数据库迁移脚本（ALTER TABLE guests ADD COLUMN...）
- [ ] 修改 RegisterRequest DTO（添加 phone, country）
- [ ] 修改 AuthService 使用 GuestRepository
- [ ] 更新 SecurityConfig 的用户加载逻辑
- [ ] 单元测试：注册功能
- [ ] 单元测试：登录功能

### 4.2 前端任务

- [ ] Login.vue 添加注册标签页和表单
- [ ] 添加表单验证（密码匹配、必填项）
- [ ] 更新 RegisterRequest 类型定义
- [ ] 添加路由守卫（预订页面检测登录）
- [ ] BookingWizard.vue 实现自动填充
- [ ] 登录后重定向逻辑
- [ ] 错误处理和用户提示

### 4.3 测试任务

- [ ] 注册新用户端到端测试
- [ ] 注册后自动登录测试
- [ ] 预订页面自动填充测试
- [ ] 未登录用户访问预订页重定向测试
- [ ] 修改预订信息不更新个人资料测试

---

## 五、验收标准

### 功能验收

1. **注册功能**
   - [ ] 用户可以填写完整信息完成注册
   - [ ] 密码不匹配时显示错误提示
   - [ ] 邮箱已存在时显示错误提示
   - [ ] 注册成功后自动登录
   - [ ] 注册成功后跳转到浏览房间页面

2. **预订自动填充**
   - [ ] 已登录用户进入客人信息步骤时自动填充
   - [ ] 自动填充字段：姓名、电话、邮箱
   - [ ] 用户可以修改自动填充的信息
   - [ ] 修改的信息不影响个人资料

3. **未登录访问**
   - [ ] 未登录用户点击"立即预订"跳转到登录页
   - [ ] 登录成功后返回预订页面
   - [ ] 注册成功后返回预订页面

### 数据验收

- [ ] Guest 表 password 字段已加密存储
- [ ] 新注册用户 role 默认为 CUSTOMER
- [ ] 预订时客人信息正确关联到 Guest 记录

---

## 六、风险评估

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 现有 User 表数据迁移 | 中 | 保留 User 表作为备份，提供数据迁移脚本 |
| Guests 表添加字段可能影响现有功能 | 低 | 使用默认值，添加 nullable=false 前先填充数据 |
| 前后端类型不一致 | 低 | 统一 TypeScript 类型定义 |

---

## 七、后续优化

- [ ] 添加第三方登录（微信、支付宝）
- [ ] 手机号验证码登录
- [ ] 个人资料页面允许用户修改信息
- [ ] 添加"记住登录设备"功能
