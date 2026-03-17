# 房间详情页面设计文档

## 概述

创建一个独立的房间详情页面，允许未登录用户查看房间完整信息（包括图片轮播），并可从详情页直接跳转到预订流程。

## 目标

- 提供更好的房间信息展示体验
- 支持多图片展示
- 保持与现有预订流程的兼容性

## 架构设计

### 数据模型变更

**Room实体扩展（`Room.java`）：**
```java
@Column(columnDefinition = "TEXT")
private String images;  // JSON字符串: ["url1", "url2", "url3"]
```

**数据库迁移：**
```sql
ALTER TABLE rooms ADD COLUMN images TEXT;
```

### 后端API设计

**新增端点：** `GET /api/rooms/{id}`

**权限：** 公开访问（未登录用户可查看）

**返回数据结构：**
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "number": "101",
    "floor": "1",
    "type": "DOUBLE",
    "status": "AVAILABLE",
    "price": 120,
    "capacity": 2,
    "images": ["url1", "url2"],
    "roomTypeConfig": {
      "name": "双人间",
      "capacity": 2,
      "basePrice": 120
    }
  }
}
```

**Controller层：**
- `RoomController.getRoomById(Long id)` - 使用现有方法，确保返回数据包含images字段

**权限配置：**
- `SecurityConfig.java`: 添加 `.antMatchers("/api/rooms/*").permitAll()`

### 前端设计

**新增组件：** `views/RoomDetail.vue`

**路由配置：**
```typescript
{
  path: '/room-detail/:id',
  name: 'room-detail',
  component: () => import('../views/RoomDetail.vue'),
  meta: { public: true }
}
```

**页面布局：**
- SimpleHeader导航栏
- 返回按钮
- 图片轮播区（el-carousel）
- 房间信息展示区
- 立即预订按钮

**交互流程：**
1. 用户点击房间卡片 → 跳转到 `/room-detail/:id`
2. 页面加载时调用API获取房间详情
3. 展示图片轮播和房间信息
4. 点击预订按钮 → 存储房间信息 → 跳转到 `/bookings/new`

**BrowseRooms.vue修改：**
- 房间卡片添加点击事件
- 修改为：点击整个卡片跳转到详情页
- 保留预订按钮作为快捷入口

## 数据流

```
用户点击房间卡片
    ↓
router.push('/room-detail/:id')
    ↓
RoomDetail.vue onMounted
    ↓
调用 GET /api/rooms/{id}
    ↓
展示房间详情（图片轮播 + 信息）
    ↓
用户点击"立即预订"
    ↓
sessionStorage存储房间信息
    ↓
router.push('/bookings/new')
```

## 错误处理

- 房间不存在（404）：显示友好提示，提供返回列表按钮
- API调用失败：显示错误提示，提供重试选项
- 无图片数据：显示默认占位图

## 实施步骤

1. 后端：修改Room实体，添加images字段
2. 后端：添加数据库迁移SQL
3. 后端：修改SecurityConfig添加公开访问
4. 前端：创建RoomDetail.vue组件
5. 前端：添加路由配置
6. 前端：修改BrowseRooms.vue添加点击跳转
7. 前端：添加API调用方法

## 测试要点

- 未登录用户可访问房间详情页
- 图片轮播正常显示
- 预订按钮正确跳转
- 返回按钮正确导航
- 错误处理正常工作
