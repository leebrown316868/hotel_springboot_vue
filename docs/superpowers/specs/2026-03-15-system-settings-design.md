# 系统设置功能设计

**日期**: 2026-03-15
**作者**: Claude
**状态**: 版本 2

## 1. 概述

实现全局系统设置功能，包括：
1. **系统设置模块**：酒店基本信息配置、安全设置、通知偏好
2. **通知系统模块**：通知实体、API、铃铛按钮集成
3. **密码管理模块**：修改密码功能扩展

## 2. 数据库设计

### 2.1 SystemSettings 实体

```java
@Entity
@Table(name = "system_settings")
public class SystemSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 酒店基本信息
    @Column(nullable = false, length = 100)
    private String hotelName = "GrandHorizon 酒店及水疗中心";

    @Column(nullable = false, length = 100)
    private String contactEmail = "contact@grandhorizon.com";

    @Column(nullable = false, length = 20)
    private String contactPhone = "+1 (555) 123-4567";

    @Column(length = 200)
    private String address = "123 Ocean Drive, Miami, FL 33139";

    @Column(nullable = false, length = 10)
    private String currency = "CNY";      // CNY, USD, EUR, GBP

    @Column(nullable = false, length = 10)
    private String timezone = "UTC+8";    // UTC+8, UTC+0, UTC-5

    @Column(nullable = false, length = 20)
    private String language = "Chinese";  // Chinese, English, Spanish

    // 安全设置
    @Column(nullable = false)
    private Boolean twoFactorEnabled = false;

    @Column(nullable = false)
    private Integer sessionTimeout = 30;  // 分钟，范围 5-120

    @Column(nullable = false)
    private Integer passwordExpiry = 90;  // 天，范围 30-365

    // 通知设置
    @Column(nullable = false)
    private Boolean emailNotificationBookings = true;

    @Column(nullable = false)
    private Boolean emailNotificationCancellations = true;

    @Column(nullable = false)
    private Boolean pushNotificationsEnabled = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(nullable = false, length = 50)
    private String updatedBy = "system";
}
```

**约束**：
- 系统只维护一条 SystemSettings 记录（singleton）
- sessionTimeout 范围：5-120 分钟
- passwordExpiry 范围：30-365 天
- currency 值：CNY, USD, EUR, GBP
- timezone 值：UTC+8, UTC+0, UTC-5
- language 值：Chinese, English, Spanish

### 2.2 Notification 实体

```java
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false, length = 50)
    private String type;  // BOOKING, CANCELLATION, CHECK_IN, CHECK_OUT, SYSTEM

    @Column(nullable = false)
    private Boolean isRead = false;

    @Column(nullable = false)
    private Integer priority = 0;  // 0=普通, 1=重要, 2=紧急

    @Column(length = 200)
    private String actionLink;  // 点击后跳转的链接

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime readAt;

    // 自动清理：删除 30 天前已读的通知
}
```

**索引**：
- `idx_user_unread`：(userId, isRead) - 查询未读通知
- `idx_user_created`：(userId, createdAt) - 按时间排序

### 2.3 ChangePasswordRequest DTO

```java
public class ChangePasswordRequest {
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String newPassword;
}
```

## 3. 后端 API 设计

### 3.1 SettingsController (`/api/settings`)

**只有 ADMIN 角色可以访问**

| 方法 | 路径 | 描述 | 权限 |
|------|------|------|------|
| GET | `/api/settings` | 获取系统设置 | ADMIN |
| PUT | `/api/settings` | 更新系统设置 | ADMIN |

**响应 DTO (SettingsResponse)**:
```java
public class SettingsResponse {
    private Long id;
    private String hotelName;
    private String contactEmail;
    private String contactPhone;
    private String address;
    private String currency;
    private String timezone;
    private String language;
    private Boolean twoFactorEnabled;
    private Integer sessionTimeout;
    private Integer passwordExpiry;
    private Boolean emailNotificationBookings;
    private Boolean emailNotificationCancellations;
    private Boolean pushNotificationsEnabled;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
```

**请求 DTO (SettingsRequest)**:
```java
public class SettingsRequest {
    @NotBlank(message = "酒店名称不能为空")
    @Size(max = 100, message = "酒店名称不能超过100个字符")
    private String hotelName;

    @NotBlank(message = "联系邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱不能超过100个字符")
    private String contactEmail;

    @NotBlank(message = "电话号码不能为空")
    @Size(max = 20, message = "电话号码不能超过20个字符")
    private String contactPhone;

    @Size(max = 200, message = "地址不能超过200个字符")
    private String address;

    @NotBlank(message = "货币不能为空")
    @Pattern(regexp = "CNY|USD|EUR|GBP", message = "货币必须是 CNY, USD, EUR 或 GBP")
    private String currency;

    @NotBlank(message = "时区不能为空")
    @Pattern(regexp = "UTC\\+8|UTC\\+0|UTC-5", message = "时区必须是 UTC+8, UTC+0 或 UTC-5")
    private String timezone;

    @NotBlank(message = "语言不能为空")
    @Pattern(regexp = "Chinese|English|Spanish", message = "语言必须是 Chinese, English 或 Spanish")
    private String language;

    private Boolean twoFactorEnabled;

    @Min(value = 5, message = "会话超时不能少于5分钟")
    @Max(value = 120, message = "会话超时不能超过120分钟")
    private Integer sessionTimeout;

    @Min(value = 30, message = "密码有效期不能少于30天")
    @Max(value = 365, message = "密码有效期不能超过365天")
    private Integer passwordExpiry;

    private Boolean emailNotificationBookings;
    private Boolean emailNotificationCancellations;
    private Boolean pushNotificationsEnabled;
}
```

