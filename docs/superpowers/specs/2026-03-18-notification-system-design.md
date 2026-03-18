# 通知系统设计文档

## 概述

在酒店预订系统中实现通知功能，在关键业务事件发生时向相关用户发送通知，提升用户体验和运营效率。

## 用户实体架构说明

**重要：** 本系统使用双实体架构存储用户信息：

1. **User 实体** - 内部用户（管理员、员工）
   - 存储位置：`users` 表
   - 角色：ADMIN、STAFF
   - 仓库：`UserRepository`

2. **Guest 实体** - 外部用户（客户）
   - 存储位置：`guests` 表
   - 角色：CUSTOMER
   - 仓库：`GuestRepository`

**通知接收者识别：**
- 两个实体都有 `email` 字段，使用 email 作为通用标识符
- 通知系统的 `userId` 字段存储用户的 email 地址

## 设计目标

1. 在预订业务流程的关键节点向相关用户发送通知
2. 通知失败不影响核心业务操作
3. 按需分角色发送通知，避免通知轰炸
4. 自动清理过期通知，控制数据库大小

## 架构设计

### 组件关系

```
BookingServiceImpl
    ↓ 注入
NotificationService
    ↓ 调用
NotificationRepository
```

### 新增组件

**NotificationHelper（通知工具类）**
- 位置：`com.hotel.util.NotificationHelper`
- 职责：提供便捷的通知创建方法，封装通知内容模板
- 依赖：
  - NotificationService
  - GuestRepository（用于获取客户信息）
  - UserRepository（用于获取员工信息）

## 通知触发点

| 业务事件 | 触发方法 | 接收者 | 通知类型 | 优先级 |
|----------|----------|--------|----------|--------|
| 创建预订 | `create()` | 客户 | BOOKING | 1 |
| 取消预订 | `cancelBooking()` | 客户 | CANCELLATION | 2 |
| 入住 | `checkIn()` | STAFF, ADMIN | CHECK_IN | 1 |
| 退房 | `checkOut()` | STAFF, ADMIN | CHECK_OUT | 1 |

## 通知内容模板

### 创建预订通知
- **标题：** "预订成功"
- **内容：** "您的预订 {预订号} 已创建，{房型名称}，{入住日期}入住，{退房日期}退房"
- **链接：** "/my-bookings"
- **接收者：** 预订客户

### 取消预订通知
- **标题：** "预订已取消"
- **内容：** "您的预订 {预订号} 已取消"
- **链接：** "/my-bookings"
- **接收者：** 预订客户

### 入住通知
- **标题：** "客户入住"
- **内容：** "客户 {客户姓名} 已入住 {房间号}，预订号 {预订号}"
- **链接：** "/staff-bookings"
- **接收者：** 所有 STAFF 和 ADMIN 用户

### 退房通知
- **标题：** "客户退房"
- **内容：** "客户 {客户姓名} 已退房 {房间号}，预订号 {预订号}"
- **链接：** "/staff-bookings"
- **接收者：** 所有 STAFF 和 ADMIN 用户

## 实现细节

### 1. 修改 BookingServiceImpl

**注入依赖：**
```java
private final NotificationService notificationService;
```

**业务方法修改：**
- 在 `create()` 方法末尾添加预订成功通知
- 在 `cancelBooking()` 方法中添加取消通知
- 在 `checkIn()` 方法中添加入住通知
- 在 `checkOut()` 方法中添加退房通知

**错误处理：**
```java
try {
    notificationService.createNotification(...);
} catch (Exception e) {
    log.error("Failed to send notification for booking: {}", booking.getBookingNumber(), e);
    // 不影响业务操作
}
```

**监控建议：**
- 记录通知失败日志，包含预订号、接收者、失败原因
- 可选：添加通知失败统计指标
- 可选：对于关键通知（如支付失败），考虑添加重试机制

### 2. 创建 NotificationHelper

**方法列表：**
- `notifyBookingCreated(Booking booking)` - 创建预订通知
- `notifyBookingCancelled(Booking booking)` - 取消预订通知
- `notifyCheckIn(Booking booking)` - 入住通知
- `notifyCheckOut(Booking booking)` - 退房通知

**日期格式化：**
- 使用 `DateTimeFormatter.ofPattern("yyyy年MM月dd日")` 格式化日期
- 示例："2024年03月20日"

