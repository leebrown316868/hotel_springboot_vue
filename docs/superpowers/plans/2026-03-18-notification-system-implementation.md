# 通知系统实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**目标：** 在酒店预订系统的关键业务事件中实现通知功能，提升用户体验和运营效率。

**架构：** 在 BookingServiceImpl 中注入 NotificationService，通过 NotificationHelper 工具类封装通知创建逻辑，在预订创建、取消、入住、退房时向相关用户发送通知。

**技术栈：** Spring Boot, JPA, Lombok, Java 17

---

## 文件结构

### 新增文件
- `src/main/java/com/hotel/util/NotificationHelper.java` - 通知工具类，封装通知创建逻辑和内容模板

### 修改文件
- `src/main/java/com/hotel/repository/UserRepository.java` - 添加按角色查询方法
- `src/main/java/com/hotel/repository/GuestRepository.java` - 添加按角色查询方法
- `src/main/java/com/hotel/service/impl/BookingServiceImpl.java` - 添加通知调用

---

## Task 1: 扩展 UserRepository 添加按角色查询

**Files:**
- Modify: `src/main/java/com/hotel/repository/UserRepository.java`

- [ ] **Step 1: 添加按角色查询方法**

```java
// 在 UserRepository 接口中添加以下方法
List<User> findByRole(UserRole role);
```

- [ ] **Step 2: 验证编译**

Run: `cd E:\project\hotel && mvn compile -q`
Expected: 编译成功，无错误

- [ ] **Step 3: 提交更改**

```bash
cd E:\project\hotel
git add src/main/java/com/hotel/repository/UserRepository.java
git commit -m "feat(repository): 添加 User 按角色查询方法

- 添加 findByRole 方法用于获取指定角色的用户列表
- 用于通知系统获取需要通知的员工用户

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 2: 扩展 GuestRepository 添加按角色查询

**Files:**
- Modify: `src/main/java/com/hotel/repository/GuestRepository.java`

- [ ] **Step 1: 添加按角色查询方法**

```java
// 在 GuestRepository 接口中添加以下方法
import com.hotel.entity.UserRole;

List<Guest> findByRole(UserRole role);
```

- [ ] **Step 2: 验证编译**

Run: `cd E:\project\hotel && mvn compile -q`
Expected: 编译成功，无错误

- [ ] **Step 3: 提交更改**

```bash
cd E:\project\hotel
git add src/main/java/com/hotel/repository/GuestRepository.java
git commit -m "feat(repository): 添加 Guest 按角色查询方法

- 添加 findByRole 方法用于获取指定角色的客户列表
- 用于通知系统获取客户信息

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 3: 创建 NotificationHelper 工具类

**Files:**
- Create: `src/main/java/com/hotel/util/NotificationHelper.java`

- [ ] **Step 1: 创建 NotificationHelper 类**

