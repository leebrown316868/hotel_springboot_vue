---
phase: 3
plan: 01
subsystem: Guest Management
tags: [entity, repository, dto, jpa]
dependency_graph:
  requires: []
  provides: [03-02]
  affects: []
tech_stack:
  added: []
  patterns:
    - JPA Entity with Builder pattern
    - Repository inheritance (JpaRepository + JpaSpecificationExecutor)
    - DTO pattern for API layer
key_files:
  created:
    - src/main/java/com/hotel/entity/GuestStatus.java
    - src/main/java/com/hotel/entity/Guest.java
    - src/main/java/com/hotel/repository/GuestRepository.java
    - src/main/java/com/hotel/dto/GuestResponse.java
    - src/main/java/com/hotel/dto/GuestRequest.java
    - src/main/java/com/hotel/dto/GuestListResponse.java
    - src/main/java/com/hotel/dto/BookingSummary.java
  modified: []
decisions: []
metrics:
  duration_seconds: 420
  completed_date: "2026-03-14"
---

# Phase 3 Plan 01: Guest实体和Repository层实现 Summary

创建完整的客户管理数据访问层基础，包括实体、枚举、Repository和DTO类，为后续Service层和Controller层提供支撑。

## 任务完成情况

| 任务 | 名称 | 状态 | 提交 |
|------|------|------|------|
| T1 | 创建GuestStatus枚举类 | 完成 | 38d9e10 |
| T2 | 创建Guest实体类 | 完成 | 38d9e10 |
| T3 | 创建GuestRepository接口 | 完成 | 38d9e10 |
| T4 | 创建Guest相关DTO类 | 完成 | 38d9e10 |
| T5 | 验证SQLiteDialect配置 | 完成 | 38d9e10 |

## 创建的文件

### 实体类

**GuestStatus.java** - 客户状态枚举
- VIP: VIP客户（高价值客户）
- ACTIVE: 活跃客户
- INACTIVE: 不活跃客户
- 使用@Getter和@AllArgsConstructor注解
- 包含code和displayName字段

**Guest.java** - 客户实体
- 字段：id, name, email(unique), phone, country, status(枚举), totalBookings, lastStay, createdAt, updatedAt
- 使用@Enumerated(EnumType.STRING)存储状态
- @PrePersist和@PreUpdate自动维护时间戳
- status默认值为ACTIVE
- totalBookings默认值为0
- Builder模式支持

### Repository接口

**GuestRepository.java** - 客户数据访问层
- 继承JpaRepository<Guest, Long>
- 继承JpaSpecificationExecutor<Guest>（支持复杂查询）
- 自定义查询方法：
  - 按姓名模糊查询（忽略大小写）
  - 按邮箱模糊/精确查询
  - 按状态查询
  - 按国家查询
  - 检查邮箱是否存在
  - 查询预订次数大于指定值的客户

### DTO类

**GuestResponse.java** - 客户响应DTO
- 包含所有客户信息字段
- status使用displayName便于前端显示

**GuestRequest.java** - 客户创建/更新请求DTO
- 包含验证注解（@NotBlank, @Email）
- 支持创建和更新操作

**GuestListResponse.java** - 客户列表响应DTO
- 包含分页信息（total, page, size, totalPages）
- 用于列表API响应

**BookingSummary.java** - 预订摘要DTO
- 简化预订信息，避免循环引用
- 用于客户预订历史展示
- 包含房间号、入住日期、离店日期、状态、总价等

## 技术实现细节

### JPA注解使用
- @Entity和@Table指定实体和表名
- @Id和@GeneratedValue配置主键自增
- @Column配置字段约束（unique, nullable, length）
- @Enumerated(EnumType.STRING)存储枚举为字符串
- @PrePersist/@PreUpdate自动维护时间戳

### 设计模式
- **Builder模式**: 使用Lombok的@Builder注解，支持链式构建
- **DTO模式**: 分离实体和API层数据传输对象
- **Repository模式**: Spring Data JPA标准数据访问层

### 数据验证
- GuestRequest使用javax.validation注解
- @NotBlank确保必填字段
- @Email验证邮箱格式
- @Valid用于嵌套验证

## 数据库支持

SQLiteDialect已正确支持：
- LocalDate → DATE类型
- LocalDateTime → TIMESTAMP类型
- 无需额外配置

## 与现有代码的一致性

所有新类遵循项目现有模式：
- 枚举类参考RoomStatus的设计
- 实体类参考Room和User的设计
- Repository参考RoomRepository的设计
- DTO参考RoomResponse/RoomRequest的设计

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] 修复GuestRequest缺少LocalDate导入**
- **Found during:** Task T4编译验证
- **Issue:** GuestRequest.java使用了LocalDate但未导入java.time.LocalDate
- **Fix:** 添加`import java.time.LocalDate;`导入语句
- **Files modified:** src/main/java/com/hotel/dto/GuestRequest.java
- **Commit:** 38d9e10

## 代码质量

- 所有类编译通过，无语法错误
- 遵循项目命名规范和代码风格
- 使用Lombok减少样板代码
- 完整的JPA注解支持ORM映射
- Repository方法命名符合Spring Data JPA规范

## 下一阶段准备

已完成的数据访问层为Plan 03-02提供：
- Guest实体用于业务逻辑处理
- GuestRepository提供数据访问能力
- DTO类用于API层数据传输
- JpaSpecificationExecutor支持复杂查询

**已完成，无需人工干预。**