**模板变量替换：**
- `{预订号}` → `booking.getBookingNumber()`
- `{房型名称}` → `booking.getRoom().getRoomType().getName()`
- `{入住日期}` → `booking.getCheckInDate().format(formatter)`
- `{退房日期}` → `booking.getCheckOutDate().format(formatter)`
- `{客户姓名}` → `booking.getGuest().getName()`
- `{房间号}` → `booking.getRoom().getRoomNumber()`

### 3. 角色用户获取

**新增方法：**
```java
// UserRepository 中添加
List<User> findByRole(UserRole role);

// GuestRepository 中添加
List<Guest> findByRole(UserRole role);
```

**使用方式：**
```java
// 获取需要通知的员工用户（从 users 表）
List<User> staffUsers = userRepository.findByRole(UserRole.STAFF);
List<User> adminUsers = userRepository.findByRole(UserRole.ADMIN);

// 向所有员工发送通知
for (User user : staffUsers) {
    notificationService.createNotification(user.getEmail(), ...);
}

// 获取客户信息（从 guests 表）
Guest guest = guestRepository.findByEmail(guestEmail).orElse(null);
```

**通知接收者获取工具方法：**
```java
// NotificationHelper 中添加
private List<String> getStaffEmails() {
    List<User> staff = userRepository.findByRole(UserRole.STAFF);
    List<User> admin = userRepository.findByRole(UserRole.ADMIN);
    return Stream.concat(staff.stream(), admin.stream())
            .map(User::getEmail)
            .collect(Collectors.toList());
}
```

## 数据持久化

### 清理策略

- 已读通知：30天后自动删除
- 未读通知：永久保留（直到用户标记为已读）

### 实现方式

利用现有的 `NotificationService.cleanupOldNotifications()` 方法，可以通过定时任务或手动调用执行。

## 前端集成

前端已完成以下组件：
- `NotificationDropdown.vue` - 通知下拉组件
- `Layout.vue` - 已集成通知组件

通知会自动显示在页面顶部导航栏中。

### 实时通知策略

**当前实现：** 轮询方式
- 前端定期调用 `/api/notifications/unread-count` 获取未读数量
- 用户点击通知图标时加载完整通知列表

**可选改进：**
- 使用 WebSocket 实现实时推送
- 使用 Server-Sent Events (SSE) 实现服务器推送

### 用户体验

- 通知图标显示未读数量徽章
- 点击通知项可跳转到相关页面
- 支持标记为已读和全部标记为已读
- 移动端响应式设计

## 测试计划

### 单元测试
- NotificationHelper 各方法测试
- 通知创建失败不影响业务的测试

### 集成测试
- 创建预订后检查通知是否创建
- 取消预订后检查通知是否创建
- 入住/退房后检查员工是否收到通知

### 人工测试
- 使用客户账号创建预订，检查是否收到通知
- 使用员工账号执行入住/退房，检查是否收到通知

## 文件修改清单

### 需要修改的文件
1. `src/main/java/com/hotel/service/impl/BookingServiceImpl.java` - 添加通知调用
2. `src/main/java/com/hotel/repository/UserRepository.java` - 添加按角色查询方法
3. `src/main/java/com/hotel/repository/GuestRepository.java` - 添加按角色查询方法

### 需要新增的文件
1. `src/main/java/com/hotel/util/NotificationHelper.java` - 通知工具类

## 性能考虑

### 批量通知创建

当需要向多个员工发送通知时（如入住/退房通知），使用循环创建通知：

```java
List<String> staffEmails = getStaffEmails();
for (String email : staffEmails) {
    try {
        notificationService.createNotification(email, title, message, type, priority, link);
    } catch (Exception e) {
        log.error("Failed to send notification to: {}", email, e);
    }
}
```

**性能说明：**
- 当前实现采用同步方式，适用于中小型酒店（员工数量 < 50）
- 如果员工数量较大，可以考虑：
  - 使用 `@Async` 注解实现异步通知
  - 使用消息队列（如 RabbitMQ）进行解耦
  - 批量插入优化数据库操作

### N+1 查询优化

当前设计在获取员工列表时会产生额外的查询，这是可接受的，因为：
- 员工数量相对较少
- 通知创建频率不高（仅在入住/退房时）
- 可以通过缓存优化（可选）

## 未来改进方向

### 架构改进
- 考虑使用 Spring Events (`@EventListener`) 解耦服务依赖
- 添加通知模板引擎，支持多语言
- 实现通知优先级队列

### 功能扩展
- 邮件通知集成
- 短信通知集成
- 通知偏好设置（用户可选择接收哪些类型的通知）
- 通知统计和分析

### 技术优化
- 异步通知处理（`@Async` 或消息队列）
- 批量通知创建优化
- 通知发送失败重试机制
