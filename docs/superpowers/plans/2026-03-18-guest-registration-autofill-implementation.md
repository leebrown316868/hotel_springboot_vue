# 用户注册与预订自动填充功能实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**目标:** 实现用户注册界面，使用 guests 表统一管理认证信息，预订页面自动填充用户信息

**架构:** 使用 guests 表同时存储认证信息（password, role）和客人信息，前端通过标签页切换登录/注册，预订时从 localStorage 读取用户信息自动填充

**技术栈:** Spring Boot (后端), Vue 3 + TypeScript + Element Plus (前端), JWT 认证, BCrypt 密码加密

---

## 文件结构

### 后端文件 (Spring Boot)
```
src/main/java/com/hotel/
├── entity/
│   ├── Guest.java                    [修改] 添加 password 和 role 字段
├── dto/
│   ├── RegisterRequest.java          [修改] 添加 phone, country 字段和验证规则
├── repository/
│   ├── GuestRepository.java          [现有] 已有 findByEmail, existsByEmail 方法
├── service/
│   ├── AuthService.java              [修改] 使用 GuestRepository 替代 UserRepository
│   └── impl/
│       └── AuthServiceImpl.java      [新建或修改] 实现注册逻辑
├── security/
│   ├── GuestDetailsImpl.java         [新建] Guest 的 UserDetails 实现
│   └── GuestDetailsService.java      [新建] 从 Guest 表加载用户
└── config/
    └── SecurityConfig.java           [修改] 使用 GuestDetailsService
```

### 前端文件 (Vue 3)
```
frontend/src/
├── types/
│   └── auth.ts                       [修改] 更新 RegisterRequest 接口
├── views/
│   ├── Login.vue                     [修改] 添加注册标签页和表单
│   └── BookingWizard.vue             [修改] 添加自动填充逻辑
├── router/
│   └── index.ts                      [修改] 添加路由守卫
├── utils/
│   └── auth.ts                       [修改] 更新 register 函数签名
└── api/
    └── api.ts                        [检查] 确保 401 拦截器正确
```

---

## 任务列表

### Task 1: 数据库迁移 - 添加新字段

**文件:** 数据库
- Modify: `data/hotel.db` (SQLite 数据库)

- [ ] **Step 1: 停止后端应用**

运行: `taskkill //F //IM java.exe`
Expected: 所有 Java 进程终止

- [ ] **Step 2: 备份数据库**

运行: `copy data\hotel.db data\hotel.db.backup`
Expected: 数据库备份文件创建

- [ ] **Step 3: 执行数据库迁移**

```sql
-- 添加 role 字段（有默认值，可以先添加）
ALTER TABLE guests ADD COLUMN role VARCHAR(20) DEFAULT 'CUSTOMER' NOT NULL;

-- 添加 password 字段（nullable，允许现有记录为空）
ALTER TABLE guests ADD COLUMN password VARCHAR(255);
```

运行: 在 SQLite 工具中执行上述 SQL
Expected: 两个新字段添加成功

- [ ] **Step 4: 验证迁移结果**

运行: `SELECT sql FROM sqlite_master WHERE type='table' AND name='guests';`
Expected: guests 表包含 role 和 password 字段

- [ ] **Step 5: 验证现有数据**

运行:
```sql
-- 检查现有 guests 记录数
SELECT COUNT(*) as total_guests FROM guests;

-- 检查 users 表记录数（用于后续迁移参考）
SELECT COUNT(*) as total_users FROM users;
```
Expected: 返回记录数

- [ ] **Step 6: 启动后端验证**

运行: `mvn spring-boot:run` (后台运行)
Expected: 后端启动无错误

---

### Task 2: Guest 实体添加新字段

**文件:**
- Modify: `src/main/java/com/hotel/entity/Guest.java`

- [ ] **Step 1: 在 Guest.java 中添加新字段**

