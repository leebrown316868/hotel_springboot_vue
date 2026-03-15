# Requirements: 酒店民宿管理系统

**Defined:** 2026-03-14
**Core Value:** 能完成答辩的完整酒店管理系统

## v1 Requirements

根据前端页面结构和业务需求，v1需要实现以下功能模块：

### 认证管理

- [x] **AUTH-01**: 用户可以使用邮箱和密码登录系统
- [x] **AUTH-02**: 客户可以自助注册账户
- [x] **AUTH-03**: 系统预设管理员账户（admin/admin）
- [x] **AUTH-04**: 系统预设前台账户（staff/staff）
- [x] **AUTH-05**: 登录状态在浏览器刷新后保持
- [x] **AUTH-06**: 用户可以登出

### 房间管理

- [ ] **ROOM-01**: 管理员可以查看房间列表（支持分页）
- [ ] **ROOM-02**: 管理员可以按房间号、楼层、状态筛选房间
- [ ] **ROOM-03**: 管理员可以添加新房间
- [ ] **ROOM-04**: 管理员可以编辑房间信息（房间号、楼层、类型、价格）
- [ ] **ROOM-05**: 管理员可以查看房间详情
- [ ] **ROOM-06**: 系统支持房间状态：空闲、已入住、清洁中、维修中
- [ ] **ROOM-07**: 系统支持房型：单人间、双人间、套房、行政套房、总统套房

### 客户管理

- [ ] **GUEST-01**: 管理员可以查看客户列表
- [ ] **GUEST-02**: 管理员可以按姓名或邮箱搜索客户
- [ ] **GUEST-03**: 管理员可以添加新客户
- [ ] **GUEST-04**: 管理员可以查看客户资料
- [ ] **GUEST-05**: 管理员可以编辑客户详情
- [ ] **GUEST-06**: 管理员可以查看客户预订历史
- [ ] **GUEST-07**: 管理员可以删除客户

### 预订管理

- [ ] **BOOK-01**: 客户可以浏览可用房间
- [ ] **BOOK-02**: 客户可以按日期范围和人数搜索房间
- [ ] **BOOK-03**: 客户可以按房型筛选
- [ ] **BOOK-04**: 客户可以通过5步向导创建预订（搜索→选择客房→客人信息→支付→确认）
- [ ] **BOOK-05**: 前台人员可以创建新预订
- [ ] **BOOK-06**: 客户可以查看自己的订单列表
- [ ] **BOOK-07**: 客户可以取消待确认的预订
- [ ] **BOOK-08**: 前台人员可以办理入住（订单状态：已确认→已入住）
- [ ] **BOOK-09**: 前台人员可以办理退房（订单状态：已入住→已退房）
- [ ] **BOOK-10**: 前台人员可以查看所有订单列表
- [ ] **BOOK-11**: 前台人员可以按订单号或客人姓名搜索
- [ ] **BOOK-12**: 前台人员可以按订单状态筛选

### 支付模拟

- [ ] **PAY-01**: 预订流程包含模拟支付步骤
- [ ] **PAY-02**: 支付成功后订单状态变更为已确认

### 数据统计

- [x] **STAT-01**: 仪表板显示总客房数
- [x] **STAT-02**: 仪表板显示当前空闲客房数和入住率
- [x] **STAT-03**: 仪表板显示今日预计入住数
- [x] **STAT-04**: 仪表板显示今日待退房数
- [x] **STAT-05**: 仪表板显示今日预估收入
- [ ] **STAT-06**: 仪表板显示客房状态分布饼图
- [ ] **STAT-07**: 仪表板显示预订趋势折线图（最近7天）
- [x] **STAT-08**: 仪表板显示最近预订列表

### 个人中心

- [x] **PROFILE-01**: 客户可以查看个人资料
- [x] **PROFILE-02**: 客户可以编辑个人资料
- [x] **PROFILE-03**: 客户可以查看历史订单
- [x] **PROFILE-04**: 客户可以对已完成订单进行评价反馈

### 系统初始化

- [x] **INIT-01**: 系统初始化时创建预设用户账户
- [ ] **INIT-02**: 系统初始化时创建示例房间数据（各种房型）
- [ ] **INIT-03**: 系统初始化时创建示例客户数据

## v2 Requirements

暂无

## Out of Scope

| Feature | Reason |
|---------|--------|
| 真实支付接口 | 毕设不需要，使用模拟支付 |
| 第三方平台对接 | 毕设不需要，使用模拟数据 |
| 移动端APP | 仅Web端，降低复杂度 |
| 多酒店管理 | 单酒店系统，满足毕设需求 |
| 复杂权限管理 | 基于角色的简单权限即可 |
| 实时通信 | 不需要WebSocket等实时功能 |
| 消息通知 | 不需要邮件/短信通知 |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| AUTH-01 | Phase 1 | Complete |
| AUTH-02 | Phase 1 | Complete |
| AUTH-03 | Phase 1 | Complete |
| AUTH-04 | Phase 1 | Complete |
| AUTH-05 | Phase 1 | Complete |
| AUTH-06 | Phase 1 | Complete |
| ROOM-01 | Phase 2 | Pending |
| ROOM-02 | Phase 2 | Pending |
| ROOM-03 | Phase 2 | Pending |
| ROOM-04 | Phase 2 | Pending |
| ROOM-05 | Phase 2 | Pending |
| ROOM-06 | Phase 2 | Pending |
| ROOM-07 | Phase 2 | Pending |
| GUEST-01 | Phase 3 | Pending |
| GUEST-02 | Phase 3 | Pending |
| GUEST-03 | Phase 3 | Pending |
| GUEST-04 | Phase 3 | Pending |
| GUEST-05 | Phase 3 | Pending |
| GUEST-06 | Phase 3 | Pending |
| GUEST-07 | Phase 3 | Pending |
| BOOK-01 | Phase 4 | Complete |
| BOOK-02 | Phase 4 | Complete |
| BOOK-03 | Phase 4 | Complete |
| BOOK-04 | Phase 4 | Complete |
| BOOK-05 | Phase 4 | Complete |
| BOOK-06 | Phase 4 | Complete |
| BOOK-07 | Phase 4 | Complete |
| BOOK-08 | Phase 4 | Complete |
| BOOK-09 | Phase 4 | Complete |
| BOOK-10 | Phase 4 | Complete |
| BOOK-11 | Phase 4 | Complete |
| BOOK-12 | Phase 4 | Complete |
| PAY-01 | Phase 4 | Complete |
| PAY-02 | Phase 4 | Complete |
| STAT-01 | Phase 5 | Complete |
| STAT-02 | Phase 5 | Complete |
| STAT-03 | Phase 5 | Complete |
| STAT-04 | Phase 5 | Complete |
| STAT-05 | Phase 5 | Complete |
| STAT-06 | Phase 5 | Pending |
| STAT-07 | Phase 5 | Pending |
| STAT-08 | Phase 5 | Complete |
| PROFILE-01 | Phase 6 | Complete |
| PROFILE-02 | Phase 6 | Complete |
| PROFILE-03 | Phase 6 | Complete |
| PROFILE-04 | Phase 6 | Complete |
| INIT-01 | Phase 1 | In Progress |
| INIT-02 | Phase 2 | Pending |
| INIT-03 | Phase 3 | Pending |

**Coverage:**
- v1 requirements: 48 total
- Mapped to phases: 48
- Unmapped: 0

---
*Requirements defined: 2026-03-14*
*Last updated: 2026-03-14 after roadmap creation*
