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
    @Column
    private String password; // nullable: 只有注册用户才有密码，历史客人记录为空

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.CUSTOMER;
}
```

**重要说明**:
- `password` 字段设置为 nullable，因为现有的 `guests` 表已有预订记录（通过后台创建），这些记录没有密码
- 只有通过网页注册的用户才会有密码
- 历史客人记录如果需要登录，需要通过"忘记密码"流程设置密码

**数据库迁移**:
```sql
-- 添加 role 字段（有默认值，可以先添加）
ALTER TABLE guests ADD COLUMN role VARCHAR(20) DEFAULT 'CUSTOMER' NOT NULL;

-- 添加 password 字段（nullable，允许现有记录为空）
ALTER TABLE guests ADD COLUMN password VARCHAR(255);

-- 为现有管理员/员工记录创建密码（如果需要从 User 表迁移）
-- 这部分在数据迁移章节中详细说明
```

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
Content-Type: application/json

请求体:
{
  "name": "张三",
  "email": "zhangsan@example.com",
  "password": "Password@123",  // 至少8位，包含大小写字母和数字
  "phone": "+86 138 0000 0000",
  "country": "中国"
}

成功响应 (201 Created):
{
  "code": 200,
  "message": "Success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "name": "张三",
      "email": "zhangsan@example.com",
      "phone": "+86 138 0000 0000",
      "country": "中国",
      "role": "CUSTOMER"
    }
  }
}

失败响应 (400 Bad Request):
{
  "code": 400,
  "message": "Email already exists",
  "data": null
}
```

**密码加密**:
- 使用 BCrypt 算法加密密码（`BCryptPasswordEncoder`）
- 加密在注册时进行：`passwordEncoder.encode(request.getPassword())`
- 登录时验证：`passwordEncoder.matches(request.getPassword(), user.getPassword())`

### 2.3 输入验证规则

**后端验证 (RegisterRequest)**:
```java
@Data
public class RegisterRequest {
    @NotBlank(message = "姓名不能为空")
    @Size(min = 2, max = 100, message = "姓名长度必须在2-100字符之间")
    private String name;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, message = "密码长度不能少于8位")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
             message = "密码必须包含大小写字母和数字")
    private String password;

    @NotBlank(message = "电话不能为空")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$",
             message = "电话号码格式不正确")
    private String phone;

    @NotBlank(message = "国家不能为空")
    @Size(min = 1, max = 50, message = "国家名称长度必须在1-50字符之间")
    private String country;
}
```

**前端验证**:
```typescript
const registerRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 100, message: '姓名长度在2-100字符之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, message: '密码长度不能少于8位', trigger: 'blur' },
    {
      pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/,
      message: '密码必须包含大小写字母和数字',
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    {
      pattern: /^\+?[1-9]\d{1,14}$/,
      message: '请输入正确的电话号码格式',
      trigger: 'blur'
    }
  ],
  country: [
    { required: true, message: '请选择国家/地区', trigger: 'change' }
  ]
}
```

### 2.4 前端组件设计

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

// 组件挂载时自动填充客人信息
onMounted(() => {
  if (user.value) {
    guestForm.name = user.value.name || ''
    guestForm.phone = user.value.phone || ''
    guestForm.email = user.value.email || ''
  }
})

// 客人信息表单
const guestForm = reactive({
  name: '',
  phone: '',
  email: '',
  notes: ''
})
```

### 2.5 路由守卫与重定向

**需要认证的路由**:
- `/bookings/new` - 预订页面
- `/my-bookings` - 我的订单
- `/profile` - 个人资料
- `/history-feedback` - 历史反馈

**路由守卫实现** (`frontend/src/router/index.ts`):
```typescript
router.beforeEach((to, from, next) => {
  const requiresAuth = to.meta.requiresAuth
  const isPublic = to.meta.public
  const isAuthenticated = !!localStorage.getItem('token')

  // 公开页面直接放行
  if (isPublic) {
    next()
    return
  }

  // 需要认证的页面
  if (requiresAuth && !isAuthenticated) {
    // 保存目标路径，登录后跳转
    localStorage.setItem('redirectPath', to.fullPath)
    next({ name: 'login', query: { redirect: to.fullPath } })
    return
  }

  next()
})
```

**登录/注册后重定向** (`frontend/src/views/Login.vue`):
```typescript
const handleLogin = async () => {
  // ... 登录逻辑 ...

  // 获取保存的重定向路径
  const redirectPath = localStorage.getItem('redirectPath') || '/browse-rooms'
  localStorage.removeItem('redirectPath')

  // 根据用户角色跳转
  if (userRole === 'ADMIN' || userRole === 'STAFF') {
    router.push('/dashboard')
  } else {
    router.push(redirectPath)
  }
}

