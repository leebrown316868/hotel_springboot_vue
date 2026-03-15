# 酒店民宿管理系统 - 项目路线图

**Created:** 2026-03-14
**Phases:** 6
**Granularity:** Standard

## Progress Summary

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. 项目基础与认证 | 3/3 | Complete   | 2026-03-14 |
| 2. 房间管理 | 3/3 | Complete   | 2026-03-14 |
| 3. 客户管理 | 3/3 | Complete   | 2026-03-14 |
| 4. 预订管理 | 3/4 | In Progress | 2026-03-15 |
| 5. 数据统计 | 0/3 | Not started | - |
| 6. 个人中心 | 0/2 | Not started | - |

## Phases

- [x] **Phase 1: 项目基础与认证** - SpringBoot项目搭建、数据库设计、用户认证系统 (completed 2026-03-14)
- [x] **Phase 2: 房间管理** - 房间CRUD、状态管理、筛选查询 (completed 2026-03-14)
- [x] **Phase 3: 客户管理** - 客户信息管理、搜索、预订历史 (completed 2026-03-14)
- [ ] **Phase 4: 预订管理** - 预订流程、订单状态流转、支付模拟
- [ ] **Phase 5: 数据统计** - 仪表板、图表、实时数据统计
- [ ] **Phase 6: 个人中心** - 个人资料、历史订单、评价反馈

## Phase Details

### Phase 1: 项目基础与认证

**Goal**: 系统启动，用户可以登录并根据角色访问对应界面

**Depends on**: Nothing

**Requirements**: AUTH-01, AUTH-02, AUTH-03, AUTH-04, AUTH-05, AUTH-06, INIT-01

**Success Criteria** (what must be TRUE):
1. SpringBoot项目可以正常启动，数据库连接成功
2. 用户可以使用邮箱和密码登录系统，登录状态在刷新后保持
3. 客户可以自助注册账户
4. 管理员(admin/admin)和前台(staff/staff)可以登录并访问对应界面
5. 用户可以从任意页面登出

**Plans**: 3

- [x] 01-01-PLAN.md — SpringBoot项目搭建与配置（Maven、application.yml、包结构） ✅ 2026-03-14
- [x] 01-02-PLAN.md — JWT认证系统实现（User实体、AuthService、AuthController） ✅ 2026-03-14
- [x] 01-03-PLAN.md — 预设账户初始化与前端集成（DataInitializer、API工具、Login.vue） ✅ 2026-03-14

### Phase 2: 房间管理

**Goal**: 管理员可以完整管理酒店房间信息

**Depends on**: Phase 1

**Requirements**: ROOM-01, ROOM-02, ROOM-03, ROOM-04, ROOM-05, ROOM-06, ROOM-07, INIT-02

**Success Criteria** (what must be TRUE):
1. 管理员可以查看分页的房间列表，支持按房间号、楼层、状态筛选
2. 管理员可以添加新房间并设置房间号、楼层、类型、价格
3. 管理员可以编辑现有房间信息
4. 管理员可以查看房间详情
5. 系统支持5种房型(单人间、双人间、套房、行政套房、总统套房)和4种状态(空闲、已入住、清洁中、维修中)

**Plans**: 3

- [x] 02-01-PLAN.md — Room实体和Repository层实现 ✅ 2026-03-14
- [x] 02-02-PLAN.md — RoomService和RoomController实现 ✅ 2026-03-14
- [x] 02-03-PLAN.md — 房间管理前端集成与测试 ✅ 2026-03-14

### Phase 3: 客户管理

**Goal**: 管理员可以管理客户信息，为预订管理提供客户数据基础

**Depends on**: Phase 1

**Requirements**: GUEST-01, GUEST-02, GUEST-03, GUEST-04, GUEST-05, GUEST-06, GUEST-07, INIT-03

**Success Criteria** (what must be TRUE):
1. 管理员可以查看客户列表，支持按姓名或邮箱搜索
2. 管理员可以添加新客户
3. 管理员可以查看和编辑客户详情
4. 管理员可以查看客户的预订历史
5. 管理员可以删除客户