```java
// 在现有字段后添加以下代码

@Column
private String password; // nullable: 只有注册用户才有密码

@Enumerated(EnumType.STRING)
@Column(nullable = false)
private UserRole role = UserRole.CUSTOMER;
```

位置: 在 `updatedAt` 字段之前添加

- [ ] **Step 2: 确保 UserRole 枚举已导入**

```java
import com.hotel.entity.UserRole;
```

位置: 文件顶部的 import 区域

- [ ] **Step 3: 编译验证**

运行: `mvn compile`
Expected: 编译成功，无错误

- [ ] **Step 4: Git commit**

```bash
git add src/main/java/com/hotel/entity/Guest.java
git commit -m "feat(entity): 添加 Guest password 和 role 字段"
```

---

### Task 3: 更新 RegisterRequest DTO

**文件:**
- Modify: `src/main/java/com/hotel/dto/RegisterRequest.java`

- [ ] **Step 1: 添加 phone 和 country 字段及验证规则**

将整个文件替换为:

```java
package com.hotel.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

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

- [ ] **Step 2: 编译验证**

运行: `mvn compile`
Expected: 编译成功

- [ ] **Step 3: Git commit**

```bash
git add src/main/java/com/hotel/dto/RegisterRequest.java
git commit -m "feat(dto): 更新 RegisterRequest 添加 phone, country 字段和验证规则"
```

---

### Task 4: 创建 GuestDetailsImpl

**文件:**
- Create: `src/main/java/com/hotel/security/GuestDetailsImpl.java`

- [ ] **Step 1: 创建 GuestDetailsImpl 类**

```java
package com.hotel.security;

import com.hotel.entity.Guest;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data
@AllArgsConstructor
public class GuestDetailsImpl implements UserDetails {

    private Guest guest;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + guest.getRole().name())
        );
    }

    @Override
    public String getPassword() {
        return guest.getPassword();
    }

    @Override
    public String getUsername() {
        return guest.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 添加 null 检查防止 NPE
        return guest.getStatus() != null && guest.getStatus() == com.hotel.entity.GuestStatus.ACTIVE;
    }
}
```

- [ ] **Step 2: 编译验证**

运行: `mvn compile`
Expected: 编译成功

- [ ] **Step 3: Git commit**

```bash
git add src/main/java/com/hotel/security/GuestDetailsImpl.java
git commit -m "feat(security): 创建 GuestDetailsImpl 实现 UserDetails"
```

---

### Task 5: 更新 JwtUtil 支持 GuestDetailsImpl

**文件:**
- Modify: `src/main/java/com/hotel/util/JwtUtil.java`

- [ ] **Step 1: 更新 generateToken 方法支持 GuestDetailsImpl**

将现有的 `generateToken(UserDetails userDetails)` 方法更新为:

```java
public String generateToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    if (userDetails instanceof com.hotel.security.UserDetailsImpl) {
        com.hotel.security.UserDetailsImpl customUserDetails = (com.hotel.security.UserDetailsImpl) userDetails;
        claims.put("id", customUserDetails.getId());
        claims.put("role", customUserDetails.getRole());
    } else if (userDetails instanceof com.hotel.security.GuestDetailsImpl) {
        com.hotel.security.GuestDetailsImpl guestDetails = (com.hotel.security.GuestDetailsImpl) userDetails;
        claims.put("id", guestDetails.getGuest().getId());
        claims.put("role", guestDetails.getGuest().getRole().name());
    }
    return createToken(claims, userDetails.getUsername());
}
```

注意: 这个修改保持了向后兼容性，原有的 UserDetailsImpl 仍然正常工作。

- [ ] **Step 2: 编译验证**

运行: `mvn compile`
Expected: 编译成功

- [ ] **Step 3: Git commit**

```bash
git add src/main/java/com/hotel/util/JwtUtil.java
git commit -m "feat(util): JwtUtil 支持 GuestDetailsImpl"
```

---

### Task 6: 创建 GuestDetailsService

**文件:**
- Create: `src/main/java/com/hotel/security/GuestDetailsService.java`

- [ ] **Step 1: 创建 GuestDetailsService 类**

```java
package com.hotel.security;

