# Rooms Resource 页面功能设计

**日期**: 2026-03-16
**状态**: 设计中
**类型**: 功能增强

## 1. 概述

为 `/rooms-resource` 页面添加实际功能，使其成为一个专注于资源管理视角的客房资源管理页面。

## 2. 功能定位

与 `/rooms` 页面（完整 CRUD 操作）不同，`/rooms-resource` 页面专注于：
- **房型配置管理**：管理现有房型的配置信息
- **客房资源查看**：只读查看房间状态和分布

## 3. 用户角色

- 前台员工：查看客房资源，了解房型配置
- 管理员：编辑房型配置

## 4. 功能需求

### 4.1 房型配置标签

**功能列表：**
- 显示所有房型配置（名称、容量、基础价格）
- 编辑房型配置（名称、容量、价格）
- 显示房型统计信息（该房型下的房间总数、空闲数）
- 不可新增或删除房型代码（保持固定的 5 种）

**房型列表：**
| 代码 | 名称 | 容量 | 基础价格 | 房间数 | 空闲数 | 操作 |
|------|------|------|----------|--------|--------|------|
| SINGLE | 单人间 | 1 | ¥150 | 20 | 15 | 编辑 |
| DOUBLE | 双人间 | 2 | ¥220 | 30 | 20 | 编辑 |
| SUITE | 套房 | 2 | ¥350 | 10 | 5 | 编辑 |
| EXECUTIVE_SUITE | 行政套房 | 3 | ¥500 | 5 | 2 | 编辑 |
| PRESIDENTIAL_SUITE | 总统套房 | 4 | ¥850 | 2 | 1 | 编辑 |

### 4.2 客房列表标签

**功能列表：**
- 搜索房间号
- 按楼层、房型、状态筛选
- 查看房间详情
- 状态显示（标签形式，不可编辑）

**筛选选项：**
- 楼层：1-5 楼
- 房型：所有可用房型
- 状态：空闲、已入住、清洁中、维护中

## 5. 技术设计

### 5.1 数据存储

使用 `SystemSettings` 表存储房型配置：

```json
Key: "room_types_config"
Value: {
  "SINGLE": {
    "name": "单人间",
    "capacity": 1,
    "basePrice": 150
  },
  "DOUBLE": {
    "name": "双人间",
    "capacity": 2,
    "basePrice": 220
  },
  "SUITE": {
    "name": "套房",
    "capacity": 2,
    "basePrice": 350
  },
  "EXECUTIVE_SUITE": {
    "name": "行政套房",
    "capacity": 3,
    "basePrice": 500
  },
  "PRESIDENTIAL_SUITE": {
    "name": "总统套房",
    "capacity": 4,
    "basePrice": 850
  }
}
```

**验证规则：**
- `capacity`: 1-10 之间（最小1人，最大10人）
- `basePrice`: 0-10000 之间（最小0元，最大10000元）
- `name`: 1-50 字符
- 并发处理：使用 @Transactional 确保原子性更新

**数据迁移策略：**
- 首次启动时检查 SystemSettings 中是否存在 `room_types_config`
- 如果不存在，使用默认配置初始化
- 位置：DataInitializer 中添加初始化逻辑

### 5.2 后端 DTO 定义

```java
// RoomTypeConfig.java - 单个房型配置
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomTypeConfig {
    @NotBlank(message = "房型名称不能为空")
    @Size(min = 1, max = 50, message = "房型名称长度必须在1-50之间")
    private String name;

    @Min(value = 1, message = "容纳人数至少为1人")
    @Max(value = 10, message = "容纳人数最多为10人")
    private Integer capacity;

    @Min(value = 0, message = "基础价格不能为负数")
    @DecimalMax(value = "10000.00", message = "基础价格不能超过10000元")
    private BigDecimal basePrice;
}

// RoomTypeStats.java - 房型统计信息
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomTypeStats {
    private String code;
    private Integer roomCount;
    private Integer availableCount;
    private Integer occupiedCount;
    private Integer cleaningCount;
    private Integer maintenanceCount;
}
```

### 5.3 后端 API

