# 系统设置功能实现计划

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**目标:** 实现全局系统设置、通知系统和密码管理功能

**架构:** 后端使用 Spring Boot JPA 实现数据层和 REST API，前端使用 Vue 3 + TypeScript + Element Plus 实现用户界面，通过 REST API 通信。

**技术栈:** Spring Boot, JPA, SQLite, Vue 3, TypeScript, Element Plus, Axios

---

## 文件结构概览

### 后端文件 (新建)
| 文件路径 | 职责 |
|----------|------|
| `src/main/java/com/hotel/entity/SystemSettings.java` | 系统设置实体 |
| `src/main/java/com/hotel/entity/Notification.java` | 通知实体 |
| `src/main/java/com/hotel/entity/NotificationType.java` | 通知类型枚举 |
| `src/main/java/com/hotel/repository/SystemSettingsRepository.java` | 系统设置数据访问 |
| `src/main/java/com/hotel/repository/NotificationRepository.java` | 通知数据访问 |
| `src/main/java/com/hotel/dto/SettingsRequest.java` | 设置请求 DTO |
| `src/main/java/com/hotel/dto/SettingsResponse.java` | 设置响应 DTO |
| `src/main/java/com/hotel/dto/NotificationResponse.java` | 通知响应 DTO |
| `src/main/java/com/hotel/dto/NotificationListResponse.java` | 通知列表响应 DTO |
| `src/main/java/com/hotel/dto/ChangePasswordRequest.java` | 修改密码请求 DTO |
| `src/main/java/com/hotel/service/SettingsService.java` | 设置服务接口 |
| `src/main/java/com/hotel/service/NotificationService.java` | 通知服务接口 |
| `src/main/java/com/hotel/service/impl/SettingsServiceImpl.java` | 设置服务实现 |
| `src/main/java/com/hotel/service/impl/NotificationServiceImpl.java` | 通知服务实现 |
| `src/main/java/com/hotel/mapper/SettingsMapper.java` | 设置映射器 |
| `src/main/java/com/hotel/mapper/NotificationMapper.java` | 通知映射器 |
| `src/main/java/com/hotel/controller/SettingsController.java` | 设置控制器 |
| `src/main/java/com/hotel/controller/NotificationController.java` | 通知控制器 |
| `src/main/java/com/hotel/exception/SettingsNotFoundException.java` | 设置未找到异常 |

### 后端文件 (修改)
| 文件路径 | 修改内容 |
|----------|----------|
| `src/main/java/com/hotel/controller/ProfileController.java` | 添加修改密码端点 |
| `src/main/java/com/hotel/service/ProfileService.java` | 添加修改密码方法 |
| `src/main/java/com/hotel/service/impl/ProfileServiceImpl.java` | 实现修改密码方法 |
| `src/main/java/com/hotel/init/DataInitializer.java` | 初始化默认系统设置 |

### 前端文件 (新建)
| 文件路径 | 职责 |
|----------|------|
| `frontend/src/types/settings.ts` | 设置类型定义 |
| `frontend/src/types/notification.ts` | 通知类型定义 |
| `frontend/src/api/settings.ts` | 设置 API |
| `frontend/src/api/notification.ts` | 通知 API |
| `frontend/src/components/NotificationDropdown.vue` | 通知下拉组件 |
| `frontend/src/components/ChangePasswordDialog.vue` | 修改密码对话框 |
| `frontend/src/composables/useNotifications.ts` | 通知轮询 composable |

### 前端文件 (修改)
| 文件路径 | 修改内容 |
|----------|----------|
| `frontend/src/views/Settings.vue` | 连接后端 API，添加功能 |
| `frontend/src/components/Layout.vue` | 添加通知功能到铃铛按钮 |

---

## Chunk 1: 后端实体和数据访问层

此块创建 SystemSettings 和 Notification 实体，以及对应的 Repository。

### Task 1: 创建 NotificationType 枚举

**Files:**
- Create: `src/main/java/com/hotel/entity/NotificationType.java`

- [ ] **Step 1: 创建 NotificationType 枚举**

```java
package com.hotel.entity;

public enum NotificationType {
    BOOKING,
    CANCELLATION,
    CHECK_IN,
    CHECK_OUT,
    SYSTEM
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/hotel/entity/NotificationType.java
git commit -m "feat(entity): 添加 NotificationType 枚举"
```

### Task 2: 创建 SystemSettings 实体

**Files:**
- Create: `src/main/java/com/hotel/entity/SystemSettings.java`

- [ ] **Step 1: 创建 SystemSettings 实体**

```java
package com.hotel.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
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
    private String currency = "CNY";

    @Column(nullable = false, length = 10)
    private String timezone = "UTC+8";

    @Column(nullable = false, length = 20)
    private String language = "Chinese";

    // 安全设置
    @Column(nullable = false)
    private Boolean twoFactorEnabled = false;

    @Column(nullable = false)
    private Integer sessionTimeout = 30;

    @Column(nullable = false)
    private Integer passwordExpiry = 90;

    // 通知设置
    @Column(nullable = false)
    private Boolean emailNotificationBookings = true;

    @Column(nullable = false)
    private Boolean emailNotificationCancellations = true;

    @Column(nullable = false)
    private Boolean pushNotificationsEnabled = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false, length = 50)
    private String updatedBy = "system";

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/hotel/entity/SystemSettings.java
git commit -m "feat(entity): 添加 SystemSettings 实体"
```

### Task 3: 创建 Notification 实体

**Files:**
- Create: `src/main/java/com/hotel/entity/Notification.java`

- [ ] **Step 1: 创建 Notification 实体**

```java
package com.hotel.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_user_unread", columnList = "userId, isRead"),
    @Index(name = "idx_user_created", columnList = "userId, createdAt")
})
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Column(nullable = false)
    private Boolean isRead = false;

    @Column(nullable = false)
    private Integer priority = 0;

    @Column(length = 200)
    private String actionLink;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime readAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isRead == null) {
            isRead = false;
        }
        if (priority == null) {
            priority = 0;
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/hotel/entity/Notification.java
git commit -m "feat(entity): 添加 Notification 实体"
```

### Task 4: 创建 SystemSettingsRepository

**Files:**
- Create: `src/main/java/com/hotel/repository/SystemSettingsRepository.java`

- [ ] **Step 1: 创建 SystemSettingsRepository**