import com.hotel.entity.Guest;
import com.hotel.repository.GuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GuestDetailsService implements UserDetailsService {

    private final GuestRepository guestRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Guest guest = guestRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Guest not found with email: " + email));

        // 只有有密码的 Guest 才能登录（通过注册创建的用户）
        if (guest.getPassword() == null || guest.getPassword().isEmpty()) {
            throw new UsernameNotFoundException("Guest account has no password set. Please reset password.");
        }

        return new GuestDetailsImpl(guest);
    }
}
```

- [ ] **Step 2: 编译验证**

运行: `mvn compile`
Expected: 编译成功

- [ ] **Step 3: Git commit**

```bash
git add src/main/java/com/hotel/security/GuestDetailsService.java
git commit -m "feat(security): 创建 GuestDetailsService 从 Guest 表加载用户"
```

---

### Task 7: 更新 SecurityConfig

**文件:**
- Modify: `src/main/java/com/hotel/config/SecurityConfig.java`

- [ ] **Step 1: 注入 GuestDetailsService 和 PasswordEncoder**

在类的开头添加:

```java
private final GuestDetailsService guestDetailsService;
private final PasswordEncoder passwordEncoder;
private final JwtAuthenticationFilter jwtAuthenticationFilter;
```

并在类定义上更新 `@RequiredArgsConstructor`（已有，确保包含这些字段）

- [ ] **Step 2: 添加 DaoAuthenticationProvider bean**

在类中添加新的 bean 方法:

```java
@Bean
public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(guestDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder);
    return authProvider;
}
```

- [ ] **Step 3: 更新 authenticationManager bean**

替换现有的 `authenticationManager` bean 为:

```java
@Bean
public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
}
```

注意: Spring Security 会自动使用我们定义的 `authenticationProvider()` bean。

- [ ] **Step 4: 编译验证**

运行: `mvn compile`
Expected: 编译成功

- [ ] **Step 5: Git commit**

```bash
git add src/main/java/com/hotel/config/SecurityConfig.java
git commit -m "feat(config): 更新 SecurityConfig 使用 GuestDetailsService"
```

---

### Task 8: 更新 AuthService 注册逻辑

**文件:**
- Modify: `src/main/java/com/hotel/service/AuthService.java`
- Modify: `src/main/java/com/hotel/service/impl/AuthServiceImpl.java`

- [ ] **Step 1: 读取现有 AuthService 实现**

运行: 查看现有代码结构
Expected: 了解当前实现

- [ ] **Step 2: 更新 AuthService 接口**

确保 register 方法接受完整的 RegisterRequest（已在 Task 3 更新）

- [ ] **Step 3: 更新 AuthServiceImpl 使用 GuestRepository**

将 register 方法实现更新为:

```java
@Override
public AuthResponse register(RegisterRequest request) {
    // 检查邮箱是否已存在
    if (guestRepository.existsByEmail(request.getEmail())) {
        throw new AuthenticationException("Email already exists");
    }

    // 创建新 Guest
    Guest guest = Guest.builder()
            .name(request.getName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .phone(request.getPhone())
            .country(request.getCountry())
            .role(UserRole.CUSTOMER)
            .status(GuestStatus.ACTIVE)
            .totalBookings(0)
            .build();

    guest = guestRepository.save(guest);

    // 使用 GuestDetailsImpl 生成 token
    GuestDetailsImpl userDetails = new GuestDetailsImpl(guest);
    String token = jwtUtil.generateToken(userDetails);

    return AuthResponse.builder()
            .token(token)
            .user(mapToUserResponse(guest))
            .build();
}
```

同时更新 login 方法使用 GuestRepository:

```java
@Override
public AuthResponse login(AuthRequest request) {
    // 通过 GuestDetailsService 加载用户
    UserDetails userDetails = guestDetailsService.loadUserByUsername(request.getEmail());

    // 验证密码
    if (!passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
        throw new AuthenticationException("Invalid email or password");
    }

    // 生成 token
    String token = jwtUtil.generateToken(userDetails);

    // 获取 Guest 实体用于返回
    Guest guest = guestRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new AuthenticationException("Guest not found"));

    return AuthResponse.builder()
            .token(token)
            .user(mapToUserResponse(guest))
            .build();
}
```

- [ ] **Step 4: 更新 getCurrentUser 方法**

```java
@Override
public UserResponse getCurrentUser(String email) {
    Guest guest = guestRepository.findByEmail(email)
            .orElseThrow(() -> new AuthenticationException("Guest not found"));

    return mapToUserResponse(guest);
}
```

- [ ] **Step 5: 更新依赖注入**

```java
private final GuestRepository guestRepository;
private final GuestDetailsService guestDetailsService;
private final PasswordEncoder passwordEncoder;
private final JwtUtil jwtUtil;
```

- [ ] **Step 6: 编译验证**

运行: `mvn compile`
Expected: 编译成功

- [ ] **Step 7: Git commit**

```bash
git add src/main/java/com/hotel/service/AuthService.java src/main/java/com/hotel/service/impl/AuthServiceImpl.java
git commit -m "feat(service): 更新 AuthService 使用 GuestRepository"
```

---

### Task 9: 更新 UserResponse DTO

**文件:**
- Modify: `src/main/java/com/hotel/dto/UserResponse.java`

- [ ] **Step 1: 添加 phone 和 country 字段**

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String country;
    private String role;
}
```