**完整 URL 路径：**
- `GET /api/settings/room-types` - 获取房型配置
- `PUT /api/settings/room-types` - 更新房型配置
- `GET /api/settings/room-types/{code}/stats` - 获取房型统计

#### SettingsController 新增接口

```java
@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;
    private final RoomService roomService;

    /**
     * 获取房型配置
     */
    @GetMapping("/room-types")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Map<String, RoomTypeConfig>>> getRoomTypesConfig() {
        Map<String, RoomTypeConfig> config = settingsService.getRoomTypesConfig();
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    /**
     * 更新房型配置
     */
    @PutMapping("/room-types")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, RoomTypeConfig>>> updateRoomTypesConfig(
        @Valid @RequestBody Map<String, RoomTypeConfig> config
    ) {
        Map<String, RoomTypeConfig> updated = settingsService.updateRoomTypesConfig(config);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * 获取房型统计
     */
    @GetMapping("/room-types/{code}/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<RoomTypeStats>> getRoomTypeStats(
        @PathVariable String code
    ) {
        RoomTypeStats stats = roomService.getRoomTypeStats(code);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
```

#### SettingsService 新增方法

```java
public interface SettingsService {
    /**
     * 获取房型配置
     */
    Map<String, RoomTypeConfig> getRoomTypesConfig();

    /**
     * 更新房型配置
     * @param config 完整的房型配置（5种房型）
     * @return 更新后的配置
     */
    @Transactional
    Map<String, RoomTypeConfig> updateRoomTypesConfig(Map<String, RoomTypeConfig> config);
}
```

#### RoomService 新增方法

```java
public interface RoomService {
    /**
     * 获取指定房型的统计信息
     */
    RoomTypeStats getRoomTypeStats(String roomType);
}
```

**服务边界：**
- **SettingsService**: 负责系统配置的读取和存储（配置层）
- **RoomService**: 负责房间领域逻辑和统计计算（业务层）
- **验证**: 在 DTO 层使用 @Valid 注解，Controller 负责触发验证

### 5.4 前端 API

```typescript
// frontend/src/api/settings.ts

import api from '@/utils/api'
import type { ApiResponse } from '@/types/settings'

export interface RoomTypeConfig {
  name: string
  capacity: number
  basePrice: number
}

export interface RoomTypeWithStats extends RoomTypeConfig {
  code: string
  roomCount: number
  availableCount: number
}

/**
 * 获取房型配置
 * GET /api/settings/room-types
 */
export const getRoomTypesConfig = async (): Promise<ApiResponse<Record<string, RoomTypeConfig>>> => {
  const response = await api.get<ApiResponse<Record<string, RoomTypeConfig>>>(
    '/api/settings/room-types'
  )
  return response.data
}

/**
 * 更新房型配置
 * PUT /api/settings/room-types
 */
export const updateRoomTypesConfig = async (
  config: Record<string, RoomTypeConfig>
): Promise<ApiResponse<Record<string, RoomTypeConfig>>> => {
  const response = await api.put<ApiResponse<Record<string, RoomTypeConfig>>>(
    '/api/settings/room-types',
    config
  )
  return response.data
}

/**
 * 获取房型统计
 * GET /api/settings/room-types/{code}/stats
 */
export const getRoomTypeStats = async (
  code: string
): Promise<ApiResponse<RoomTypeStats>> => {
  const response = await api.get<ApiResponse<RoomTypeStats>>(
    `/api/settings/room-types/${code}/stats`
  )
  return response.data
}
```

### 5.5 数据类型定义

```typescript
// frontend/src/types/settings.ts

export interface RoomTypeConfig {
  name: string
  capacity: number
  basePrice: number
}

export interface RoomTypeStats {
  code: string
  roomCount: number
  availableCount: number
  occupiedCount: number
  cleaningCount: number
  maintenanceCount: number
}
```

## 6. UI 设计

### 6.1 房型配置标签