```java
package com.hotel.repository;

import com.hotel.entity.SystemSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemSettingsRepository extends JpaRepository<SystemSettings, Long> {

    // 系统设置是单例，总是返回第一条记录
    default SystemSettings getSingleton() {
        return findById(1L).orElseGet(this::createDefault);
    }

    private SystemSettings createDefault() {
        SystemSettings settings = new SystemSettings();
        return save(settings);
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/hotel/repository/SystemSettingsRepository.java
git commit -m "feat(repository): 添加 SystemSettingsRepository"
```

### Task 5: 创建 NotificationRepository

**Files:**
- Create: `src/main/java/com/hotel/repository/NotificationRepository.java`

- [ ] **Step 1: 创建 NotificationRepository**

```java
package com.hotel.repository;

import com.hotel.entity.Notification;
import com.hotel.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    long countByUserIdAndIsReadFalse(String userId);

    List<Notification> findByUserIdAndIsReadFalse(String userId);

    @Query("DELETE FROM Notification n WHERE n.userId = :userId AND n.isRead = true AND n.readAt < :date")
    void deleteOldReadNotifications(@Param("userId") String userId, @Param("date") LocalDateTime date);

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.type = :type ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdAndType(@Param("userId") String userId, @Param("type") NotificationType type);
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/hotel/repository/NotificationRepository.java
git commit -m "feat(repository): 添加 NotificationRepository"
```

---

## Chunk 2: DTO 和异常类

此块创建所有请求/响应 DTO 和异常类。

### Task 6: 创建 SettingsRequest DTO

**Files:**
- Create: `src/main/java/com/hotel/dto/SettingsRequest.java`

- [ ] **Step 1: 创建 SettingsRequest DTO**

```java
package com.hotel.dto;

import lombok.Data;

import javax.validation.constraints.*;

@Data
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

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/hotel/dto/SettingsRequest.java
git commit -m "feat(dto): 添加 SettingsRequest DTO"
```

### Task 7: 创建 SettingsResponse DTO

**Files:**
- Create: `src/main/java/com/hotel/dto/SettingsResponse.java`

- [ ] **Step 1: 创建 SettingsResponse DTO**

```java
package com.hotel.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
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

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/hotel/dto/SettingsResponse.java
git commit -m "feat(dto): 添加 SettingsResponse DTO"
```

### Task 8: 创建 NotificationResponse DTO

**Files:**
- Create: `src/main/java/com/hotel/dto/NotificationResponse.java`

- [ ] **Step 1: 创建 NotificationResponse DTO**

```java
package com.hotel.dto;

import com.hotel.entity.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private NotificationType type;
    private Boolean isRead;
    private Integer priority;
    private String actionLink;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/hotel/dto/NotificationResponse.java
git commit -m "feat(dto): 添加 NotificationResponse DTO"
```

### Task 9: 创建 NotificationListResponse DTO

**Files:**
- Create: `src/main/java/com/hotel/dto/NotificationListResponse.java`

- [ ] **Step 1: 创建 NotificationListResponse DTO**

```java
package com.hotel.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NotificationListResponse {
    private List<NotificationResponse> notifications;
    private Long total;
    private Integer unreadCount;
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/hotel/dto/NotificationListResponse.java
git commit -m "feat(dto): 添加 NotificationListResponse DTO"
```

### Task 10: 创建 ChangePasswordRequest DTO

**Files:**
- Create: `src/main/java/com/hotel/dto/ChangePasswordRequest.java`

- [ ] **Step 1: 创建 ChangePasswordRequest DTO**

```java
package com.hotel.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class ChangePasswordRequest {

    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String newPassword;
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/hotel/dto/ChangePasswordRequest.java
git commit -m "feat(dto): 添加 ChangePasswordRequest DTO"
```

### Task 11: 创建 SettingsNotFoundException 异常

**Files:**
- Create: `src/main/java/com/hotel/exception/SettingsNotFoundException.java`
- Modify: `src/main/java/com/hotel/exception/GlobalExceptionHandler.java`

- [ ] **Step 1: 创建 SettingsNotFoundException**

```java
package com.hotel.exception;

public class SettingsNotFoundException extends RuntimeException {
    public SettingsNotFoundException(String message) {
        super(message);
    }
}
```

- [ ] **Step 2: 读取并验证 GlobalExceptionHandler 结构**

```bash
# 确认 GlobalExceptionHandler 存在并了解其结构
cat src/main/java/com/hotel/exception/GlobalExceptionHandler.java
```

- [ ] **Step 3: 在 GlobalExceptionHandler 中添加处理**

在 GlobalExceptionHandler 类中添加以下方法（在现有异常处理方法之后）：

```java
@ExceptionHandler(SettingsNotFoundException.class)
public ResponseEntity<ApiResponse<?>> handleSettingsNotFoundException(SettingsNotFoundException ex) {
    return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ex.getMessage()));
}
```

同时确保导入 HttpStatus：
```java
import org.springframework.http.HttpStatus;
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/hotel/exception/SettingsNotFoundException.java src/main/java/com/hotel/exception/GlobalExceptionHandler.java
git commit -m "feat(exception): 添加 SettingsNotFoundException 及其处理"
```

---

## Chunk 3: 服务层

此块创建 SettingsService 和 NotificationService 接口及实现。

### Task 12: 创建 SettingsService 接口

**Files:**
- Create: `src/main/java/com/hotel/service/SettingsService.java`

- [ ] **Step 1: 创建 SettingsService 接口**

```java
package com.hotel.service;

import com.hotel.dto.SettingsRequest;
import com.hotel.dto.SettingsResponse;

public interface SettingsService {
    SettingsResponse getSettings();
    SettingsResponse updateSettings(SettingsRequest request, String updatedBy);
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/hotel/service/SettingsService.java
git commit -m "feat(service): 添加 SettingsService 接口"
```

### Task 13: 创建 NotificationService 接口

**Files:**
- Create: `src/main/java/com/hotel/service/NotificationService.java`

- [ ] **Step 1: 创建 NotificationService 接口**

```java
package com.hotel.service;

import com.hotel.dto.NotificationListResponse;
import com.hotel.dto.NotificationResponse;
import com.hotel.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {
    NotificationListResponse getUserNotifications(String userId, Pageable pageable);
    Integer getUnreadCount(String userId);
    NotificationResponse markAsRead(Long notificationId, String userId);
    void markAllAsRead(String userId);
    NotificationResponse createNotification(String userId, String title, String message, NotificationType type, Integer priority, String actionLink);
    void cleanupOldNotifications(String userId);
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/hotel/service/NotificationService.java
git commit -m "feat(service): 添加 NotificationService 接口"
```