- [ ] **Step 2: 更新 AuthServiceImpl 的 mapToUserResponse 方法**

```java
private UserResponse mapToUserResponse(Guest guest) {
    return UserResponse.builder()
            .id(guest.getId())
            .name(guest.getName())
            .email(guest.getEmail())
            .phone(guest.getPhone())
            .country(guest.getCountry())
            .role(guest.getRole().name())
            .build();
}
```

- [ ] **Step 3: 编译验证**

运行: `mvn compile`
Expected: 编译成功

- [ ] **Step 4: Git commit**

```bash
git add src/main/java/com/hotel/dto/UserResponse.java src/main/java/com/hotel/service/impl/AuthServiceImpl.java
git commit -m "feat(dto): 更新 UserResponse 添加 phone, country 字段"
```

---

### Task 10: 后端集成测试

**文件:**
- Test: 通过 curl 或 Postman 测试

- [ ] **Step 1: 启动后端**

运行: `mvn spring-boot:run` (后台运行)
Expected: 后端启动成功

- [ ] **Step 2: 测试注册功能**

运行:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Test User\",\"email\":\"test@example.com\",\"password\":\"Password@123\",\"phone\":\"+86 13800000000\",\"country\":\"中国\"}"
```

Expected:
```json
{"code":200,"message":"Success","data":{"token":"...","user":{"id":1,"name":"Test User","email":"test@example.com","phone":"+86 13800000000","country":"中国","role":"CUSTOMER"}}}
```

- [ ] **Step 3: 测试重复注册**

运行: 再次执行 Step 2 的命令
Expected: `{"code":400,"message":"Email already exists"}`

- [ ] **Step 4: 测试登录功能**

运行:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"test@example.com\",\"password\":\"Password@123\"}"
```

Expected: 返回 token 和用户信息

- [ ] **Step 5: 测试弱密码注册**