### 3.2 NotificationController (`/api/notifications`)

| 方法 | 路径 | 描述 | 权限 |
|------|------|------|------|
| GET | `/api/notifications` | 获取当前用户的通知列表（分页） | AUTHENTICATED |
| GET | `/api/notifications/unread-count` | 获取未读通知数量 | AUTHENTICATED |
| PUT | `/api/notifications/{id}/read` | 标记通知为已读 | AUTHENTICATED |
| PUT | `/api/notifications/read-all` | 标记所有通知为已读 | AUTHENTICATED |

**响应 DTO (NotificationResponse)**:
```java
public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private String type;
    private Boolean isRead;
    private Integer priority;
    private String actionLink;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
```

**分页响应**:
```java
public class NotificationListResponse {
    private List<NotificationResponse> notifications;
    private Long total;
    private Integer unreadCount;
}
```

### 3.3 ProfileController 扩展

| 方法 | 路径 | 描述 | 权限 |
|------|------|------|------|
| PUT | `/api/profile/password` | 修改当前用户密码 | AUTHENTICATED |

**错误响应**:
- `400 BAD_REQUEST` - 旧密码不正确或验证失败
- `401 UNAUTHORIZED` - 未登录
- `403 FORBIDDEN` - 无权限

## 4. 前端组件

### 4.1 Settings.vue 更新

- 连接后端 API 加载/保存设置
- 添加表单验证（使用 Element Plus 表单验证）
- 添加加载状态和错误处理
- "更改管理员密码" 按钮打开对话框（实现密码修改功能）

### 4.2 Layout.vue 铃铛按钮更新

- 显示未读通知数量徽章（红色圆形，最大显示 99+）
- 点击展示通知下拉列表（最多显示 5 条，超出显示"查看全部"）
- 点击通知项可跳转到相关页面
- 点击"标记全部已读"按钮

### 4.3 新建文件

| 文件路径 | 描述 |
|----------|------|
| `frontend/src/api/settings.ts` | 设置 API |
| `frontend/src/api/notification.ts` | 通知 API |
| `frontend/src/types/settings.ts` | 设置类型定义 |
| `frontend/src/types/notification.ts` | 通知类型定义 |
| `frontend/src/components/NotificationDropdown.vue` | 通知下拉组件 |
| `frontend/src/components/ChangePasswordDialog.vue` | 修改密码对话框 |

### 4.4 轮询策略

- 使用轮询方式获取未读通知数量
- 轮询间隔：30 秒
- 用户活动时（点击、输入）暂停轮询
- 页面隐藏时暂停轮询

## 5. 数据流程

### 5.1 设置更新流程
```
用户修改设置 → 前端验证 → PUT /api/settings →
后端验证权限 → 更新数据库 → 记录审计日志 → 返回成功
```

### 5.2 通知创建流程
```
业务事件发生（预订创建/取消/入住/退房）→
创建通知记录 → 存入数据库 →
前端轮询获取 → 更新铃铛徽章
```

### 5.3 通知读取流程
```
用户点击通知 → 标记为已读 → PUT /api/notifications/{id}/read →
更新数据库 → 前端更新已读状态
```

## 6. 初始化

**默认系统设置值**：
```java
- hotelName: "GrandHorizon 酒店及水疗中心"
- contactEmail: "contact@grandhorizon.com"
- contactPhone: "+1 (555) 123-4567"
- address: "123 Ocean Drive, Miami, FL 33139"
- currency: "CNY"
- timezone: "UTC+8"
- language: "Chinese"
- twoFactorEnabled: false
- sessionTimeout: 30
- passwordExpiry: 90
- emailNotificationBookings: true
- emailNotificationCancellations: true
- pushNotificationsEnabled: true
```

**SystemSettings 为单例**：确保数据库中只有一条记录。

## 7. 通知触发点

以下事件会创建通知：

| 事件 | 通知类型 | 标题 | 消息模板 | 优先级 |
|------|----------|------|----------|--------|
| 新预订创建 | BOOKING | 新预订通知 | "客人 {guestName} 预订了 {roomNumber} 房间" | 0 |
| 预订取消 | CANCELLATION | 预订取消 | "预订 #{bookingId} 已取消" | 1 |
| 客人入住 | CHECK_IN | 入住通知 | "{guestName} 已入住 {roomNumber} 房间" | 0 |
| 客人退房 | CHECK_OUT | 退房通知 | "{roomNumber} 房间已退房，需要清洁" | 1 |
| 系统设置更新 | SYSTEM | 设置更新 | "系统设置已更新" | 2 |

## 8. 安全考虑

- **权限控制**：只有 ADMIN 角色可以修改系统设置
- **密码修改**：需要验证旧密码，新密码必须符合复杂度要求
- **通知隔离**：用户只能看到自己的通知
- **审计日志**：记录谁在什么时候修改了什么设置
- **输入验证**：所有用户输入都需要后端验证
- **速率限制**：通知轮询限制为最少 10 秒间隔

## 9. 技术栈

- **后端**: Spring Boot, JPA, SQLite
- **前端**: Vue 3, TypeScript, Element Plus
- **通信**: REST API + 前端轮询