### Task 14: 创建 SettingsMapper

**Files:**
- Create: `src/main/java/com/hotel/mapper/SettingsMapper.java`

- [ ] **Step 1: 创建 SettingsMapper**

```java
package com.hotel.mapper;

import com.hotel.dto.SettingsRequest;
import com.hotel.dto.SettingsResponse;
import com.hotel.entity.SystemSettings;
import org.springframework.stereotype.Component;

@Component
public class SettingsMapper {

    public SettingsResponse toResponse(SystemSettings entity) {
        SettingsResponse response = new SettingsResponse();
        response.setId(entity.getId());
        response.setHotelName(entity.getHotelName());
        response.setContactEmail(entity.getContactEmail());
        response.setContactPhone(entity.getContactPhone());
        response.setAddress(entity.getAddress());
        response.setCurrency(entity.getCurrency());
        response.setTimezone(entity.getTimezone());
        response.setLanguage(entity.getLanguage());
        response.setTwoFactorEnabled(entity.getTwoFactorEnabled());
        response.setSessionTimeout(entity.getSessionTimeout());
        response.setPasswordExpiry(entity.getPasswordExpiry());
        response.setEmailNotificationBookings(entity.getEmailNotificationBookings());
        response.setEmailNotificationCancellations(entity.getEmailNotificationCancellations());
        response.setPushNotificationsEnabled(entity.getPushNotificationsEnabled());
        response.setUpdatedAt(entity.getUpdatedAt());
        response.setUpdatedBy(entity.getUpdatedBy());
        return response;
    }

    public void updateEntityFromRequest(SettingsRequest request, SystemSettings entity) {
        entity.setHotelName(request.getHotelName());
        entity.setContactEmail(request.getContactEmail());
        entity.setContactPhone(request.getContactPhone());
        entity.setAddress(request.getAddress());
        entity.setCurrency(request.getCurrency());
        entity.setTimezone(request.getTimezone());
        entity.setLanguage(request.getLanguage());
        entity.setTwoFactorEnabled(request.getTwoFactorEnabled());
        entity.setSessionTimeout(request.getSessionTimeout());
        entity.setPasswordExpiry(request.getPasswordExpiry());
        entity.setEmailNotificationBookings(request.getEmailNotificationBookings());
        entity.setEmailNotificationCancellations(request.getEmailNotificationCancellations());
        entity.setPushNotificationsEnabled(request.getPushNotificationsEnabled());
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/hotel/mapper/SettingsMapper.java
git commit -m "feat(mapper): 添加 SettingsMapper"
```

### Task 15: 创建 NotificationMapper

**Files:**
- Create: `src/main/java/com/hotel/mapper/NotificationMapper.java`

- [ ] **Step 1: 创建 NotificationMapper**

```java
package com.hotel.mapper;

import com.hotel.dto.NotificationResponse;
import com.hotel.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification entity) {
        NotificationResponse response = new NotificationResponse();
        response.setId(entity.getId());
        response.setTitle(entity.getTitle());
        response.setMessage(entity.getMessage());
        response.setType(entity.getType());
        response.setIsRead(entity.getIsRead());
        response.setPriority(entity.getPriority());
        response.setActionLink(entity.getActionLink());
        response.setCreatedAt(entity.getCreatedAt());
        response.setReadAt(entity.getReadAt());
        return response;
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/hotel/mapper/NotificationMapper.java
git commit -m "feat(mapper): 添加 NotificationMapper"
```

### Task 16: 创建 SettingsServiceImpl

**Files:**
- Create: `src/main/java/com/hotel/service/impl/SettingsServiceImpl.java`

- [ ] **Step 1: 创建 SettingsServiceImpl**

```java
package com.hotel.service.impl;

import com.hotel.dto.SettingsRequest;
import com.hotel.dto.SettingsResponse;
import com.hotel.entity.SystemSettings;
import com.hotel.mapper.SettingsMapper;
import com.hotel.repository.SystemSettingsRepository;
import com.hotel.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettingsServiceImpl implements SettingsService {

    private final SystemSettingsRepository repository;
    private final SettingsMapper mapper;

    @Override
    public SettingsResponse getSettings() {
        SystemSettings settings = repository.getSingleton();
        return mapper.toResponse(settings);
    }

    @Override
    @Transactional
    public SettingsResponse updateSettings(SettingsRequest request, String updatedBy) {
        SystemSettings settings = repository.getSingleton();
        mapper.updateEntityFromRequest(request, settings);
        settings.setUpdatedBy(updatedBy);
        SystemSettings saved = repository.save(settings);
        return mapper.toResponse(saved);
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/hotel/service/impl/SettingsServiceImpl.java
git commit -m "feat(service): 添加 SettingsServiceImpl"
```

### Task 17: 创建 NotificationServiceImpl

**Files:**
- Create: `src/main/java/com/hotel/service/impl/NotificationServiceImpl.java`

- [ ] **Step 1: 创建 NotificationServiceImpl**

```java
package com.hotel.service.impl;

import com.hotel.dto.NotificationListResponse;
import com.hotel.dto.NotificationResponse;
import com.hotel.entity.Notification;
import com.hotel.entity.NotificationType;
import com.hotel.mapper.NotificationMapper;
import com.hotel.repository.NotificationRepository;
import com.hotel.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repository;
    private final NotificationMapper mapper;

    @Override
    public NotificationListResponse getUserNotifications(String userId, Pageable pageable) {
        Page<Notification> page = repository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        List<NotificationResponse> notifications = page.getContent().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return NotificationListResponse.builder()
                .notifications(notifications)
                .total(page.getTotalElements())
                .unreadCount(getUnreadCount(userId))
                .build();
    }

    @Override
    public Integer getUnreadCount(String userId) {
        return Math.toIntExact(repository.countByUserIdAndIsReadFalse(userId));
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, String userId) {
        Notification notification = repository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            repository.save(notification);
        }

        return mapper.toResponse(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(String userId) {
        List<Notification> unreadNotifications = repository.findByUserIdAndIsReadFalse(userId);
        LocalDateTime now = LocalDateTime.now();

        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
            notification.setReadAt(now);
        }

        repository.saveAll(unreadNotifications);
    }

    @Override
    @Transactional
    public NotificationResponse createNotification(String userId, String title, String message,
                                                   NotificationType type, Integer priority, String actionLink) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setPriority(priority != null ? priority : 0);
        notification.setActionLink(actionLink);

        Notification saved = repository.save(notification);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void cleanupOldNotifications(String userId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        repository.deleteOldReadNotifications(userId, thirtyDaysAgo);
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/hotel/service/impl/NotificationServiceImpl.java
git commit -m "feat(service): 添加 NotificationServiceImpl"
```