运行:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Test2\",\"email\":\"test2@example.com\",\"password\":\"weak\",\"phone\":\"+86 13800000001\",\"country\":\"中国\"}"
```

Expected: 返回 400 错误，提示密码不符合要求

- [ ] **Step 6: Git commit**

```bash
git commit --allow-empty -m "test: 后端注册和登录功能测试通过"
```

---

### Task 11: 更新前端类型定义

**文件:**
- Modify: `frontend/src/types/auth.ts`

- [ ] **Step 1: 更新 User 接口**

```typescript
export interface User {
  id: number
  email: string
  name: string
  phone: string
  country: string
  role: string
}
```

- [ ] **Step 2: 更新 RegisterRequest 接口**

```typescript
export interface RegisterRequest {
  email: string
  password: string
  name: string
  phone: string
  country: string
}
```

- [ ] **Step 3: Git commit**

```bash
git add frontend/src/types/auth.ts
git commit -m "feat(types): 更新 User 和 RegisterRequest 类型定义"
```

---

### Task 12: 更新前端 auth.ts

**文件:**
- Modify: `frontend/src/utils/auth.ts`

- [ ] **Step 1: 更新 register 函数签名**

```typescript
export const register = async (
  email: string,
  password: string,
  name: string,
  phone: string,
  country: string
): Promise<AuthResponse> => {
  const response = await api.post<ApiResponse<AuthResponse>>('/api/auth/register', {
    email,
    password,
    name,
    phone,
    country
  })
  const { token, user } = response.data.data

  // Store token and user info
  localStorage.setItem('token', token)
  localStorage.setItem('user', JSON.stringify(user))

  return response.data.data
}
```

- [ ] **Step 2: Git commit**

```bash
git add frontend/src/utils/auth.ts
git commit -m "feat(utils): 更新 register 函数接受完整参数"
```

---

### Task 13: 更新 Login.vue 添加注册表单

**文件:**
- Modify: `frontend/src/views/Login.vue`

- [ ] **Step 1: 添加标签页状态**

在 script setup 中添加:

```typescript
const activeTab = ref<'login' | 'register'>('login')
```

- [ ] **Step 2: 添加注册表单数据**

```typescript
const registerForm = reactive({
  name: '',
  email: '',
  password: '',
  confirmPassword: '',
  phone: '',
  country: '',
  remember: false
})
```

- [ ] **Step 3: 添加注册表单验证规则**

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

- [ ] **Step 4: 添加国家选项**

```typescript
const countryOptions = [
  '中国', '美国', '英国', '日本', '韩国', '新加坡', '马来西亚', '澳大利亚', '加拿大', '其他'
]
```

- [ ] **Step 5: 添加注册处理函数**

```typescript
const registerLoading = ref(false)

