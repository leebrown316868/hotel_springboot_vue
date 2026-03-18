# 通知系统设计文档

## 概述

在酒店预订系统中实现通知功能，在关键业务事件发生时向相关用户发送通知，提升用户体验和运营效率。

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
- 依赖：NotificationService

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
    log.error("Failed to send notification", e);
    // 不影响业务操作
}
```

### 2. 创建 NotificationHelper

**方法列表：**
- `notifyBookingCreated(Booking booking)` - 创建预订通知
- `notifyBookingCancelled(Booking booking)` - 取消预订通知
- `notifyCheckIn(Booking booking)` - 入住通知
- `notifyCheckOut(Booking booking)` - 退房通知

### 3. 角色用户获取

**新增方法：**
```java
// UserRepository 中添加
List<User> findByRole(UserRole role);
```

**使用方式：**
```java
// 获取需要通知的员工用户
List<User> staffUsers = userRepository.findByRole(UserRole.STAFF);
List<User> adminUsers = userRepository.findByRole(UserRole.ADMIN);

// 向所有员工发送通知
for (User user : staffUsers) {
    notificationService.createNotification(user.getEmail(), ...);
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

### 需要新增的文件
1. `src/main/java/com/hotel/util/NotificationHelper.java` - 通知工具类

## 非功能性考虑

1. **性能：** 通知创建采用同步方式，但使用 try-catch 确保不影响业务性能
2. **可扩展性：** NotificationHelper 封装通知逻辑，便于后续添加新的通知类型
3. **可维护性：** 通知内容模板集中管理，便于修改
4. **可靠性：** 通知失败不影响业务，确保系统稳定性