---

## Chunk 4: 控制器层

此块创建 SettingsController 和 NotificationController。

### Task 18: 创建 SettingsController

**Files:**
- Create: `src/main/java/com/hotel/controller/SettingsController.java`

- [ ] **Step 1: 创建 SettingsController**

```java
package com.hotel.controller;

import com.hotel.dto.ApiResponse;
import com.hotel.dto.SettingsRequest;
import com.hotel.dto.SettingsResponse;
import com.hotel.entity.UserRole;
import com.hotel.security.UserDetailsImpl;
import com.hotel.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    public ResponseEntity<ApiResponse<SettingsResponse>> getSettings() {
        SettingsResponse response = settingsService.getSettings();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping
    @Secured("ROLE_ADMIN")
    public ResponseEntity<ApiResponse<SettingsResponse>> updateSettings(
            @Valid @RequestBody SettingsRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        SettingsResponse response = settingsService.updateSettings(request, userDetails.getEmail());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/hotel/controller/SettingsController.java
git commit -m "feat(controller): 添加 SettingsController"
```

### Task 19: 创建 NotificationController

**Files:**
- Create: `src/main/java/com/hotel/controller/NotificationController.java`

- [ ] **Step 1: 创建 NotificationController**

```java
package com.hotel.controller;

import com.hotel.dto.ApiResponse;
import com.hotel.dto.NotificationListResponse;
import com.hotel.dto.NotificationResponse;
import com.hotel.security.UserDetailsImpl;
import com.hotel.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<NotificationListResponse>> getNotifications(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        NotificationListResponse response = notificationService.getUserNotifications(
                userDetails.getEmail(), pageRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Integer>> getUnreadCount(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Integer count = notificationService.getUnreadCount(userDetails.getEmail());
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        NotificationResponse response = notificationService.markAsRead(id, userDetails.getEmail());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        notificationService.markAllAsRead(userDetails.getEmail());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/hotel/controller/NotificationController.java
git commit -m "feat(controller): 添加 NotificationController"
```

---

## Chunk 5: 密码管理功能

此块扩展 ProfileService 和 ProfileController 添加修改密码功能。

### Task 20: 扩展 ProfileService 接口

**Files:**
- Modify: `src/main/java/com/hotel/service/ProfileService.java`

- [ ] **Step 1: 添加修改密码方法到接口**

在 `ProfileService` 接口中添加：

```java
void changePassword(String email, ChangePasswordRequest request);
```

确保添加导入：
```java
import com.hotel.dto.ChangePasswordRequest;
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/hotel/service/ProfileService.java
git commit -m "feat(service): ProfileService 添加修改密码方法"
```

### Task 21: 实现修改密码方法

**Files:**
- Modify: `src/main/java/com/hotel/service/impl/ProfileServiceImpl.java`

- [ ] **Step 1: 读取现有 ProfileServiceImpl 结构**

```bash
# 查看现有文件结构
cat src/main/java/com/hotel/service/impl/ProfileServiceImpl.java
```

- [ ] **Step 2: 验证依赖注入**

ProfileServiceImpl 使用 `@RequiredArgsConstructor`，并且已经有了 `UserRepository` 注入（名为 `userRepository`）。无需修改构造函数。

- [ ] **Step 3: 添加 PasswordEncoder 依赖注入**

由于类使用 Lombok 的 `@RequiredArgsConstructor`，只需在类中添加字段：

```java
private final PasswordEncoder passwordEncoder;
```

在类顶部添加导入：
```java
import org.springframework.security.crypto.password.PasswordEncoder;
```

- [ ] **Step 4: 实现修改密码方法**

在 ProfileServiceImpl 类中添加：

```java
@Override
@Transactional
public void changePassword(String email, ChangePasswordRequest request) {
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ProfileNotFoundException("用户不存在"));

    // 验证旧密码
    if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
        throw new IllegalArgumentException("旧密码不正确");
    }

    // 设置新密码
    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
}
```

确保添加导入：
```java
import com.hotel.dto.ChangePasswordRequest;
import org.springframework.transaction.annotation.Transactional;
```

- [ ] **Step 5: 编译验证**

```bash
mvn compile -DskipTests
```

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/hotel/service/impl/ProfileServiceImpl.java
git commit -m "feat(service): 实现修改密码方法"
```

### Task 22: 添加修改密码端点

**Files:**
- Modify: `src/main/java/com/hotel/controller/ProfileController.java`
- Modify: `src/main/java/com/hotel/exception/GlobalExceptionHandler.java`

- [ ] **Step 1: 读取现有 ProfileController 结构**

```bash
cat src/main/java/com/hotel/controller/ProfileController.java
```

- [ ] **Step 2: 添加修改密码端点**

在 ProfileController 类中添加（在现有方法之后）：

```java
@PutMapping("/password")
public ResponseEntity<ApiResponse<Void>> changePassword(
        @Valid @RequestBody ChangePasswordRequest request,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
    profileService.changePassword(userDetails.getEmail(), request);
    return ResponseEntity.ok(ApiResponse.success(null));
}
```

确保添加导入：
```java
import com.hotel.dto.ChangePasswordRequest;
```

- [ ] **Step 3: 检查 GlobalExceptionHandler 是否已有 IllegalArgumentException 处理**

```bash
grep -n "IllegalArgumentException" src/main/java/com/hotel/exception/GlobalExceptionHandler.java
```

如果返回空结果，添加处理方法。

- [ ] **Step 4: 添加 IllegalArgumentException 处理（如果不存在）**

在 GlobalExceptionHandler 类中添加：

```java
@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(IllegalArgumentException ex) {
    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.getMessage()));
}
```

- [ ] **Step 5: 编译验证**

```bash
mvn compile -DskipTests
```

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/hotel/controller/ProfileController.java src/main/java/com/hotel/exception/GlobalExceptionHandler.java
git commit -m "feat(controller): 添加修改密码端点及错误处理"
```