const handleRegister = async () => {
  // 表单验证
  const registerFormRef = ref<any>()
  await registerFormRef.value?.validate()

  if (registerForm.password !== registerForm.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }

  registerLoading.value = true

  try {
    await register(
      registerForm.email,
      registerForm.password,
      registerForm.name,
      registerForm.phone,
      registerForm.country
    )

    ElMessage.success('注册成功！正在跳转...')

    // 获取保存的重定向路径（优先使用 localStorage，其次使用 URL 查询参数）
    const redirectPath = localStorage.getItem('redirectPath') ||
                         router.currentRoute.value.query.redirect as string ||
                         '/browse-rooms'
    localStorage.removeItem('redirectPath')
    router.push(redirectPath)
  } catch (error: any) {
    const message = error.response?.data?.message || '注册失败，请重试'
    ElMessage.error(message)
  } finally {
    registerLoading.value = false
  }
}
```

- [ ] **Step 6: 更新登录处理函数支持重定向**

```typescript
const handleLogin = async () => {
  // ... 现有登录逻辑代码 ...

  // 获取保存的重定向路径（优先使用 localStorage，其次使用 URL 查询参数）
  const redirectPath = localStorage.getItem('redirectPath') ||
                       router.currentRoute.value.query.redirect as string ||
                       '/browse-rooms'
  localStorage.removeItem('redirectPath')

  // 根据用户角色跳转
  const userRole = authResponse.user.role
  if (userRole === 'ADMIN' || userRole === 'STAFF') {
    router.push('/dashboard')
  } else {
    router.push(redirectPath)
  }
}
```

注意: 这确保了重定向在两种情况下都能工作：
1. 路由守卫保存的 redirectPath
2. URL 查询参数中的 redirect（如 ?redirect=/bookings/new）

- [ ] **Step 7: 更新模板添加标签页和注册表单**

在 template 中，将登录表单包裹在标签页中，并添加注册表单。完整模板结构:

```vue
<template>
  <div class="h-screen w-full overflow-hidden">
    <div class="login-bg h-full w-full flex items-center justify-center relative">
      <div class="overlay absolute inset-0"></div>

      <main class="relative z-10 w-full max-w-md px-6">
        <div class="bg-white rounded-2xl shadow-2xl p-8 md:p-10">
          <div class="text-center mb-8">
            <!-- 标题保持不变 -->
          </div>

          <!-- 标签页 -->
          <el-tabs v-model="activeTab" class="mb-6">
            <el-tab-pane label="登录" name="login"></el-tab-pane>
            <el-tab-pane label="注册" name="register"></el-tab-pane>
          </el-tabs>

          <!-- 登录表单 -->
          <div v-show="activeTab === 'login'">
            <!-- 现有登录表单 -->
          </div>

          <!-- 注册表单 -->
          <el-form
            v-show="activeTab === 'register'"
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            @submit.prevent="handleRegister"
            class="space-y-6"
          >
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">姓名</label>
              <el-input
                v-model="registerForm.name"
                placeholder="请输入姓名"
                clearable
                size="large"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">电子邮箱</label>
              <el-input
                v-model="registerForm.email"
                type="email"
                placeholder="your@email.com"
                clearable
                size="large"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">密码</label>
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="至少8位，包含大小写字母和数字"
                show-password
                size="large"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">确认密码</label>
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="再次输入密码"
                show-password
                size="large"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">联系电话</label>
              <el-input
                v-model="registerForm.phone"
                placeholder="+86 138 0000 0000"
                clearable
                size="large"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">国家/地区</label>
              <el-select
                v-model="registerForm.country"
                placeholder="请选择国家/地区"
                class="w-full"
                size="large"
              >
                <el-option
                  v-for="country in countryOptions"
                  :key="country"
                  :label="country"
                  :value="country"
                />
              </el-select>
            </div>

            <div>
              <el-button
                type="primary"
                native-type="submit"
                class="w-full"
                :loading="registerLoading"
                size="large"
              >
                注册
              </el-button>
            </div>
          </el-form>

          <!-- 底部链接 -->
          <footer class="mt-8 pt-6 border-t border-gray-100 text-center">
            <p class="text-sm text-gray-600" v-if="activeTab === 'login'">
              还没有账户？
              <a @click="activeTab = 'register'" class="font-semibold text-blue-600 hover:text-blue-700 transition-colors cursor-pointer">注册新账户</a>
            </p>
            <p class="text-sm text-gray-600" v-else>
              已有账户？
              <a @click="activeTab = 'login'" class="font-semibold text-blue-600 hover:text-blue-700 transition-colors cursor-pointer">立即登录</a>
            </p>
          </footer>
        </div>

        <p class="text-center text-white/60 text-xs mt-8">
          © 2023 Grand Horizon 酒店集团。保留所有权利。
        </p>
      </main>
    </div>
  </div>