```java
package com.hotel.util;

import com.hotel.entity.Booking;
import com.hotel.entity.Guest;
import com.hotel.entity.NotificationType;
import com.hotel.entity.User;
import com.hotel.entity.UserRole;
import com.hotel.repository.GuestRepository;
import com.hotel.repository.UserRepository;
import com.hotel.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

/**
 * 通知工具类，封装通知创建逻辑和内容模板
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationHelper {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final GuestRepository guestRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日");

    /**
     * 发送预订创建通知
     */
    public void notifyBookingCreated(Booking booking) {
        try {
            String guestEmail = booking.getGuest().getEmail();
            String title = "预订成功";
            String message = String.format(
                "您的预订 %s 已创建，%s，%s入住，%s退房",
                booking.getBookingNumber(),
                booking.getRoom().getRoomType().getName(),
                booking.getCheckInDate().format(DATE_FORMATTER),
                booking.getCheckOutDate().format(DATE_FORMATTER)
            );

            notificationService.createNotification(
                guestEmail,
                title,
                message,
                NotificationType.BOOKING,
                1,
                "/my-bookings"
            );

            log.info("Sent booking created notification to: {}", guestEmail);
        } catch (Exception e) {
            log.error("Failed to send booking created notification for: {}", booking.getBookingNumber(), e);
        }
    }

    /**
     * 发送预订取消通知
     */
    public void notifyBookingCancelled(Booking booking) {
        try {
            String guestEmail = booking.getGuest().getEmail();
            String title = "预订已取消";
            String message = String.format("您的预订 %s 已取消", booking.getBookingNumber());

            notificationService.createNotification(
                guestEmail,
                title,
                message,
                NotificationType.CANCELLATION,
                2,
                "/my-bookings"
            );

            log.info("Sent booking cancelled notification to: {}", guestEmail);
        } catch (Exception e) {
            log.error("Failed to send booking cancelled notification for: {}", booking.getBookingNumber(), e);
        }
    }

    /**
     * 发送入住通知（给所有员工）
     */
    public void notifyCheckIn(Booking booking) {
        String title = "客户入住";
        String message = String.format(
            "客户 %s 已入住 %s，预订号 %s",
            booking.getGuest().getName(),
            booking.getRoom().getRoomNumber(),
            booking.getBookingNumber()
        );

        List<String> staffEmails = getStaffEmails();
        for (String email : staffEmails) {
            try {
                notificationService.createNotification(
                    email,
                    title,
                    message,
                    NotificationType.CHECK_IN,
                    1,
                    "/staff-bookings"
                );
            } catch (Exception e) {
                log.error("Failed to send check-in notification to: {}", email, e);
            }
        }

        log.info("Sent check-in notifications to {} staff members", staffEmails.size());
    }

    /**
     * 发送退房通知（给所有员工）
     */
    public void notifyCheckOut(Booking booking) {
        String title = "客户退房";
        String message = String.format(
            "客户 %s 已退房 %s，预订号 %s",
            booking.getGuest().getName(),
            booking.getRoom().getRoomNumber(),
            booking.getBookingNumber()
        );

        List<String> staffEmails = getStaffEmails();
        for (String email : staffEmails) {
            try {
                notificationService.createNotification(
                    email,
                    title,
                    message,
                    NotificationType.CHECK_OUT,
                    1,
                    "/staff-bookings"
                );
            } catch (Exception e) {
                log.error("Failed to send check-out notification to: {}", email, e);
            }
        }

        log.info("Sent check-out notifications to {} staff members", staffEmails.size());
    }

    /**
     * 获取所有员工（STAFF 和 ADMIN）的邮箱列表
     */
    private List<String> getStaffEmails() {
        List<User> staff = userRepository.findByRole(UserRole.STAFF);
        List<User> admin = userRepository.findByRole(UserRole.ADMIN);
        return Stream.concat(staff.stream(), admin.stream())
                .map(User::getEmail)
                .toList();
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `cd E:\project\hotel && mvn compile -q`
Expected: 编译成功，无错误

- [ ] **Step 3: 提交更改**

```bash
cd E:\project\hotel
git add src/main/java/com/hotel/util/NotificationHelper.java
git commit -m "feat(util): 创建 NotificationHelper 工具类

- 封装通知创建逻辑和内容模板
- 实现预订创建、取消、入住、退房通知方法
- 支持向客户和员工发送不同类型的通知
- 通知失败不影响业务操作，仅记录日志

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 4: 在 BookingServiceImpl 中注入 NotificationHelper

**Files:**
- Modify: `src/main/java/com/hotel/service/impl/BookingServiceImpl.java:47-53`

- [ ] **Step 1: 添加依赖注入**

在 BookingServiceImpl 类的依赖注入部分添加：

```java
private final NotificationHelper notificationHelper;
```

确保构造函数参数包含 notificationHelper。

- [ ] **Step 2: 验证编译**

Run: `cd E:\project\hotel && mvn compile -q`
Expected: 编译成功，无错误

- [ ] **Step 3: 提交更改**

```bash
cd E:\project\hotel
git add src/main/java/com/hotel/service/impl/BookingServiceImpl.java
git commit -m "refactor(service): BookingServiceImpl 注入 NotificationHelper

- 添加 NotificationHelper 依赖注入
- 为后续添加通知调用做准备

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 5: 在预订创建方法中添加通知

**Files:**
- Modify: `src/main/java/com/hotel/service/impl/BookingServiceImpl.java:148-151`

- [ ] **Step 1: 在 create 方法末尾添加通知调用**

在 `return bookingMapper.toResponse(booking);` 之前添加：

```java
// 发送预订成功通知
notificationHelper.notifyBookingCreated(booking);
```

完整代码应该是：
```java
booking = bookingRepository.save(booking);
log.info("Created booking with number: {}", bookingNumber);

// 发送预订成功通知
notificationHelper.notifyBookingCreated(booking);

return bookingMapper.toResponse(booking);
```

- [ ] **Step 2: 验证编译**

Run: `cd E:\project\hotel && mvn compile -q`
Expected: 编译成功，无错误

- [ ] **Step 3: 提交更改**

```bash
cd E:\project\hotel
git add src/main/java/com/hotel/service/impl/BookingServiceImpl.java
git commit -m "feat(service): 预订创建时发送通知

- 在 create 方法中添加预订成功通知
- 客户会在预订创建后收到通知

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 6: 在预订取消方法中添加通知

**Files:**
- Modify: `src/main/java/com/hotel/service/impl/BookingServiceImpl.java`

- [ ] **Step 1: 找到 cancelBooking 方法**

找到 `cancelBooking` 方法中设置状态为 CANCELLED 后的位置。

- [ ] **Step 2: 在状态更新后添加通知调用**

在 `booking.setStatus(BookingStatus.CANCELLED);` 和 `bookingRepository.save(booking);` 之后添加：