---

## Chunk 6: 数据初始化

此块修改 DataInitializer 以初始化默认系统设置。

### Task 23: 初始化系统设置

**Files:**
- Modify: `src/main/java/com/hotel/init/DataInitializer.java`

- [ ] **Step 1: 读取现有 DataInitializer 结构**

DataInitializer 使用 `@Autowired` 字段注入，使用 `@EventListener(ApplicationReadyEvent.class)` 和 `initializeData()` 方法。

- [ ] **Step 2: 添加 SystemSettingsRepository 依赖**

在 DataInitializer 类中添加字段（在其他 @Autowired 字段之后）：

```java
@Autowired
private SystemSettingsRepository systemSettingsRepository;
```

- [ ] **Step 3: 添加导入**

在类顶部添加：
```java
import com.hotel.entity.SystemSettings;
import com.hotel.repository.SystemSettingsRepository;
```

- [ ] **Step 4: 在 initializeData() 方法中添加调用**

在 `initializeData()` 方法的 `logger.info("DataInitializer completed");` 之前添加：

```java
// Initialize system settings
initializeSystemSettings();
```

- [ ] **Step 5: 添加初始化方法**

在 DataInitializer 类中添加私有方法（在 migrateDatabase() 方法之后）：

```java
private void initializeSystemSettings() {
    try {
        if (systemSettingsRepository.count() > 0) {
            logger.info("System settings already exist, skipping initialization");
            return;
        }

        SystemSettings settings = new SystemSettings();
        systemSettingsRepository.save(settings);
        logger.info("Default system settings initialized successfully");
    } catch (Exception e) {
        logger.warn("System settings initialization failed: {}", e.getMessage());
    }
}
```

- [ ] **Step 6: 编译验证**

```bash
mvn compile -DskipTests
```

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/hotel/init/DataInitializer.java
git commit -m "feat(init): 添加系统设置初始化"
```

---

## Chunk 7: 前端类型定义

此块创建前端 TypeScript 类型定义。

### Task 24: 创建设置类型定义

**Files:**
- Create: `frontend/src/types/settings.ts`

- [ ] **Step 1: 创建设置类型定义**

```typescript
export interface Settings {
  id: number
  hotelName: string
  contactEmail: string
  contactPhone: string
  address: string
  currency: string
  timezone: string
  language: string
  twoFactorEnabled: boolean
  sessionTimeout: number
  passwordExpiry: number
  emailNotificationBookings: boolean
  emailNotificationCancellations: boolean
  pushNotificationsEnabled: boolean
  updatedAt: string
  updatedBy: string
}

export interface SettingsRequest {
  hotelName: string
  contactEmail: string
  contactPhone: string
  address: string
  currency: string
  timezone: string
  language: string
  twoFactorEnabled: boolean
  sessionTimeout: number
  passwordExpiry: number
  emailNotificationBookings: boolean
  emailNotificationCancellations: boolean
  pushNotificationsEnabled: boolean
}

export type Currency = 'CNY' | 'USD' | 'EUR' | 'GBP'
export type Timezone = 'UTC+8' | 'UTC+0' | 'UTC-5'
export type Language = 'Chinese' | 'English' | 'Spanish'
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/types/settings.ts
git commit -m "feat(types): 添加设置类型定义"
```

### Task 25: 创建通知类型定义

**Files:**
- Create: `frontend/src/types/notification.ts`

- [ ] **Step 1: 创建通知类型定义**

```typescript
export type NotificationType = 'BOOKING' | 'CANCELLATION' | 'CHECK_IN' | 'CHECK_OUT' | 'SYSTEM'

export interface Notification {
  id: number
  title: string
  message: string
  type: NotificationType
  isRead: boolean
  priority: number
  actionLink: string | null
  createdAt: string
  readAt: string | null
}

export interface NotificationListResponse {
  notifications: Notification[]
  total: number
  unreadCount: number
}

export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/types/notification.ts
git commit -m "feat(types): 添加通知类型定义"
```

---

## Chunk 8: 前端 API 工具

此块创建前端 API 工具函数。

### Task 26: 创建设置 API

**Files:**
- Create: `frontend/src/api/settings.ts`
- Verify: `frontend/src/types/api.ts`

- [ ] **Step 1: 检查 ApiResponse 类型是否存在**

```bash
# 检查 api.ts 是否定义了 ApiResponse
grep -n "ApiResponse" frontend/src/types/api.ts || echo "ApiResponse not found"
```

- [ ] **Step 2: 创建 api.ts 类型文件（如果不存在）**

如果 ApiResponse 不存在，创建 `frontend/src/types/api.ts`：

```typescript
export interface ApiResponse<T> {
  success: boolean
  data: T
  message?: string
}
```

- [ ] **Step 3: 创建设置 API**

创建 `frontend/src/api/settings.ts`：

```typescript
import api from '@/utils/api'
import type { Settings, SettingsRequest } from '@/types/settings'
import type { ApiResponse } from '@/types/api'

export const settingsApi = {
  // 获取系统设置
  getSettings: async (): Promise<Settings> => {
    const response = await api.get<ApiResponse<Settings>>('/api/settings')
    return response.data.data
  },

  // 更新系统设置
  updateSettings: async (request: SettingsRequest): Promise<Settings> => {
    const response = await api.put<ApiResponse<Settings>>('/api/settings', request)
    return response.data.data
  }
}
```

- [ ] **Step 4: Commit**

```bash
git add frontend/src/api/settings.ts frontend/src/types/api.ts
git commit -m "feat(api): 添加设置 API"
```

### Task 27: 创建通知 API 和扩展 profile API

**Files:**
- Create: `frontend/src/api/notification.ts`
- Modify: `frontend/src/api/profile.ts`

- [ ] **Step 1: 读取现有 profile.ts 结构**

```bash
cat frontend/src/api/profile.ts
```

文件当前使用函数导出方式，需要保持一致。

- [ ] **Step 2: 创建通知 API**

创建 `frontend/src/api/notification.ts`：

```typescript
import api from '@/utils/api'
import type { Notification, NotificationListResponse } from '@/types/notification'
import type { ApiResponse } from '@/types/api'

