---
phase: 06-profile
plan: 01
subsystem: Profile Management
tags: [profile, review, entity, dto, database]
requires:
  provides:
    - ProfileResponse
    - UpdateProfileRequest
    - ReviewRequest
    - ReviewResponse
    - BookingReviewResponse
    - User entity extension
    - Review entity
    - ReviewRepository
  affects:
    - Phase 06-02 (Profile Service)
    - Phase 06-03 (Profile Controller)
tech-stack:
  added: []
  patterns:
    - JPA entity relationships (@ManyToOne)
    - DTO pattern for API responses
    - Repository pattern with Spring Data JPA
    - Cascade delete for referential integrity
key-files:
  created:
    - src/main/java/com/hotel/entity/Review.java
    - src/main/java/com/hotel/repository/ReviewRepository.java
    - src/main/java/com/hotel/dto/ProfileResponse.java
    - src/main/java/com/hotel/dto/UpdateProfileRequest.java
    - src/main/java/com/hotel/dto/ReviewRequest.java
    - src/main/java/com/hotel/dto/ReviewResponse.java
    - src/main/java/com/hotel/dto/BookingReviewResponse.java
    - src/main/java/com/hotel/exception/ProfileNotFoundException.java
    - src/main/java/com/hotel/exception/ReviewAlreadyExistsException.java
  modified:
    - src/main/java/com/hotel/entity/User.java
    - src/main/resources/schema.sql
decisions:
  - "User实体添加phone、address、nationality、preferencesEnabled字段支持个人资料管理"
  - "Review实体通过@ManyToOne关联Booking和User，使用级联删除保证数据一致性"
  - "preferencesEnabled默认值设为true，用户默认接收通知"
  - "reviews表添加UNIQUE约束确保每个订单只能评价一次"
metrics:
  duration: 3 minutes
  completed: "2026-03-15"
  tasks: 3
  files: 10
---

# Phase 06 Plan 01: 个人资料和评价数据模型实现 Summary

个人资料管理和评价反馈的数据模型实现，包括User实体扩展、Review实体、DTO类、Repository接口和数据库schema更新

## One-Liner

扩展User实体添加个人资料字段，创建Review实体支持订单评价，定义完整DTO体系和数据库schema

## Deviations from Plan

无 - 计划完全按预期执行

## Implementation Details

### Task 1: 扩展User实体和创建Review实体

**User实体扩展:**
- 添加 `phone` (String) - 电话号码
- 添加 `address` (String) - 地址
- 添加 `nationality` (String) - 国籍
- 添加 `preferencesEnabled` (Boolean, 默认true) - 通知偏好

**Review实体创建:**
- 通过 `@ManyToOne` 关联 Booking
- 通过 `@ManyToOne` 关联 User
- 包含 rating (1-5星) 和 comment 字段
- 自动设置 createdAt 时间戳

**ReviewRepository:**
- `findByBookingId(Long bookingId)` - 根据订单ID查找评价
- `findByUserId(Long userId)` - 根据用户ID查找所有评价
- `existsByBookingId(Long bookingId)` - 检查订单是否已评价
- `deleteByBookingId(Long bookingId)` - 根据订单ID删除评价

**Commit:** `85f607e`

### Task 2: 创建DTO类和异常类

**ProfileResponse:** 匹配前端 Profile.vue 的数据结构
- firstName/lastName (从name字段拆分)
- email, phone, nationality
- preferencesEnabled (映射到前端的notifications)
- role

**UpdateProfileRequest:** 个人资料更新请求
- name验证 (@NotBlank)
- 可选字段: phone, address, nationality, preferencesEnabled

**ReviewRequest:** 评价提交请求
- bookingId验证 (@NotNull)
- rating验证 (@Min=1, @Max=5)
- comment验证 (@NotBlank, @Size min=10 max=500)

**BookingReviewResponse:** 匹配前端 HistoryFeedback.vue
- bookingNumber, roomType, checkInDate, checkOutDate
- reviewed (Boolean) - 是否已评价
- rating, comment - 评价内容

**异常类:**
- ProfileNotFoundException - 用户资料未找到
- ReviewAlreadyExistsException - 订单已评价

**Commit:** `31b5081`

### Task 3: 更新数据库schema

**users表扩展:**
```sql
phone VARCHAR
address TEXT
nationality VARCHAR
preferences_enabled INTEGER DEFAULT 1 NOT NULL
```

**reviews表创建:**
- booking_id, user_id 外键关联
- rating CHECK约束 (1-5)
- UNIQUE(booking_id) - 每个订单只能评价一次
- ON DELETE CASCADE - 级联删除
- 索引: idx_reviews_user, idx_reviews_booking

**Commit:** `c592aca`

## Technical Decisions

1. **User.name字段保持不变**: 前端通过firstName/lastName展示，后端存储为完整name，由Service层处理拆分逻辑

2. **Review与Booking/User使用LAZY加载**: 评价查询时通常不需要加载完整订单和用户信息，按需加载提高性能

3. **preferencesEnabled使用Boolean而非枚举**: 简单开关状态，Boolean类型足够

4. **reviews表添加UNIQUE约束**: 数据库层面确保每个订单只能评价一次，避免脏数据

## Verification Results

- [x] 编译项目成功: mvn clean compile
- [x] User实体包含新增字段: phone, address, nationality, preferencesEnabled
- [x] Review实体与Booking和User正确关联
- [x] ReviewRepository包含必要查询方法
- [x] 所有DTO类编译无错误
- [x] ProfileResponse结构匹配前端Profile.vue的profileForm
- [x] BookingReviewResponse结构匹配前端HistoryFeedback.vue的history项
- [x] schema.sql包含reviews表定义和users表扩展

## Requirements Traceability

- [PROFILE-01] 用户个人资料管理 - User实体扩展完成
- [PROFILE-02] 个人资料编辑 - UpdateProfileRequest创建完成
- [PROFILE-03] 订单评价 - Review实体和Repository完成
- [PROFILE-04] 评价查询 - BookingReviewResponse创建完成

## Next Steps

Phase 06-02: 实现ProfileService和ReviewService，包含:
- 个人资料查询和更新业务逻辑
- 评价创建和查询业务逻辑
- 姓名拆分逻辑(name -> firstName/lastName)
- 订单可评价状态校验

## Self-Check: PASSED

所有创建的文件存在:
- ProfileResponse.java
- UpdateProfileRequest.java
- ReviewRequest.java
- ReviewResponse.java
- BookingReviewResponse.java
- Review.java
- ReviewRepository.java
- ProfileNotFoundException.java
- ReviewAlreadyExistsException.java

所有commits存在:
- 85f607e - feat(06-01): 扩展User实体和创建Review实体
- 31b5081 - feat(06-01): 创建DTO类和异常类
- c592aca - feat(06-01): 更新数据库schema