</template>
```

- [ ] **Step 8: Git commit**

```bash
git add frontend/src/views/Login.vue
git commit -m "feat(views): Login.vue 添加注册标签页和表单"
```

---

### Task 14: 添加路由守卫

**文件:**
- Modify: `frontend/src/router/index.ts`

- [ ] **Step 1: 更新路由守卫**

将现有的 `beforeEach` 守卫更新为（保持现有逻辑，添加重定向支持）:

```typescript
// Navigation guard for authentication
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

  // 已登录用户访问登录页，根据角色跳转
  // 注意：只有当没有 redirect 参数时才自动跳转
  if (to.path === '/login' && isAuthenticated && !to.query.redirect) {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    if (user.role === 'ADMIN' || user.role === 'STAFF') {
      return next({ name: 'dashboard' })
    } else {
      return next({ name: 'browse-rooms' })
    }
  }

  // 不返回任何值 = 允许导航通过
  next()
})
```

- [ ] **Step 2: 为 /bookings/new 添加 requiresAuth**

```typescript
{
  path: '/bookings/new',
  name: 'booking-wizard',
  component: () => import('../views/BookingWizard.vue'),
  meta: { requiresAuth: true }  // 改为 requiresAuth
}
```

- [ ] **Step 3: Git commit**

```bash
git add frontend/src/router/index.ts
git commit -m "feat(router): 添加路由守卫和重定向支持"
```

---

### Task 15: 更新 BookingWizard.vue 自动填充

**文件:**
- Modify: `frontend/src/views/BookingWizard.vue`

- [ ] **Step 1: 导入 getUser 函数**

在 script setup 中:

```typescript
import { getUser } from '@/utils/auth'
```

- [ ] **Step 2: 在组件挂载时自动填充**

在 `onMounted` 中添加:

```typescript
onMounted(async () => {
  await fetchRoomTypesConfig()

  // 自动填充客人信息
  const user = getUser()
  if (user) {
    guestForm.name = user.name || ''
    guestForm.phone = user.phone || ''
    guestForm.email = user.email || ''
  }

  handleSearch()
})
```

- [ ] **Step 3: 更新 User 类型定义**

确保 types/auth.ts 中的 User 接口包含 phone 和 country:

```typescript
export interface User {
  id: number
  email: string
  name: string
  phone: string
  country: string
  role: string
}
```

- [ ] **Step 4: Git commit**

```bash
git add frontend/src/views/BookingWizard.vue frontend/src/types/auth.ts
git commit -m "feat(views): BookingWizard 添加客人信息自动填充"
```

---

### Task 16: 确认 API 401 拦截器

**文件:**
- Check: `frontend/src/api/api.ts`

- [ ] **Step 1: 检查 401 拦截器**

确保有以下代码:

```typescript
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

- [ ] **Step 2: 如果没有，添加拦截器**

- [ ] **Step 3: Git commit** (如果有修改)

```bash
git add frontend/src/api/api.ts
git commit -m "fix(api): 确保 401 拦截器正确处理"
```

---

### Task 17: 前端集成测试

**文件:**
- Test: 浏览器手动测试

- [ ] **Step 1: 启动前端**

运行: `taskkill //F //IM bash.exe` 然后 `npm run dev` (后台运行)
Expected: 前端启动成功

- [ ] **Step 2: 测试未登录访问预订页**

访问: `http://localhost:3000/bookings/new`
Expected: 自动跳转到登录页，URL 包含 redirect 参数

- [ ] **Step 3: 测试注册功能**

1. 访问 `http://localhost:3000/login`
2. 点击"注册"标签
3. 填写完整表单
4. 提交注册

Expected: 注册成功，跳转到浏览房间页面

- [ ] **Step 4: 测试注册后登录状态**

1. 检查 localStorage 中有 token 和 user
2. 刷新页面，保持登录状态

Expected: 保持登录状态

- [ ] **Step 5: 测试预订自动填充**

1. 登录后访问预订页
2. 选择房间进入 Step 3

Expected: 客人信息自动填充（姓名、电话、邮箱）

- [ ] **Step 6: 测试密码验证**

1. 尝试注册弱密码（如 "123456"）
2. 尝试密码不匹配

Expected: 显示相应的错误提示

- [ ] **Step 7: 测试重复邮箱注册**