export const notificationApi = {
  // 获取通知列表
  getNotifications: async (page = 0, size = 10): Promise<NotificationListResponse> => {
    const response = await api.get<ApiResponse<NotificationListResponse>>(
      `/api/notifications?page=${page}&size=${size}`
    )
    return response.data.data
  },

  // 获取未读通知数量
  getUnreadCount: async (): Promise<number> => {
    const response = await api.get<ApiResponse<number>>('/api/notifications/unread-count')
    return response.data.data
  },

  // 标记为已读
  markAsRead: async (id: number): Promise<Notification> => {
    const response = await api.put<ApiResponse<Notification>>(`/api/notifications/${id}/read`)
    return response.data.data
  },

  // 标记所有为已读
  markAllAsRead: async (): Promise<void> => {
    await api.put('/api/notifications/read-all')
  }
}
```

- [ ] **Step 3: 扩展 profile.ts 添加密码修改**

在 `frontend/src/api/profile.ts` 中添加：

```typescript
// 在现有导入后添加
import type { ChangePasswordRequest } from '@/types/notification'

// 在文件末尾添加函数
export const changePassword = async (request: ChangePasswordRequest): Promise<void> => {
  const response = await api.put<ApiResponse<void>>('/api/profile/password', request)
  return response.data
}
```

- [ ] **Step 4: Commit**

```bash
git add frontend/src/api/notification.ts frontend/src/api/profile.ts
git commit -m "feat(api): 添加通知 API 和密码修改 API"
```

---

## Chunk 9: 前端组件 - 修改密码对话框

此块创建修改密码对话框组件。

### Task 28: 创建修改密码对话框组件

**Files:**
- Create: `frontend/src/components/ChangePasswordDialog.vue`

- [ ] **Step 1: 创建组件**

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { ChangePasswordRequest } from '../types/notification'
import { profileApi } from '../api/profile'

interface Props {
  modelValue: boolean
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const form = ref<ChangePasswordRequest>({
  oldPassword: '',
  newPassword: ''
})

const confirmPassword = ref('')
const loading = ref(false)

const rules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度必须在6-20个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (value !== form.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const formRef = ref()

const handleClose = () => {
  emit('update:modelValue', false)
  resetForm()
}

const resetForm = () => {
  form.value = {
    oldPassword: '',
    newPassword: ''
  }
  confirmPassword.value = ''
  formRef.value?.clearValidate()
}

const handleSubmit = async () => {
  try {
    const valid = await formRef.value?.validate()
    if (!valid) return

    loading.value = true
    await profileApi.changePassword(form.value)
    ElMessage.success('密码修改成功')
    handleClose()
  } catch (error: any) {
    if (error?.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('密码修改失败，请重试')
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    title="修改密码"
    width="400px"
    @update:model-value="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="旧密码" prop="oldPassword">
        <el-input
          v-model="form.oldPassword"
          type="password"
          placeholder="请输入旧密码"
          show-password
        />
      </el-form-item>

      <el-form-item label="新密码" prop="newPassword">
        <el-input
          v-model="form.newPassword"
          type="password"
          placeholder="请输入新密码"
          show-password
        />
      </el-form-item>

      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
          v-model="confirmPassword"
          type="password"
          placeholder="请再次输入新密码"
          show-password
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/components/ChangePasswordDialog.vue
git commit -m "feat(component): 添加修改密码对话框组件"
```

---

## Chunk 10: 前端组件 - 通知下拉组件

此块创建通知下拉组件和 composable。

### Task 29: 创建通知轮询 composable

**Files:**
- Create: `frontend/src/composables/useNotifications.ts`

- [ ] **Step 1: 创建 composable**

```typescript
import { ref, onMounted, onUnmounted } from 'vue'
import { notificationApi } from '../api/notification'

export function useNotifications() {
  const unreadCount = ref(0)
  let pollingTimer: ReturnType<typeof setInterval> | null = null
  const POLLING_INTERVAL = 30000 // 30秒

  const fetchUnreadCount = async () => {
    try {
      unreadCount.value = await notificationApi.getUnreadCount()
    } catch (error) {
      console.error('获取未读通知数量失败:', error)
    }
  }

  const startPolling = () => {
    fetchUnreadCount()
    pollingTimer = setInterval(fetchUnreadCount, POLLING_INTERVAL)
  }

  const stopPolling = () => {
    if (pollingTimer) {
      clearInterval(pollingTimer)
      pollingTimer = null
    }
  }

  const refresh = () => {
    fetchUnreadCount()
  }

  onMounted(() => {
    startPolling()
  })

  onUnmounted(() => {
    stopPolling()
  })

  return {
    unreadCount,
    refresh,
    startPolling,
    stopPolling
  }
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/composables/useNotifications.ts
git commit -m "feat(composable): 添加通知轮询 composable"
```

### Task 30: 创建通知下拉组件

**Files:**
- Create: `frontend/src/components/NotificationDropdown.vue`

- [ ] **Step 1: 创建组件**

