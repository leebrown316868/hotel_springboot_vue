# 关于我们页面设计文档

## 概述

创建一个数据驱动的"关于我们"页面，从 system_settings 表读取酒店基础信息，使用与现有公开页面一致的视觉风格。

## 设计目标

1. 在所有公开页面保持导航栏一致性
2. 移除不需要的"联系方式"链接
3. 创建动态的"关于我们"页面，支持后台配置编辑

## 页面路由

| 路径 | 组件 | 访问权限 |
|------|------|----------|
| `/about` | About.vue | 公开访问 |

## 功能需求

### 1. 导航栏优化

**文件**: `frontend/src/components/SimpleHeader.vue`

修改内容：
- 移除"联系方式"链接（第36-38行）：
```vue
<!-- 删除这段代码 -->
<a href="#" class="text-gray-700 hover:text-blue-600 font-medium transition-colors">
  联系方式
</a>
```
- 将"关于我们"的 `href="#"` 改为 `@click="handleAbout"`（第33-35行）
- 添加 `handleAbout` 方法：
```typescript
const handleAbout = () => {
  router.push('/about')
}
```

### 2. 关于我们页面

**文件**: `frontend/src/views/About.vue`（新建）

**页面结构**:
```
┌─────────────────────────────────────────┐
│          SimpleHeader                    │
├─────────────────────────────────────────┤
│                                         │
│  ┌─────────────────────────────────┐   │
│  │  酒店名称                        │   │
│  │  GrandHotel 豪华酒店             │   │
│  └─────────────────────────────────┘   │
│                                         │
│  ┌─────────────────────────────────┐   │
│  │  酒店简介                        │   │
│  │  [描述文本...]                   │   │
│  └─────────────────────────────────┘   │
│                                         │
│  ┌─────────────────────────────────┐   │
│  │  联系方式                        │   │
│  │  📍 地址                         │   │
│  │  📞 电话                         │   │
│  │  ✉️ 邮箱                         │   │
│  └─────────────────────────────────┘   │
│                                         │
│  ┌─────────────────────────────────┐   │
│  │     [ 返回首页 ]                │   │
│  └─────────────────────────────────┘   │
│                                         │
└─────────────────────────────────────────┘
```

**组件代码示例**:
```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import SimpleHeader from '@/components/SimpleHeader.vue'
import { settingsApi } from '@/api/settings'
import type { PublicSettings } from '@/types/settings'

const router = useRouter()
const loading = ref(false)
const hotelInfo = ref<PublicSettings | null>(null)

const fetchHotelInfo = async () => {
  loading.value = true
  try {
    hotelInfo.value = await settingsApi.getPublicSettings()
  } catch (error) {
    console.error('获取酒店信息失败:', error)
    ElMessage.error('获取酒店信息失败')
  } finally {
    loading.value = false
  }
}

const goHome = () => {
  router.push('/browse-rooms')
}

onMounted(() => {
  fetchHotelInfo()
})
</script>

<template>
  <div class="min-h-screen bg-slate-50">
    <SimpleHeader />
    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- 酒店信息卡片 -->
    </div>
  </div>
</template>
```

**数据来源**: GET `/api/settings/public`

### 3. 路由配置

**文件**: `frontend/src/router/index.ts`

添加新路由：
```typescript
{
  path: '/about',
  name: 'about',
  component: () => import('../views/About.vue'),
  meta: { public: true }
}
```

## 视觉设计规范

### 颜色系统
- 主背景: `bg-slate-50`
- 卡片背景: `bg-white`
- 主色调: `blue-600`
- 文字颜色: `gray-900` / `gray-500` / `gray-700`

### 卡片样式
- 圆角: `rounded-2xl`
- 阴影: `shadow-sm`
- 内边距: `p-8`
- 边框: `border border-gray-100`

### 响应式
- 容器最大宽度: `max-w-6xl`
- 移动端适配: 使用 Tailwind 响应式类

## API 接口

### 后端修改

**文件**: `src/main/java/com/hotel/dto/SettingsResponse.java`

添加 `description` 字段：
```java
private String description; // 酒店简介
```

**文件**: `src/main/java/com/hotel/entity/SystemSettings.java`

