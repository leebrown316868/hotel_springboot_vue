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

```java
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

### 5.2 后端 API

#### SettingsController 新增接口

```java
/**
 * 获取房型配置
 */
@GetMapping("/room-types")
public ResponseEntity<ApiResponse<Map<String, RoomTypeConfig>>> getRoomTypesConfig()

/**
 * 更新房型配置
 */
@PutMapping("/room-types")
public ResponseEntity<ApiResponse<Map<String, RoomTypeConfig>>> updateRoomTypesConfig(
    @RequestBody Map<String, RoomTypeConfig> config
)

/**
 * 获取房型统计
 */
@GetMapping("/room-types/{code}/stats")
public ResponseEntity<ApiResponse<RoomTypeStats>> getRoomTypeStats(
    @PathVariable String code
)
```

#### RoomService 新增方法

```java
/**
 * 获取指定房型的统计信息
 */
RoomTypeStats getRoomTypeStats(String roomType);
```

### 5.3 前端 API

```typescript
// frontend/src/api/settings.ts

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

export const getRoomTypesConfig = async (): Promise<ApiResponse<Record<string, RoomTypeConfig>>>

export const updateRoomTypesConfig = async (
  config: Record<string, RoomTypeConfig>
): Promise<ApiResponse<Record<string, RoomTypeConfig>>>

export const getRoomTypeStats = async (
  code: string
): Promise<ApiResponse<RoomTypeStats>>
```

### 5.4 数据类型定义

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
│  房型配置                                    [新增资源]   │
├─────────────────────────────────────────────────────────┤
│  ┌─────────┬────────┬────────┬──────────┬────────┬────┐ │
│  │ 房型    │ 名称   │ 容量   │ 基础价格 │ 房间数 │操作│ │
│  ├─────────┼────────┼────────┼──────────┼────────┼────┤ │
│  │ 单人间  │ 单人间 │   1    │   ¥150   │  15/20 │编辑│ │
│  │ 双人间  │ 双人间 │   2    │   ¥220   │  20/30 │编辑│ │
│  │ 套房    │ 套房   │   2    │   ¥350   │   5/10 │编辑│ │
│  │ 行政套房│ 行政套房│   3    │   ¥500   │   2/5  │编辑│ │
│  │ 总统套房│ 总统套房│   4    │   ¥850   │   1/2  │编辑│ │
│  └─────────┴────────┴────────┴──────────┴────────┴────┘ │
│  备注：显示格式为 "空闲数/总数"                           │
└─────────────────────────────────────────────────────────┘
```

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
   - getRoomTypeStats() for each type - 获取各房型统计
   ↓
3. 渲染房型配置表格
```

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

| 场景 | 处理方式 |
|------|----------|
| 获取房型配置失败 | 显示错误提示，使用默认配置 |
| 更新房型配置失败 | 显示错误提示，保持原数据 |
| 获取统计数据失败 | 显示 "N/A" 而非数字 |
| 网络超时 | 显示 "加载失败，请重试" |

## 9. 权限控制

- **查看**: 所有已登录用户 (ADMIN, STAFF)
- **编辑房型**: 仅 ADMIN

## 10. 实现检查清单

### 后端
- [ ] RoomTypeStats DTO
- [ ] RoomTypeConfig DTO
- [ ] SettingsService.getRoomTypesConfig()
- [ ] SettingsService.updateRoomTypesConfig()
- [ ] RoomService.getRoomTypeStats()
- [ ] SettingsController.getRoomTypesConfig()
- [ ] SettingsController.updateRoomTypesConfig()
- [ ] SettingsController.getRoomTypeStats()

### 前端
- [ ] types/settings.ts - 类型定义
- [ ] api/settings.ts - API 调用
- [ ] RoomsResource.vue - 房型配置标签
- [ ] RoomsResource.vue - 客房列表标签
- [ ] 编辑房型对话框

## 11. 测试要点

- [ ] 房型配置正确加载和显示
- [ ] 编辑房型后数据正确更新
- [ ] 统计数据准确显示
- [ ] 搜索和筛选功能正常
- [ ] 权限控制生效
- [ ] 错误处理正确