**Plans**: TBD

### Phase 4: 预订管理

**Goal**: 客户可以创建预订，前台可以管理订单状态流转

**Depends on**: Phase 1, Phase 2, Phase 3

**Requirements**: BOOK-01, BOOK-02, BOOK-03, BOOK-04, BOOK-05, BOOK-06, BOOK-07, BOOK-08, BOOK-09, BOOK-10, BOOK-11, BOOK-12, PAY-01, PAY-02

**Success Criteria** (what must be TRUE):
1. 客户可以按日期范围、人数、房型搜索可用房间
2. 客户可以通过5步向导完成预订(搜索→选择客房→客人信息→支付→确认)
3. 客户可以查看自己的订单列表并取消待确认的预订
4. 前台人员可以创建新预订
5. 前台人员可以办理入住(已确认→已入住)和退房(已入住→已退房)
6. 前台人员可以查看所有订单，支持按订单号、客人姓名、状态筛选
7. 预订流程包含模拟支付步骤，支付成功后订单状态变更为已确认

**Plans**: 4

- [x] 04-01-PLAN.md — 预订实体和Repository层实现 ✅ 2026-03-15
- [x] 04-02-PLAN.md — 预订Service层实现 ✅ 2026-03-15
- [x] 04-03-PLAN.md — 预订Controller层实现 ✅ 2026-03-15
- [x] 04-04-PLAN.md — 前端集成和测试 ✅ 2026-03-15

### Phase 5: 数据统计

**Goal**: 仪表板展示酒店运营数据和图表

**Depends on**: Phase 4

**Requirements**: STAT-01, STAT-02, STAT-03, STAT-04, STAT-05, STAT-06, STAT-07, STAT-08

**Success Criteria** (what must be TRUE):
1. 仪表板显示总客房数、当前空闲客房数和入住率
2. 仪表板显示今日预计入住数和待退房数
3. 仪表板显示今日预估收入
4. 仪表板显示客房状态分布饼图
5. 仪表板显示预订趋势折线图(最近7天)
6. 仪表板显示最近预订列表

**Plans**: TBD

### Phase 6: 个人中心

**Goal**: 客户可以管理个人信息并对已完成订单进行评价

**Depends on**: Phase 4

**Requirements**: PROFILE-01, PROFILE-02, PROFILE-03, PROFILE-04

**Success Criteria** (what must be TRUE):
1. 客户可以查看个人资料
2. 客户可以编辑个人资料
3. 客户可以查看历史订单
4. 客户可以对已完成订单进行评价反馈

**Plans**: TBD

## Requirements Coverage

**Total v1 requirements:** 48
**Mapped to phases:** 48
**Coverage:** 100%

### Phase Distribution

| Phase | Requirements | Count |
|-------|-------------|-------|
| 1 | AUTH-01~06, INIT-01 | 7 |
| 2 | ROOM-01~07, INIT-02 | 8 |
| 3 | GUEST-01~07, INIT-03 | 8 |
| 4 | BOOK-01~12, PAY-01~02 | 14 |
| 5 | STAT-01~08 | 8 |
| 6 | PROFILE-01~04 | 4 |

### Dependency Graph

```
Phase 1 (基础+认证)
    ├──→ Phase 2 (房间管理)
    │       └──→ Phase 4 (预订管理) ──→ Phase 5 (数据统计)
    │
    ├──→ Phase 3 (客户管理)
    │       └──→ Phase 4 (预订管理) ──→ Phase 6 (个人中心)
    │
    └──→ Phase 4 (预订管理)
```

## Notes

- 前端已完成，后端API需与前端对接
- SQLite数据库需要初始化schema和示例数据
- 认证系统支持3种角色：管理员、前台、客户
- 预订状态流转：待确认→已确认→已入住→已退房

---
*Roadmap created: 2026-03-14*
*Last updated: 2026-03-14 - Phase 1 completed with gap fixes*