```java
// 发送取消通知
notificationHelper.notifyBookingCancelled(booking);
```

- [ ] **Step 3: 验证编译**

Run: `cd E:\project\hotel && mvn compile -q`
Expected: 编译成功，无错误

- [ ] **Step 4: 提交更改**

```bash
cd E:\project\hotel
git add src/main/java/com/hotel/service/impl/BookingServiceImpl.java
git commit -m "feat(service): 预订取消时发送通知

- 在 cancelBooking 方法中添加取消通知
- 客户会在预订取消后收到通知

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 7: 在入住方法中添加通知

**Files:**
- Modify: `src/main/java/com/hotel/service/impl/BookingServiceImpl.java`

- [ ] **Step 1: 找到 checkIn 方法**

找到 `checkIn` 方法中更新房间状态后的位置。

- [ ] **Step 2: 在方法末尾添加通知调用**

在方法末尾（状态更新和保存之后）添加：

```java
// 发送入住通知给员工
notificationHelper.notifyCheckIn(booking);
```

- [ ] **Step 3: 验证编译**

Run: `cd E:\project\hotel && mvn compile -q`
Expected: 编译成功，无错误

- [ ] **Step 4: 提交更改**

```bash
cd E:\project\hotel
git add src/main/java/com/hotel/service/impl/BookingServiceImpl.java
git commit -m "feat(service): 客户入住时发送通知

- 在 checkIn 方法中添加入住通知
- 所有 STAFF 和 ADMIN 用户会收到入住通知

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 8: 在退房方法中添加通知

**Files:**
- Modify: `src/main/java/com/hotel/service/impl/BookingServiceImpl.java`

- [ ] **Step 1: 找到 checkOut 方法**

找到 `checkOut` 方法中更新房间状态后的位置。

- [ ] **Step 2: 在方法末尾添加通知调用**

在方法末尾（状态更新和保存之后）添加：

```java
// 发送退房通知给员工
notificationHelper.notifyCheckOut(booking);
```

- [ ] **Step 3: 验证编译**

Run: `cd E:\project\hotel && mvn compile -q`
Expected: 编译成功，无错误

- [ ] **Step 4: 提交更改**

```bash
cd E:\project\hotel
git add src/main/java/com/hotel/service/impl/BookingServiceImpl.java
git commit -m "feat(service): 客户退房时发送通知

- 在 checkOut 方法中添加退房通知
- 所有 STAFF 和 ADMIN 用户会收到退房通知

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 9: 编译和重启后端验证

**Files:**
- None (验证任务)

- [ ] **Step 1: 完整编译**

Run: `cd E:\project\hotel && mvn clean compile -q`
Expected: 编译成功，无错误

- [ ] **Step 2: 启动后端**

Run:
```bash
cd E:\project\hotel
taskkill //F //IM java.exe 2>nul
mvn spring-boot:run
```

Expected: 后端成功启动，无错误日志

- [ ] **Step 3: 验证 API 端点**

Run: `curl -s http://localhost:8080/api/settings/public | head -c 50`
Expected: 返回 JSON 格式的设置数据

---

## Task 10: 功能测试

**Files:**
- None (测试任务)

- [ ] **Step 1: 测试预订创建通知**

1. 使用客户账号登录前端
2. 创建新预订
3. 检查通知图标是否显示未读数量
4. 点击通知查看是否收到"预订成功"通知

- [ ] **Step 2: 测试预订取消通知**

1. 取消一个预订
2. 检查是否收到"预订已取消"通知

- [ ] **Step 3: 测试入住通知**

1. 使用 STAFF 或 ADMIN 账号登录
2. 执行入住操作
3. 检查是否收到"客户入住"通知

- [ ] **Step 4: 测试退房通知**

1. 执行退房操作
2. 检查是否收到"客户退房"通知

- [ ] **Step 5: 提交测试完成标记**

```bash
cd E:\project\hotel
git commit --allow-empty -m "test: 通知系统功能测试完成

- 验证预订创建通知正常工作
- 验证预订取消通知正常工作
- 验证入住通知正常工作
- 验证退房通知正常工作

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## 完成检查清单

- [ ] 所有任务已完成
- [ ] 所有代码已提交
- [ ] 功能测试通过
- [ ] 无编译错误或警告
- [ ] 后端正常运行

---

## 注意事项

1. **通知失败不影响业务**：所有通知调用都包裹在 try-catch 中，确保通知失败不会导致业务操作失败

2. **日志记录**：所有通知失败都会记录错误日志，便于排查问题

3. **邮件格式**：通知的 userId 字段存储用户的 email 地址，这是 Guest 和 User 实体的通用标识符

4. **测试顺序**：建议按任务顺序执行，确保每个任务都完成后再进行下一个

5. **回滚策略**：如果某个任务出现问题，可以使用 `git reset --hard HEAD~1` 回滚到上一个提交