1. 使用已存在的邮箱注册

Expected: 显示"邮箱已存在"错误

- [ ] **Step 8: 测试修改预订信息**

1. 在自动填充后修改客人信息
2. 完成预订
3. 检查 localStorage 中的用户信息未变化

Expected: 预订信息修改不影响用户信息

- [ ] **Step 9: Git commit**

```bash
git commit --allow-empty -m "test: 前端注册和自动填充功能测试通过"
```

---

### Task 18: 端到端测试

**文件:**
- Test: 完整流程测试

- [ ] **Step 1: 清理测试数据**

删除测试用户记录

- [ ] **Step 2: 完整注册到预订流程**

1. 访问浏览房间页面
2. 点击"立即预订" → 跳转登录
3. 切换到注册标签
4. 填写注册信息提交
5. 自动返回预订页面
6. 搜索房间
7. 选择房间
8. 确认自动填充
9. 完成预订

Expected: 整个流程无错误

- [ ] **Step 3: Git commit**

```bash
git commit --allow-empty -m "test: 端到端注册和预订流程测试通过"
```

---

### Task 19: 文档更新

**文件:**
- Update: `E:\project\hotel\问题.md` (如果需要)

- [ ] **Step 1: 更新问题文档**

记录本次实施的关键信息

- [ ] **Step 2: 最终 Git commit**

```bash
git add docs/
git commit -m "docs: 更新问题文档"
```

---

## 验收检查清单

### 功能验收

- [ ] 用户可以填写完整信息完成注册
- [ ] 密码不匹配时显示错误提示
- [ ] 邮箱已存在时显示错误提示
- [ ] 密码强度验证正常工作
- [ ] 注册成功后自动登录
- [ ] 注册成功后跳转到原目标页面或浏览房间页面
- [ ] 未登录用户点击"立即预订"跳转到登录页
- [ ] 登录成功后返回预订页面
- [ ] 已登录用户进入客人信息步骤时自动填充
- [ ] 自动填充字段：姓名、电话、邮箱
- [ ] 用户可以修改自动填充的信息
- [ ] 修改的信息不影响个人资料

### 数据验收

- [ ] Guest 表 password 字段已加密存储（BCrypt）
- [ ] 新注册用户 role 默认为 CUSTOMER
- [ ] 历史客人记录 password 为 null
- [ ] 预订时客人信息正确关联到 Guest 记录

---

## 回滚计划

如果出现问题，执行以下回滚:

### 数据库回滚

**方法 1: 使用备份恢复**
```bash
# 1. 停止后端应用
taskkill //F //IM java.exe

# 2. 恢复数据库备份
copy data\hotel.db.backup data\hotel.db

# 3. 验证恢复
sqlite3 data\hotel.db "SELECT sql FROM sqlite_master WHERE type='table' AND name='guests';"

# 4. 重启后端
mvn spring-boot:run
```

**方法 2: 手动删除字段（如果备份不可用）**
```sql
-- SQLite 不支持 DROP COLUMN，需要重建表
CREATE TABLE guests_new AS SELECT id, name, email, phone, country, status, total_bookings, last_stay, created_at, updated_at FROM guests;
DROP TABLE guests;
ALTER TABLE guests_new RENAME TO guests;
```

### 代码回滚
```bash
# 查看实施前的 commit
git log --oneline -10

# 回滚到指定 commit
git reset --hard <commit-before-implementation>

# 如果已经推送到远程，使用强制回滚（谨慎使用）
git push origin master --force
```

### 回滚验证
- [ ] 后端启动成功
- [ ] 现有功能正常工作
- [ ] 数据库结构正确

---

## 参考资料

- 设计文档: `docs/superpowers/specs/2026-03-18-guest-registration-autofill-design.md`
- Spring Security 文档: https://docs.spring.io/spring-security/reference/
- Element Plus 文档: https://element-plus.org/
