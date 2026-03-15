---
phase: "04"
plan: "01"
title: "预订实体和Repository层"
subsystem: "booking-management"
tags: ["booking", "repository", "entity", "dto"]
wave: 1
dependency_graph:
  requires: ["01-01", "01-02", "02-01", "03-01"]
  provides: ["04-02", "04-03"]
  affects: []
tech_stack:
  added: []
  patterns: ["Repository Pattern", "DTO Pattern", "JPA Entities"]
key_files:
  created: 
    - "src/main/java/com/hotel/entity/BookingStatus.java"
    - "src/main/java/com/hotel/entity/PaymentStatus.java"
    - "src/main/java/com/hotel/entity/Booking.java"
    - "src/main/java/com/hotel/repository/BookingRepository.java"
    - "src/main/java/com/hotel/dto/BookingRequest.java"
    - "src/main/java/com/hotel/dto/BookingResponse.java"
    - "src/main/java/com/hotel/dto/RoomSearchRequest.java"
  modified:
    - "src/main/resources/schema.sql"
decisions: []
metrics:
  duration_seconds: 227
  completed_date: "2026-03-15T07:16:55Z"
  tasks_completed: 5
  files_created: 7
  files_modified: 1
---

# Phase 4 Plan 01: 预订实体和Repository层 总结

## One-Liner Summary
实现预订管理系统的数据访问层基础，包含Booking实体、状态枚举、Repository接口和DTO类，支持完整的预订生命周期管理。

## Tasks Completed

### Task 1: 创建预订状态和支付状态枚举
- 创建 `BookingStatus` 枚举：PENDING, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED
- 创建 `PaymentStatus` 枚举：UNPAID, PAID, REFUNDED
- 使用 `@Enumerated(EnumType.STRING)` 确保枚举值存储为字符串

**Commit:** `4a9b47d` - feat(04-01): 创建预订状态和支付状态枚举

### Task 2: 创建Booking实体
- 实现 Booking 实体包含所有必需字段
- 与 Guest 和 Room 建立多对一关联关系
- 添加 `@UniqueConstraint` 确保订单号唯一
- 使用 `@PrePersist` 和 `@PreUpdate` 自动管理时间戳和冗余字段
- 冗余存储 `guestName` 和 `roomType` 优化查询性能

**Commit:** `4d4e678` - feat(04-01): 创建Booking实体

### Task 3: 创建BookingRepository
- 继承 `JpaRepository` 提供基础 CRUD 操作
- 实现按订单号、客人ID、状态查询预订
- 实现日期冲突检查方法 `existsByRoomAndCheckInDateBeforeAndCheckOutDateAfterAndStatusNotIn`
- 实现关键字搜索功能（订单号/客人姓名）
- 实现可用房间查询（支持日期、人数、房型筛选）
- 所有查询方法支持分页

**Commit:** `a71c8f4` - feat(04-01): 创建BookingRepository

### Task 4: 更新数据库schema
- 创建 `bookings` 表包含所有预订相关字段
- 添加外键约束关联 `guests` 和 `rooms` 表
- 添加 CHECK 约束确保数据有效性
- 创建索引优化查询性能（订单号、客人、房间、状态、日期）
- 设置默认状态值（PENDING/UNPAID）

**Commit:** `a4db5a2` - feat(04-01): 更新数据库schema添加bookings表

### Task 5: 创建DTO类
- 创建 `BookingRequest` DTO 包含预订请求字段及验证注解
- 创建 `BookingResponse` DTO 包含预订响应字段（订单号、客人、房间、状态等）
- 创建 `RoomSearchRequest` DTO 用于房间可用性搜索
- 所有 DTO 添加 `@NotNull`、`@Min` 等验证注解确保数据有效性

**Commit:** `6f59c5c` - feat(04-01): 创建预订相关DTO类

## Deviations from Plan

### Auto-fixed Issues

**无** - 计划完全按照预期执行，无需修复任何问题。

### Out of Scope Issues

**编译警告：** 项目中存在 `RoomServiceImpl` 的编译错误，但这些错误与本次计划的代码无关，是之前遗留的问题。根据偏差规则中的"范围边界"，这不属于当前任务的直接因果关系，已记录但不处理。

## Verification Criteria

- [x] BookingStatus 和 PaymentStatus 枚举创建成功
- [x] Booking 实体创建成功，包含所有字段和关联
- [x] BookingRepository 创建成功，查询方法可用
- [x] 数据库 schema 更新成功，bookings 表创建成功
- [x] DTO 类创建成功，包含验证注解
- [x] 新代码编译无错误

## Must Haves (Goal-Backward Verification)

- [x] Booking 实体支持与 Guest 和 Room 的关联
- [x] 订单编号唯一约束已添加
- [x] 数据库表已添加索引以支持高效查询
- [x] Repository 提供可用房间查询方法
- [x] DTO 包含数据验证注解

## Technical Details

### Key Design Decisions
1. **冗余字段设计**：在 Booking 表中冗余存储 `guestName` 和 `roomType`，避免每次查询都需要 JOIN 操作
2. **日期冲突检测**：Repository 方法支持排除已取消和已退房的记录进行冲突检测
3. **可用房间查询**：支持多维度筛选（日期范围、入住人数、房型）
4. **字符串枚举**：使用 `EnumType.STRING` 存储枚举值，提高数据库可读性

### Performance Considerations
- 在 `booking_number`、`guest_id`、`room_id`、`status` 上创建索引
- 复合索引 `(check_in_date, check_out_date)` 优化日期范围查询
- 冗余字段减少 JOIN 操作

## Next Steps

下一步计划（04-02）将实现：
- BookingService 业务逻辑层
- 订单编号生成器（BK-YYYYMMDD-序号格式）
- 价格计算逻辑
- 日期冲突验证
- 预订状态流转管理

---
*Plan completed: 2026-03-15*
*Duration: 3分47秒*
*All tasks completed successfully*