```
┌─────────────────────────────────────────────────────────┐
│  房型配置                                                │
├─────────────────────────────────────────────────────────┤
│  ┌─────────┬────────┬────────┬──────────┬────────┬────┐ │
│  │房型代码 │ 名称   │ 容量   │ 基础价格 │ 房间数 │操作│ │
│  ├─────────┼────────┼────────┼──────────┼────────┼────┤ │
│  │ SINGLE  │ 单人间 │   1    │   ¥150   │  15/20 │编辑│ │
│  │ DOUBLE  │ 双人间 │   2    │   ¥220   │  20/30 │编辑│ │
│  │ SUITE   │ 套房   │   2    │   ¥350   │   5/10 │编辑│ │
│  │ EXEC... │ 行政套房│   3   │   ¥500   │   2/5  │编辑│ │
│  │ PRES... │ 总统套房│   4   │   ¥850   │   1/2  │编辑│ │
│  └─────────┴────────┴────────┴──────────┴────────┴────┘ │
│  备注：显示格式为 "空闲数/总数"；房型代码列显示完整代码   │
└─────────────────────────────────────────────────────────┘
```

**注意：** 不显示"新增资源"按钮，因为房型代码固定为5种，不可新增或删除

### 6.2 编辑房型对话框

```
┌──────────────────────────────┐
│  编辑房型配置               │
├──────────────────────────────┤
│  房型代码:  [SINGLE      ]  │
│  (只读)                      │
│                              │
│  房型名称:  [单人间      ]  │
│                              │
│  容纳人数:  [   1   ] 人     │
│                              │
│  基础价格:  [  150  ] 元     │
│                              │
│         [取消]  [确定]       │
└──────────────────────────────┘
```

### 6.3 客房列表标签

```
┌─────────────────────────────────────────────────────────┐
│  客房列表                                                │
├─────────────────────────────────────────────────────────┤
│  [🔍 搜索房间号...] [楼层▼] [房型▼] [状态▼]             │
├─────────────────────────────────────────────────────────┤
│  ┌──────┬──────┬────┬────┬──────┬────────┬────────┐   │
│  │房间号│ 房型  │楼层│价格│状态  │最后更新│ 操作  │   │
│  ├──────┼──────┼────┼────┼──────┼────────┼────────┤   │
│  │ 101  │ 单人间│ 1  │150 │[空闲] │ 2小时前 │ 查看  │   │
│  │ 102  │ 单人间│ 1  │150 │[已入] │ 1天前  │ 查看  │   │
│  │ 201  │ 双人间│ 2  │220 │[清洁] │ 3小时前│ 查看  │   │
│  └──────┴──────┴────┴────┴──────┴────────┴────────┘   │
└─────────────────────────────────────────────────────────┘
```

## 7. 数据流

### 7.1 页面加载流程

```
1. 页面挂载
   ↓
2. 并行请求：
   - getRoomTypesConfig() - 获取房型配置
   - getRoomTypeStats() for each type - 获取各房型统计（5个并行请求）
   ↓
3. 合并数据渲染房型配置表格
   ↓
4. 异步加载客房列表（使用现有 getRooms API）
```

**性能考虑：**
- 房型统计请求最多5个（房型数量固定），可接受
- 如果后续房型数量增加，考虑添加批量统计接口

### 7.2 编辑房型流程

```
1. 用户点击"编辑"
   ↓
2. 打开编辑对话框，预填充当前房型数据
   ↓
3. 用户修改数据，点击"确定"
   ↓
4. 调用 updateRoomTypesConfig()
   ↓
5. 后端验证并保存
   ↓
6. 刷新房型配置和统计数据
```

### 7.3 搜索筛选流程

```
1. 用户输入搜索条件或选择筛选器
   ↓
2. 调用 getRooms() API（已有接口）
   ↓
3. 渲染筛选后的房间列表
```

## 8. 错误处理

### 8.1 验证错误场景