```vue
<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { Notification } from '../types/notification'
import { notificationApi } from '../api/notification'
import { useNotifications } from '../composables/useNotifications'

const router = useRouter()
const { unreadCount, refresh } = useNotifications()

const notifications = ref<Notification[]>([])
const loading = ref(false)

const displayCount = computed(() => {
  return unreadCount.value > 99 ? '99+' : unreadCount.value.toString()
})

const hasNotifications = computed(() => {
  return unreadCount.value > 0
})

const fetchNotifications = async () => {
  try {
    loading.value = true
    const response = await notificationApi.getNotifications(0, 5)
    notifications.value = response.notifications
  } catch (error) {
    console.error('获取通知列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleMarkAsRead = async (notification: Notification) => {
  if (!notification.isRead) {
    try {
      await notificationApi.markAsRead(notification.id)
      notification.isRead = true
      refresh()
    } catch (error) {
      ElMessage.error('标记失败')
    }
  }

  if (notification.actionLink) {
    router.push(notification.actionLink)
  }
}

const handleMarkAllAsRead = async () => {
  try {
    await notificationApi.markAllAsRead()
    notifications.value.forEach(n => n.isRead = true)
    refresh()
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const getTimeAgo = (dateString: string): string => {
  const date = new Date(dateString)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffMins = Math.floor(diffMs / 60000)

  if (diffMins < 1) return '刚刚'
  if (diffMins < 60) return `${diffMins}分钟前`
  const diffHours = Math.floor(diffMins / 60)
  if (diffHours < 24) return `${diffHours}小时前`
  const diffDays = Math.floor(diffHours / 24)
  return `${diffDays}天前`
}

const getNotificationIcon = (type: string) => {
  switch (type) {
    case 'BOOKING': return 'document'
    case 'CANCELLATION': return 'close'
    case 'CHECK_IN': return 'check'
    case 'CHECK_OUT': return 'house'
    case 'SYSTEM': return 'setting'
    default: return 'bell'
  }
}

const getNotificationColor = (priority: number) => {
  switch (priority) {
    case 2: return 'danger'
    case 1: return 'warning'
    default: return 'primary'
  }
}
</script>

<template>
  <el-dropdown trigger="click" @visible-change="fetchNotifications">
    <div class="notification-bell cursor-pointer">
      <el-badge :value="displayCount" :hidden="!hasNotifications">
        <el-icon :size="20">
          <Bell />
        </el-icon>
      </el-badge>
    </div>

    <template #dropdown>
      <el-dropdown-menu class="notification-dropdown">
        <div class="notification-header">
          <span class="notification-title">通知</span>
          <el-button
            v-if="hasNotifications"
            link
            type="primary"
            @click="handleMarkAllAsRead"
          >
            全部已读
          </el-button>
        </div>

        <div v-if="loading" class="notification-loading">
          <el-icon class="is-loading"><Loading /></el-icon>
        </div>

        <div v-else-if="notifications.length === 0" class="notification-empty">
          <el-empty description="暂无通知" :image-size="60" />
        </div>

        <div v-else class="notification-list">
          <div
            v-for="notification in notifications"
            :key="notification.id"
            class="notification-item"
            :class="{ unread: !notification.isRead }"
            @click="handleMarkAsRead(notification)"
          >
            <div class="notification-icon">
              <el-tag :type="getNotificationColor(notification.priority)" size="small">
                {{ getNotificationIcon(notification.type) }}
              </el-tag>
            </div>
            <div class="notification-content">
              <div class="notification-message">{{ notification.title }}</div>
              <div class="notification-desc">{{ notification.message }}</div>
              <div class="notification-time">{{ getTimeAgo(notification.createdAt) }}</div>
            </div>
            <div v-if="!notification.isRead" class="notification-dot"></div>
          </div>
        </div>

        <div v-if="notifications.length >= 5" class="notification-footer">
          <el-button link @click="router.push('/notifications')">
            查看全部
          </el-button>
        </div>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<style scoped>
.notification-bell {
  padding: 8px;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.notification-bell:hover {
  background-color: #f5f5f5;
}

.notification-dropdown {
  width: 320px;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
}

.notification-title {
  font-weight: 600;
  color: #303133;
}

.notification-loading,
.notification-empty {
  padding: 20px;
  text-align: center;
}

.notification-list {
  max-height: 400px;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  padding: 12px 16px;
  cursor: pointer;
  position: relative;
  transition: background-color 0.2s;
}

.notification-item:hover {
  background-color: #f5f5f5;
}

.notification-item.unread {
  background-color: #ecf5ff;
}

.notification-icon {
  margin-right: 12px;
}

.notification-content {
  flex: 1;
}

.notification-message {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.notification-desc {
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
}

.notification-time {
  font-size: 11px;
  color: #909399;
}

.notification-dot {
  width: 6px;
  height: 6px;
  background-color: #409eff;
  border-radius: 50%;
  position: absolute;
  top: 16px;
  right: 12px;
}

.notification-footer {
  padding: 8px;
  text-align: center;
  border-top: 1px solid #ebeef5;
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/components/NotificationDropdown.vue
git commit -m "feat(component): 添加通知下拉组件"
```

---

## Chunk 11: 前端页面更新

此块更新 Settings.vue 和 Layout.vue 组件。

### Task 31: 更新 Settings.vue

**Files:**
- Modify: `frontend/src/views/Settings.vue`

- [ ] **Step 1: 读取现有 Settings.vue 结构**

```bash
cat frontend/src/views/Settings.vue
```

现有文件结构：
- 使用 `<script setup>` 语法
- 已定义 `activeTab`, `generalSettings`, `securitySettings`, `notificationSettings`
- 导入了 `Layout` 组件

- [ ] **Step 2: 更新 script 部分**

在现有 `<script setup lang="ts">` 中进行以下修改：

1. 在导入部分添加：
```typescript
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import ChangePasswordDialog from '../components/ChangePasswordDialog.vue'
import { settingsApi } from '../api/settings'
import { changePassword } from '../api/profile'
import type { Settings, SettingsRequest } from '../types/settings'
import type { ChangePasswordRequest } from '../types/notification'
```

2. 在现有 ref 定义后添加：
```typescript
const loading = ref(false)
const saving = ref(false)
const showPasswordDialog = ref(false)
```

3. 在 script 末尾添加函数：
```typescript
const loadSettings = async () => {
  try {
    loading.value = true
    const data = await settingsApi.getSettings()
    generalSettings.value.hotelName = data.hotelName
    generalSettings.value.email = data.contactEmail
    generalSettings.value.phone = data.contactPhone
    generalSettings.value.address = data.address
    generalSettings.value.currency = data.currency
    generalSettings.value.timezone = data.timezone
    generalSettings.value.language = data.language
    securitySettings.value.twoFactor = data.twoFactorEnabled
    securitySettings.value.sessionTimeout = data.sessionTimeout
    securitySettings.value.passwordExpiry = data.passwordExpiry
    notificationSettings.value.emailBookings = data.emailNotificationBookings
    notificationSettings.value.emailCancellations = data.emailNotificationCancellations
    notificationSettings.value.smsAlerts = data.pushNotificationsEnabled
  } catch (error) {
    ElMessage.error('加载设置失败')
  } finally {
    loading.value = false
  }
}

const handleSaveSettings = async () => {
  try {
    saving.value = true
    const request: SettingsRequest = {
      hotelName: generalSettings.value.hotelName,
      contactEmail: generalSettings.value.email,
      contactPhone: generalSettings.value.phone,
      address: generalSettings.value.address,
      currency: generalSettings.value.currency,
      timezone: generalSettings.value.timezone,
      language: generalSettings.value.language,
      twoFactorEnabled: securitySettings.value.twoFactor,
      sessionTimeout: securitySettings.value.sessionTimeout,
      passwordExpiry: securitySettings.value.passwordExpiry,
      emailNotificationBookings: notificationSettings.value.emailBookings,
      emailNotificationCancellations: notificationSettings.value.emailCancellations,
      pushNotificationsEnabled: notificationSettings.value.smsAlerts
    }
    await settingsApi.updateSettings(request)
    ElMessage.success('设置保存成功')
  } catch (error: any) {
    if (error?.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('保存失败，请重试')
    }
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadSettings()
})
```