const handleRegister = async () => {
  // ... 注册逻辑 ...

  // 注册成功后同样重定向
  const redirectPath = localStorage.getItem('redirectPath') || '/browse-rooms'
  localStorage.removeItem('redirectPath')
  router.push(redirectPath)
}
```

### 2.6 Token 管理

**Token 配置**:
- 存储位置: `localStorage`
- Token 类型: JWT (HS256)
- 过期时间: 30天
- 刷新机制: 无（token 过期后需要重新登录）

**Token 使用**:
```typescript
// api.ts - Axios 拦截器
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token 过期或无效，清除本地数据并跳转登录
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login')
    }
    return Promise.reject(error)
  }
)
```

### 2.7 错误处理

**错误响应格式**:
```typescript
interface ErrorResponse {
  code: number
  message: string
  data: null
}
```

**常见错误码**:
| 错误码 | 场景 | 用户提示 |
|--------|------|----------|
| 400 | 邮箱已存在 | "该邮箱已被注册，请使用其他邮箱或直接登录" |
| 400 | 密码不符合要求 | "密码必须至少8位，包含大小写字母和数字" |
| 400 | 验证失败 | 显示具体字段验证错误 |
| 401 | Token 过期 | "登录已过期，请重新登录" |
| 500 | 服务器错误 | "服务器错误，请稍后重试" |

**前端错误处理**:
```typescript
const handleRegister = async () => {
  loading.value = true
  try {
    await register(registerForm.email, registerForm.password, registerForm.name)
    ElMessage.success('注册成功！')
    // ... 跳转逻辑 ...
  } catch (error: any) {
    const message = error.response?.data?.message || '注册失败，请重试'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}
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
- [ ] 添加输入验证规则（@Pattern 注解）
- [ ] 修改 AuthService 使用 GuestRepository
- [ ] 更新 SecurityConfig 的用户加载逻辑
- [ ] 实现 BCrypt 密码加密
- [ ] 单元测试：注册功能
- [ ] 单元测试：登录功能
- [ ] 单元测试：密码加密验证

### 4.2 前端任务

- [ ] Login.vue 添加注册标签页和表单
- [ ] 添加表单验证（密码匹配、强度检查、必填项）
- [ ] 更新 RegisterRequest 类型定义（添加 phone, country）
- [ ] 添加路由守卫（检测登录状态和保存重定向路径）
- [ ] BookingWizard.vue 实现自动填充
- [ ] 登录/注册后重定向逻辑
- [ ] Axios 拦截器处理 401 错误
- [ ] 错误处理和用户提示

### 4.3 测试任务

- [ ] 注册新用户端到端测试
- [ ] 注册后自动登录测试
- [ ] 密码强度验证测试（太短、缺少大小写等）
- [ ] 邮箱重复注册测试
- [ ] 密码不匹配验证测试
- [ ] 预订页面自动填充测试
- [ ] 自动填充数据准确性测试
- [ ] 修改预订信息不更新个人资料测试
- [ ] 未登录用户访问预订页重定向测试
- [ ] 登录后返回原页面测试
- [ ] Token 过期后跳转登录测试
- [ ] 并发注册测试（相同邮箱）

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
| Guests 表添加字段可能影响现有功能 | 低 | password 字段设置为 nullable，允许现有记录为空 |
| 前后端类型不一致 | 低 | 统一 TypeScript 类型定义 |

---

## 七、数据迁移策略

### 7.1 User 表到 Guest 表迁移

**迁移场景**: 如果需要将现有 User 表的用户迁移到 Guest 表

**迁移脚本**:
```sql
-- 步骤 1: 备份现有数据
CREATE TABLE users_backup AS SELECT * FROM users;
CREATE TABLE guests_backup AS SELECT * FROM guests;

-- 步骤 2: 为已存在的邮箱用户在 Guest 表创建记录（如果不存在）
INSERT INTO guests (name, email, phone, country, password, role, status, total_bookings, created_at, updated_at)
SELECT
    u.name,
    u.email,
    COALESCE(g.phone, ''),  -- 如果 guest 已有电话，使用 guest 的
    COALESCE(g.country, '中国'),
    u.password,
    u.role,
    'ACTIVE',
    0,
    u.created_at,
    u.updated_at
FROM users u
LEFT JOIN guests g ON g.email = u.email
WHERE NOT EXISTS (
    SELECT 1 FROM guests WHERE guests.email = u.email AND guests.password IS NOT NULL
);

-- 步骤 3: 验证迁移结果
SELECT
    (SELECT COUNT(*) FROM users) as user_count,
    (SELECT COUNT(*) FROM guests WHERE password IS NOT NULL) as migrated_guest_count;
```

### 7.2 迁移验证

**验证步骤**:
1. 确认 User 表中的所有用户都在 Guest 表中有对应记录
2. 测试迁移后用户的登录功能
3. 确认原有预订记录关联正确
4. 验证用户角色正确迁移

### 7.3 回滚方案

如果迁移出现问题，执行以下回滚：
```sql
-- 恢复 Guest 表
DROP TABLE guests;
ALTER TABLE guests_backup RENAME TO guests;

-- 恢复 User 表
DROP TABLE users;
ALTER TABLE users_backup RENAME TO users;
```

### 7.4 渐进式迁移方案

为了降低风险，可以采用渐进式迁移：

**阶段 1**: 新系统并行运行
- 保留 User 表和 Guest 表
- 注册时同时创建两个表的记录

**阶段 2**: 逐步迁移
- 优先迁移活跃用户
- 老用户登录时自动迁移到 Guest 表

**阶段 3**: 完全切换
- 确认所有用户迁移完成后
- 停止写入 User 表

**阶段 4**: 清理
- 经过一段观察期后
- 删除 User 表

---

## 八、重要设计决策说明

### 8.1 为什么使用 Guests 表统一管理？

**决策**: 使用 guests 表同时存储认证信息和客人信息

**权衡考虑**:
- ✅ **优点**: 简化数据模型，避免冗余，注册即完成
- ⚠️ **限制**: 预订客人可能不需要注册账号
- ✅ **缓解**: 允许预订时修改客人信息（不影响注册信息）

**适用场景**: 这个设计适合中小型酒店，大部分预订来自注册用户。如果需要支持大量未注册用户预订，可以考虑后续扩展"快速预订"功能。

### 8.2 前端状态管理说明

**当前方案**: 使用 localStorage 存储用户信息和 token

**存储内容**:
```typescript
localStorage.setItem('token', jwtToken)
localStorage.setItem('user', JSON.stringify({
  id: user.id,
  name: user.name,
  email: user.email,
  phone: user.phone,
  country: user.country,
  role: user.role
}))
```

**更新机制**:
- 登录/注册时写入
- 退出登录时清除
- Token 过期时清除
- 如果用户修改个人资料，同步更新 localStorage

**注意事项**:
- localStorage 在不同标签页之间同步
- 敏感信息（密码）永不存储在 localStorage
- Token 通过 HTTP Header 发送，不暴露在 URL 中

---

## 九、后续优化

- [ ] 添加第三方登录（微信、支付宝）
- [ ] 手机号验证码登录
- [ ] 个人资料页面允许用户修改信息
- [ ] 添加"记住登录设备"功能