| 场景 | 验证规则 | 错误提示 |
|------|----------|----------|
| capacity ≤ 0 | @Min(1) | "容纳人数至少为1人" |
| capacity > 10 | @Max(10) | "容纳人数最多为10人" |
| basePrice < 0 | @DecimalMin("0.00") | "基础价格不能为负数" |
| basePrice > 10000 | @DecimalMax("10000.00") | "基础价格不能超过10000元" |
| name 为空 | @NotBlank | "房型名称不能为空" |
| name > 50字符 | @Size(max=50) | "房型名称长度不能超过50字符" |
| 房型代码不存在 | 前端校验 | 不允许修改或添加不存在的房型代码 |
| JSON 格式错误 | Jackson 异常处理 | "配置格式错误，请检查输入" |

### 8.2 API 错误响应

所有错误统一使用 `ApiResponse` 格式：

```java
// 成功响应
{
  "code": 200,
  "message": "success",
  "data": { ... }
}

// 验证失败响应 (400)
{
  "code": 400,
  "message": "验证失败",
  "data": {
    "capacity": "容纳人数至少为1人",
    "basePrice": "基础价格不能超过10000元"
  }
}

// 权限不足 (403)
{
  "code": 403,
  "message": "权限不足",
  "data": null
}

// 配置不存在 (404)
{
  "code": 404,
  "message": "房型配置不存在",
  "data": null
}
```

### 8.3 前端错误处理

| 场景 | 处理方式 |
|------|----------|
| 获取房型配置失败 | 显示错误提示 "加载房型配置失败"，显示默认配置数据 |
| 更新房型配置失败 | 显示后端返回的具体错误信息，保持原数据 |
| 获取统计数据失败 | 统计列显示 "N/A" 而非数字 |
| 验证失败 | 在表单字段下方显示具体错误信息 |
| 网络超时 | 显示 "网络连接超时，请重试" |
| 权限不足 | 非管理员用户不显示编辑按钮，或显示禁用状态 |

## 9. 权限控制

### 9.1 角色权限矩阵

| 功能 | ADMIN | STAFF | 说明 |
|------|-------|-------|------|
| 查看房型配置 | ✅ | ✅ | 所有登录用户 |
| 编辑房型配置 | ✅ | ❌ | 仅管理员 |
| 查看客房列表 | ✅ | ✅ | 所有登录用户 |
| 搜索和筛选 | ✅ | ✅ | 所有登录用户 |

### 9.2 前端权限控制

- **STAFF 用户**：编辑按钮显示为禁用状态，点击时提示"需要管理员权限"
- **ADMIN 用户**：编辑按钮可用
- 判断方式：从 localStorage 读取用户信息中的 role 字段

### 9.3 后端权限控制

使用 `@PreAuthorize` 注解：
- 查看：`@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")`
- 编辑：`@PreAuthorize("hasRole('ADMIN')")`

## 10. 实现检查清单

### 后端
- [ ] RoomTypeConfig DTO（包含验证注解）
- [ ] RoomTypeStats DTO
- [ ] SettingsService.getRoomTypesConfig() - 从数据库读取配置
- [ ] SettingsService.updateRoomTypesConfig() - 更新配置（@Transactional）
- [ ] RoomService.getRoomTypeStats() - 统计各状态房间数
- [ ] SettingsController.getRoomTypesConfig() - GET /api/settings/room-types
- [ ] SettingsController.updateRoomTypesConfig() - PUT /api/settings/room-types
- [ ] SettingsController.getRoomTypeStats() - GET /api/settings/room-types/{code}/stats
- [ ] DataInitializer 初始化默认房型配置

### 前端
- [ ] types/settings.ts - 类型定义（RoomTypeConfig, RoomTypeStats）
- [ ] api/settings.ts - API 调用函数（getRoomTypesConfig, updateRoomTypesConfig, getRoomTypeStats）
- [ ] RoomsResource.vue - 房型配置标签（表格显示、编辑功能）
- [ ] RoomsResource.vue - 客房列表标签（只读显示、搜索筛选）
- [ ] 编辑房型对话框组件
- [ ] 权限控制（根据用户角色显示/隐藏编辑按钮）

## 11. 测试要点

- [ ] 房型配置正确加载和显示
- [ ] 编辑房型后数据正确更新
- [ ] 统计数据准确显示
- [ ] 搜索和筛选功能正常
- [ ] 权限控制生效
- [ ] 错误处理正确