- [ ] **Step 3: 更新 template 部分**

1. 在 `<div class="max-w-4xl mx-auto">` 后添加 `v-loading` 指令：
```vue
<div v-loading="loading" class="max-w-4xl mx-auto">
```

2. 找到"常规设置"标签页的保存按钮，替换为：
```vue
<el-button type="primary" :loading="saving" @click="handleSaveSettings">
  保存更改
</el-button>
```

3. 找到"安全设置"标签页的"更改管理员密码"按钮，替换为：
```vue
<el-button type="primary" plain @click="showPasswordDialog = true">
  更改管理员密码
</el-button>
```

4. 找到"通知设置"标签页的"保存偏好"按钮，替换为：
```vue
<el-button type="primary" :loading="saving" @click="handleSaveSettings">
  保存偏好
</el-button>
```

5. 在 `</Layout>` 标签前添加：
```vue
<ChangePasswordDialog v-model="showPasswordDialog" />
```

- [ ] **Step 4: 验证前端编译**

```bash
cd frontend && npm run build
```

- [ ] **Step 5: Commit**

```bash
git add frontend/src/views/Settings.vue
git commit -m "feat(view): 更新 Settings.vue 连接后端 API"
```

### Task 32: 更新 Layout.vue 添加通知功能

**Files:**
- Modify: `frontend/src/components/Layout.vue`

- [ ] **Step 1: 读取现有 Layout.vue 结构**

```bash
cat frontend/src/components/Layout.vue
```

Layout.vue 当前结构：
- 使用 `<script setup lang="ts">` 语法
- 有菜单导航定义（menuItems）
- 有用户下拉菜单功能
- 需要找到 header 区域（铃铛按钮位置）

- [ ] **Step 2: 查找铃铛按钮位置**

检查模板中是否有铃铛图标。如果没有，需要在 header 中添加通知铃铛。

- [ ] **Step 3: 添加组件导入**

在 script 部分的导入区域添加：
```typescript
import NotificationDropdown from './NotificationDropdown.vue'
```

- [ ] **Step 4: 在 header 中添加通知铃铛**

在 Layout.vue 的 header 区域（用户下拉菜单之前），找到合适位置添加：

```vue
<!-- 通知铃铛 -->
<div class="flex items-center">
  <NotificationDropdown />
</div>
```

具体位置应该在菜单按钮旁边或用户下拉菜单之前。

- [ ] **Step 5: 添加必要的图标导入**

确保 script 中导入了需要的 Element Plus 图标：
```typescript
import { Bell, Loading } from '@element-plus/icons-vue'
```

- [ ] **Step 6: 验证前端编译**

```bash
cd frontend && npm run build
```

- [ ] **Step 7: Commit**

```bash
git add frontend/src/components/Layout.vue
git commit -m "feat(component): Layout.vue 添加通知下拉功能"
```

---

## Chunk 12: 测试和验证

此块进行功能测试和验证。

### Task 33: 测试后端 API

**Files:**
- None (测试任务)

- [ ] **Step 1: 重启后端服务**

```bash
# 停止所有 Java 进程
Stop-Process -Name java -Force -ErrorAction SilentlyContinue

# 重新启动后端
cd E:\project\hotel
mvn spring-boot:run
```

- [ ] **Step 2: 测试设置 API**

使用浏览器或 Postman 测试：
- GET `http://localhost:8080/api/settings` - 应返回默认设置
- PUT `http://localhost:8080/api/settings` - 应更新设置（需要 ADMIN 权限）

- [ ] **Step 3: 测试通知 API**

- GET `http://localhost:8080/api/notifications` - 应返回通知列表
- GET `http://localhost:8080/api/notifications/unread-count` - 应返回未读数量
- PUT `http://localhost:8080/api/notifications/{id}/read` - 应标记为已读

- [ ] **Step 4: 测试密码修改**

- PUT `http://localhost:8080/api/profile/password` - 应修改密码

### Task 34: 测试前端功能

**Files:**
- None (测试任务)

- [ ] **Step 1: 启动前端服务**

```bash
cd E:\project\hotel\frontend
npm run dev
```

- [ ] **Step 2: 测试设置页面**

1. 访问 `http://localhost:3000/settings`
2. 验证设置正确加载
3. 修改设置并保存
4. 刷新页面验证设置已保存

- [ ] **Step 3: 测试通知功能**

1. 检查铃铛按钮显示未读数量
2. 点击铃铛查看通知列表
3. 点击标记已读功能
4. 验证徽章数量更新

- [ ] **Step 4: 测试密码修改**

1. 点击设置页面安全标签的"更改管理员密码"按钮
2. 输入旧密码和新密码
3. 验证密码修改成功
4. 用新密码重新登录验证

### Task 35: 最终验证和提交

**Files:**
- None (验证任务)

- [ ] **Step 1: 检查数据库**

验证数据库中创建了以下表：
- `system_settings` - 系统设置表
- `notifications` - 通知表

- [ ] **Step 2: 检查前端构建**

```bash
cd E:\project\hotel\frontend
npm run build
```

- [ ] **Step 3: 最终代码提交**

```bash
git status
git add .
git commit -m "feat: 完成系统设置功能实现"
```

---

## 完成检查清单

- [ ] 所有实体类已创建并正确配置
- [ ] 所有 DTO 类已创建并包含验证注解
- [ ] 所有 Repository 接口已创建
- [ ] 所有 Service 接口和实现类已创建
- [ ] 所有 Controller 已创建并正确配置路由
- [ ] ProfileController 已扩展密码修改功能
- [ ] DataInitializer 已更新以初始化系统设置
- [ ] 所有前端类型定义已创建
- [ ] 所有前端 API 工具已创建
- [ ] NotificationDropdown 组件已创建并集成到 Layout.vue
- [ ] ChangePasswordDialog 组件已创建并集成到 Settings.vue
- [ ] Settings.vue 已更新连接后端 API
- [ ] Layout.vue 已更新添加通知功能
- [ ] 所有功能已测试验证
- [ ] 代码已提交到版本控制