添加 `description` 字段到实体类。

**文件**: `src/main/java/com/hotel/controller/SettingsController.java`

创建公开访问的 endpoint：
```java
@GetMapping("/public")
public ResponseEntity<ApiResponse<PublicSettingsResponse>> getPublicSettings() {
    PublicSettingsResponse response = settingsService.getPublicSettings();
    return ResponseEntity.ok(ApiResponse.success(response));
}
```

**文件**: `src/main/java/com/hotel/config/SecurityConfig.java`

添加公开访问规则：
```java
.antMatchers(HttpMethod.GET, "/api/settings/public").permitAll()
```

### GET /api/settings/public

**请求**: 无

**响应**:
```typescript
interface PublicSettingsResponse {
  code: number
  data: {
    hotelName: string
    description: string
    address: string
    contactPhone: string
    contactEmail: string
  }
}
```

**错误处理**:
- 500: 服务器错误，显示默认值或错误提示

## 组件结构

```
frontend/src/
├── components/
│   └── SimpleHeader.vue          [修改]
├── views/
│   └── About.vue                 [新建]
├── router/
│   └── index.ts                  [修改]
├── types/
│   └── settings.ts               [修改 - 添加类型定义]
└── api/
    └── settings.ts               [修改 - 添加公开API方法]
```

## 前端类型定义

**文件**: `frontend/src/types/settings.ts`

添加以下接口：
```typescript
export interface Settings {
  id: number
  hotelName: string
  description: string
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
  hotelName?: string
  description?: string
  contactEmail?: string
  contactPhone?: string
  address?: string
  currency?: string
  timezone?: string
  language?: string
  twoFactorEnabled?: boolean
  sessionTimeout?: number
  passwordExpiry?: number
  emailNotificationBookings?: boolean
  emailNotificationCancellations?: boolean
  pushNotificationsEnabled?: boolean
}

export interface PublicSettings {
  hotelName: string
  description: string
  address: string
  contactPhone: string
  contactEmail: string
}
```

## API 客户端修改

**文件**: `frontend/src/api/settings.ts`

添加公开API方法：
```typescript
// 获取公开的酒店信息（无需认证）
getPublicSettings: async (): Promise<PublicSettings> => {
  const response = await api.get<ApiResponse<PublicSettings>>('/api/settings/public')
  return response.data.data
}
```

## 数据流

```
用户访问 /about
    ↓
About.vue onMounted
    ↓
调用 settingsApi.getPublicSettings()
    ↓
GET /api/settings/public (公开访问，无需认证)
    ↓
后端从 system_settings 表读取必要字段
    ↓
返回 PublicSettingsResponse
    ↓
前端渲染页面
```

## 后端实现顺序

1. 修改 SystemSettings 实体类，添加 `description` 字段
2. 修改 SettingsRequest 和 SettingsResponse DTO，添加 `description` 字段
3. 创建 PublicSettingsResponse DTO（仅包含公开字段）
4. 在 SettingsService 添加 `getPublicSettings()` 方法
5. 在 SettingsController 添加 `/api/settings/public` endpoint
6. 在 SecurityConfig 添加公开访问规则

## 边界情况处理

1. **数据加载中**: 显示加载动画（el-icon is-loading）
2. **数据为空**: 显示默认值或"暂无信息"提示
3. **API 错误**: 显示错误消息，提供重试按钮
4. **用户未登录**: 页面公开访问，无需认证

## 测试计划

### 功能测试
- [ ] 点击"关于我们"正确跳转到 /about
- [ ] 页面正确显示从 API 获取的酒店信息
- [ ] 导航栏在所有公开页面保持一致
- [ ] "联系方式"链接已移除

### 视觉测试
- [ ] 移动端布局正常
- [ ] 卡片样式与 RoomDetail 一致
- [ ] 图标正确显示

### API 测试
- [ ] GET /api/settings/public 返回正确数据
- [ ] 错误处理正常工作

## 实现顺序

1. 修改 SimpleHeader.vue（移除联系方式，添加关于我们链接）
2. 创建 About.vue 组件
3. 添加路由配置
4. 确认后端 API 存在并可访问
5. 测试页面功能和视觉效果
