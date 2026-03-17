# 关于我们页面实施计划

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 创建数据驱动的"关于我们"公开页面，从 system_settings 表读取酒店基础信息，保持与现有公开页面一致的导航体验。

**Architecture:** 前后端分离架构。后端提供公开 API endpoint 返回酒店配置信息，前端 Vue 组件调用 API 并渲染数据。导航栏组件在所有公开页面复用，确保一致的导航体验。

**Tech Stack:** Vue 3 Composition API, TypeScript, Element Plus, Spring Boot, Spring Security, JPA

---

## 文件结构映射

### 后端文件
| 文件 | 操作 | 职责 |
|------|------|------|
| `src/main/java/com/hotel/entity/SystemSettings.java` | 修改 | 添加 description 字段到实体类 |
| `src/main/java/com/hotel/dto/SettingsRequest.java` | 修改 | 添加 description 字段到请求 DTO |
| `src/main/java/com/hotel/dto/SettingsResponse.java` | 修改 | 添加 description 字段到响应 DTO |
| `src/main/java/com/hotel/dto/PublicSettingsResponse.java` | 新建 | 公开信息的响应 DTO（仅包含公开字段） |
| `src/main/java/com/hotel/service/SettingsService.java` | 修改 | 添加 getPublicSettings 方法声明 |
| `src/main/java/com/hotel/service/impl/SettingsServiceImpl.java` | 修改 | 实现 getPublicSettings 方法 |
| `src/main/java/com/hotel/controller/SettingsController.java` | 修改 | 添加 /api/settings/public endpoint |
| `src/main/java/com/hotel/config/SecurityConfig.java` | 修改 | 添加公开访问权限配置 |

### 前端文件
| 文件 | 操作 | 职责 |
|------|------|------|
| `frontend/src/types/settings.ts` | 修改 | 添加 Settings, SettingsRequest, PublicSettings 类型定义 |
| `frontend/src/api/settings.ts` | 修改 | 添加 getPublicSettings API 方法 |
| `frontend/src/components/SimpleHeader.vue` | 修改 | 移除联系方式链接，添加关于我们跳转 |
| `frontend/src/views/About.vue` | 新建 | 关于我们页面组件 |
| `frontend/src/router/index.ts` | 修改 | 添加 /about 路由配置 |

---

## 任务分解

### 阶段一：后端 API 实现

#### Task 1: 修改 SystemSettings 实体类

⚠️ **重要提醒**：如果数据库文件 `data/hotel.db` 已存在，必须先完成 Step 2 的数据库迁移，再启动应用！否则会报错 "column description not found"。

**Files:**
- Modify: `src/main/java/com/hotel/entity/SystemSettings.java`

- [ ] **Step 1: 添加 description 字段**

在 `@Column(length = 200)` 的 `address` 字段后添加：
```java
@Column(length = 1000)
private String description = "欢迎来到 GrandHotel 豪华酒店，我们致力于为您提供最舒适的住宿体验。";
```

- [ ] **Step 2: 数据库迁移（如数据库已存在）**

**重要警告**: 必须在启动应用之前完成数据库迁移！如果跳过此步骤，应用启动时会报错 "column description not found"。

如果数据库文件 `data/hotel.db` 已存在，需要手动添加字段：

**选项A - 使用 SQLite 工具**:
```bash
sqlite3 data/hotel.db "ALTER TABLE system_settings ADD COLUMN description VARCHAR(1000) DEFAULT '欢迎来到 GrandHotel 豪华酒店，我们致力于为您提供最舒适的住宿体验。';"
```

**选项B - 删除数据库重建**（开发环境）:
```bash
rm data/hotel.db
```
重启后端时会自动创建新表。

**选项C - 使用 Hibernate 自动更新**:
在 `application.properties` 中设置:
```properties
spring.jpa.hibernate.ddl-auto=update
```

- [ ] **Step 3: 编译验证**

运行: `mvn compile`
预期: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/hotel/entity/SystemSettings.java
git commit -m "feat(entity): 添加 description 字段到 SystemSettings"
```

#### Task 2: 修改 SettingsRequest DTO

**Files:**
- Modify: `src/main/java/com/hotel/dto/SettingsRequest.java`

- [ ] **Step 1: 添加 description 字段**

在文件中找到其他字段定义位置，添加：
```java
private String description;
```

- [ ] **Step 2: 编译验证**

运行: `mvn compile`
预期: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/hotel/dto/SettingsRequest.java
git commit -m "feat(dto): 添加 description 字段到 SettingsRequest"
```

#### Task 3: 修改 SettingsResponse DTO

**Files:**
- Modify: `src/main/java/com/hotel/dto/SettingsResponse.java`

- [ ] **Step 1: 添加 description 字段**

在 `private String hotelName;` 后添加：
```java
private String description;
```

- [ ] **Step 2: 编译验证**

运行: `mvn compile`
预期: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/hotel/dto/SettingsResponse.java
git commit -m "feat(dto): 添加 description 字段到 SettingsResponse"
```

#### Task 4: 创建 PublicSettingsResponse DTO

**Files:**
- Create: `src/main/java/com/hotel/dto/PublicSettingsResponse.java`

- [ ] **Step 1: 创建新 DTO 文件**

创建文件并添加以下内容：
```java
package com.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicSettingsResponse {
    private String hotelName;
    private String description;
    private String address;
    private String contactPhone;
    private String contactEmail;
}
```

- [ ] **Step 2: 编译验证**

运行: `mvn compile`
预期: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/hotel/dto/PublicSettingsResponse.java
git commit -m "feat(dto): 创建 PublicSettingsResponse 用于公开API"
```

#### Task 5: 修改 SettingsService 接口

**Files:**
- Modify: `src/main/java/com/hotel/service/SettingsService.java`

- [ ] **Step 1: 添加 getPublicSettings 方法声明**

在文件中添加：
```java
PublicSettingsResponse getPublicSettings();
```

- [ ] **Step 2: 编译验证**（会失败，因为还没实现）

运行: `mvn compile`
预期: 编译失败（提示需要实现方法）

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/hotel/service/SettingsService.java
git commit -m "feat(service): 添加 getPublicSettings 方法声明"
```

#### Task 6: 实现 SettingsServiceImpl

**Files:**
- Modify: `src/main/java/com/hotel/service/impl/SettingsServiceImpl.java`
- Read: `src/main/java/com/hotel/service/SettingsService.java`（确认方法签名）

- [ ] **Step 1: 实现 getPublicSettings 方法**

在文件中添加以下导入和方法：
```java
import com.hotel.dto.PublicSettingsResponse;

// 在类中添加方法
@Override
public PublicSettingsResponse getPublicSettings() {
    SystemSettings settings = getSystemSettings();
    return PublicSettingsResponse.builder()
            .hotelName(settings.getHotelName())
            .description(settings.getDescription())
            .address(settings.getAddress())
            .contactPhone(settings.getContactPhone())
            .contactEmail(settings.getContactEmail())
            .build();
}
```

- [ ] **Step 2: 编译验证**

运行: `mvn compile`
预期: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/hotel/service/impl/SettingsServiceImpl.java
git commit -m "feat(service): 实现 getPublicSettings 方法"
```

#### Task 7: 添加公开 API Endpoint

**Files:**
- Modify: `src/main/java/com/hotel/controller/SettingsController.java`

- [ ] **Step 1: 添加导入**

在文件顶部的导入区域添加：
```java
import com.hotel.dto.PublicSettingsResponse;
import org.springframework.web.bind.annotation.RequestMethod;
```

- [ ] **Step 2: 添加公开 endpoint**

在 `getSettings()` 方法后添加：
```java
@GetMapping("/public")
public ResponseEntity<ApiResponse<PublicSettingsResponse>> getPublicSettings() {
    PublicSettingsResponse response = settingsService.getPublicSettings();
    return ResponseEntity.ok(ApiResponse.success(response));
}
```

- [ ] **Step 3: 编译验证**

运行: `mvn compile`
预期: BUILD SUCCESS

- [ ] **Step 4: 启动后端测试**

运行: `mvn spring-boot:run`

等待启动完成后，测试 API：
```bash
curl http://localhost:8080/api/settings/public
```

预期返回：
```json
{"code":200,"message":"Success","data":{"hotelName":"GrandHorizon 酒店及水疗中心","description":"欢迎来到...","address":"123 Ocean Drive, Miami, FL 33139","contactPhone":"+1 (555) 123-4567","contactEmail":"contact@grandhorizon.com"}}
```

- [ ] **Step 5: 提交**

```bash
git add src/main/java/com/hotel/controller/SettingsController.java
git commit -m "feat(controller): 添加 /api/settings/public 公开endpoint"
```

#### Task 8: 配置公开访问权限

**Files:**
- Modify: `src/main/java/com/hotel/config/SecurityConfig.java`

**重要说明**:
- 当前第73行有 `/api/settings/**` 的全局认证规则：
  ```java
  .antMatchers("/api/rooms/**", "/api/guests/**", "/api/bookings/**", "/api/profile/**", "/api/reviews/**", "/api/statistics/**", "/api/settings/**", "/api/notifications/**").authenticated()
  ```
- 这个规则会覆盖所有 `/api/settings/**` 的访问，包括新的 `/api/settings/public` endpoint
- 需要在该全局规则之前添加公开访问规则，并修改全局规则以避免冲突

- [ ] **Step 1: 找到公开访问配置区域**

找到 `.antMatchers("/api/bookings/available-rooms").permitAll()` 这一行（第69行）

- [ ] **Step 2: 添加公开访问规则**

在该行后添加（注意：必须在第73行的全局规则之前）：
```java
.antMatchers(HttpMethod.GET, "/api/settings/public").permitAll()
```

- [ ] **Step 3: 修改全局认证规则（避免冲突）**

找到第73行：
```java
.antMatchers("/api/rooms/**", "/api/guests/**", "/api/bookings/**", "/api/profile/**", "/api/reviews/**", "/api/statistics/**", "/api/settings/**", "/api/notifications/**").authenticated()
```

将 `/api/settings/**` 改为具体的管理端点，避免覆盖公开端点：
```java
.antMatchers("/api/rooms/**", "/api/guests/**", "/api/bookings/**", "/api/profile/**", "/api/reviews/**", "/api/statistics/**", "/api/settings/system", "/api/settings/room-types", "/api/settings/room-types/**", "/api/notifications/**").authenticated()
```

**说明**:
- `/api/settings/system` 保护主设置 endpoint
- `/api/settings/room-types` 和 `/api/settings/room-types/**` 保护房型配置 endpoints
- `/api/settings/public` 保持公开访问（已在 Step 2 添加）

- [ ] **Step 4: 编译验证**

运行: `mvn compile`
预期: BUILD SUCCESS

- [ ] **Step 5: 重启后端并测试 API**

先结束所有 java 进程: `taskkill //F //IM java.exe`
然后启动: `mvn spring-boot:run`

等待启动后，测试未认证访问：
```bash
curl http://localhost:8080/api/settings/public
```

预期: 返回正常数据（无需 token）

- [ ] **Step 6: 提交**

```bash
git add src/main/java/com/hotel/config/SecurityConfig.java
git commit -m "feat(security): 配置 /api/settings/public 公开访问，调整全局认证规则"
```

---

### 阶段二：前端类型和 API

#### Task 9: 添加前端类型定义

**重要说明**：
- 当前 `frontend/src/api/settings.ts` 第2行导入了 `Settings` 和 `SettingsRequest` 类型
- 但 `frontend/src/types/settings.ts` 中缺少这些类型定义（当前只有 `RoomTypeConfig` 和 `RoomTypeStats`）
- 这会导致 TypeScript 编译错误
- 执行此任务后，类型定义将被添加，编译错误将被解决

**Files:**
- Modify: `frontend/src/types/settings.ts`

- [ ] **Step 1: 添加类型接口（追加到现有文件）**

**注意**: 这是追加到现有文件末尾，不要替换文件内容。

在文件末尾添加：
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

- [ ] **Step 2: TypeScript 类型检查**

运行: `cd frontend && npm run type-check`（如果有）或者 `cd frontend && npx vue-tsc --noEmit`
预期: 无类型错误

- [ ] **Step 3: 提交**

```bash
git add frontend/src/types/settings.ts
git commit -m "feat(types): 添加 Settings, SettingsRequest, PublicSettings 类型"
```

#### Task 10: 添加前端 API 方法

**Files:**
- Modify: `frontend/src/api/settings.ts`

- [ ] **Step 1: 添加 getPublicSettings 方法**

在 settingsApi 对象中添加（最后一个方法后）：
```typescript
// 获取公开的酒店信息（无需认证）
getPublicSettings: async (): Promise<PublicSettings> => {
  const response = await api.get<ApiResponse<PublicSettings>>('/api/settings/public')
  return response.data.data
}
```

- [ ] **Step 2: TypeScript 类型检查**

运行: `cd frontend && npx vue-tsc --noEmit`
预期: 无类型错误

- [ ] **Step 3: 提交**

```bash
git add frontend/src/api/settings.ts
git commit -m "feat(api): 添加 getPublicSettings 方法"
```

---

### 阶段三：前端组件和路由

#### Task 11: 修改 SimpleHeader 组件

**Files:**
- Modify: `frontend/src/components/SimpleHeader.vue`

- [ ] **Step 1: 添加 handleAbout 方法**

在 `<script setup>` 中的 `handleHome` 方法后添加：
```typescript
const handleAbout = () => {
  router.push('/about')
}
```

- [ ] **Step 2: 修改"关于我们"链接**

找到 `<a href="#" class="text-gray-700 hover:text-blue-600 font-medium transition-colors">关于我们</a>`
修改为：
```vue
<a @click="handleAbout" class="text-gray-700 hover:text-blue-600 font-medium cursor-pointer transition-colors">
  关于我们
</a>
```

- [ ] **Step 3: 删除"联系方式"链接**

删除整个"联系方式" `<a>` 标签（包括其父元素中的空白）

- [ ] **Step 4: TypeScript 检查**

运行: `cd frontend && npx vue-tsc --noEmit`
预期: 无类型错误

- [ ] **Step 5: 提交**

```bash
git add frontend/src/components/SimpleHeader.vue
git commit -m "feat(header): 移除联系方式，添加关于我们跳转"
```

#### Task 12: 创建 About.vue 组件

**Files:**
- Create: `frontend/src/views/About.vue`

- [ ] **Step 1: 创建组件文件**

创建文件并添加完整内容：
```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, OfficeBuilding, Location, Phone, Message } from '@element-plus/icons-vue'
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
      <!-- 页面标题 -->
      <header class="mb-8">
        <h1 class="text-2xl font-bold text-gray-900">关于我们</h1>
        <p class="text-sm text-gray-500 mt-1">了解 {{ hotelInfo?.hotelName || '酒店' }}</p>
      </header>

      <!-- 加载状态 -->
      <div v-if="loading" class="flex justify-center py-12">
        <el-icon class="is-loading text-4xl text-blue-600"><Loading /></el-icon>
      </div>

      <!-- 酒店信息 -->
      <div v-else-if="hotelInfo" class="space-y-6">
        <!-- 酒店名称卡片 -->
        <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
          <div class="flex items-center mb-4">
            <div class="w-12 h-12 bg-blue-600 rounded-lg flex items-center justify-center text-white mr-4">
              <el-icon :size="24"><OfficeBuilding /></el-icon>
            </div>
            <h2 class="text-2xl font-bold text-gray-900">{{ hotelInfo.hotelName }}</h2>
          </div>
        </div>

        <!-- 酒店简介卡片 -->
        <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
          <h3 class="text-lg font-semibold text-gray-900 mb-4">酒店简介</h3>
          <p class="text-gray-700 leading-relaxed">{{ hotelInfo.description }}</p>
        </div>

        <!-- 联系方式卡片 -->
        <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
          <h3 class="text-lg font-semibold text-gray-900 mb-4">联系方式</h3>
          <div class="space-y-4">
            <div class="flex items-center text-gray-700">
              <el-icon class="mr-3 text-blue-600"><Location /></el-icon>
              <span>{{ hotelInfo.address }}</span>
            </div>
            <div class="flex items-center text-gray-700">
              <el-icon class="mr-3 text-blue-600"><Phone /></el-icon>
              <span>{{ hotelInfo.contactPhone }}</span>
            </div>
            <div class="flex items-center text-gray-700">
              <el-icon class="mr-3 text-blue-600"><Message /></el-icon>
              <span>{{ hotelInfo.contactEmail }}</span>
            </div>
          </div>
        </div>

        <!-- 返回首页按钮 -->
        <div class="flex justify-center pt-4">
          <el-button @click="goHome" type="primary" size="large">
            返回首页
          </el-button>
        </div>
      </div>

      <!-- 错误状态 -->
      <div v-else class="bg-white rounded-2xl shadow-sm border border-gray-100 p-12 text-center">
        <el-icon class="text-6xl text-gray-300 mb-4"><OfficeBuilding /></el-icon>
        <h3 class="text-xl font-semibold text-gray-900 mb-2">加载失败</h3>
        <p class="text-gray-500 mb-6">无法获取酒店信息，请稍后重试</p>
        <el-button type="primary" @click="fetchHotelInfo">重新加载</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 可以添加额外的样式 */
</style>
```

- [ ] **Step 2: 验证图标导入**

运行以下命令验证图标包已安装：
```bash
cd frontend && npm list @element-plus/icons-vue
```
预期: 应该显示已安装的版本号（如 `@element-plus/icons-vue@2.3.1`）

- [ ] **Step 3: TypeScript 类型检查**

运行: `cd frontend && npx vue-tsc --noEmit`
预期: 无类型错误

- [ ] **Step 4: 提交**

```bash
git add frontend/src/views/About.vue
git commit -m "feat(views): 创建 About 组件"
```

#### Task 13: 添加路由配置

**Files:**
- Modify: `frontend/src/router/index.ts`

- [ ] **Step 1: 找到路由插入位置**

找到 `{ path: '/room-detail/:id', name: 'room-detail', ... }` 这个路由配置

- [ ] **Step 2: 添加新路由**

在该路由后添加：
```typescript
{
  path: '/about',
  name: 'about',
  component: () => import('../views/About.vue'),
  meta: { public: true }
}
```

- [ ] **Step 3: TypeScript 类型检查**

运行: `cd frontend && npx vue-tsc --noEmit`
预期: 无类型错误

- [ ] **Step 4: 提交**

```bash
git add frontend/src/router/index.ts
git commit -m "feat(router): 添加 /about 路由"
```

---

### 阶段四：测试和验证

#### Task 14: 端到端测试

**Files:**
- 无

- [ ] **Step 1: 启动后端**

确保后端正在运行（8080端口）
```bash
taskkill //F //IM java.exe
mvn spring-boot:run
```

- [ ] **Step 2: 启动前端**

```bash
taskkill //F //IM bash.exe
cd frontend && npm run dev
```

- [ ] **Step 3: 测试公开 API**

在浏览器或 Postman 中访问：
```
http://localhost:8080/api/settings/public
```

预期: 返回 JSON 数据包含 hotelName, description, address, contactPhone, contactEmail

- [ ] **Step 4: 测试关于我们页面**

在浏览器访问：
```
http://localhost:3000/about
```

预期:
1. 页面正常显示，导航栏存在
2. 显示酒店信息卡片
3. 控制台无错误

- [ ] **Step 5: 测试导航栏跳转**

1. 访问 `http://localhost:3000/browse-rooms`
2. 点击导航栏的"关于我们"链接
3. 验证跳转到 `/about` 页面
4. 验证"联系方式"链接已移除

- [ ] **Step 6: 测试所有公开页面导航**

依次访问并验证导航栏一致性：
- `http://localhost:3000/browse-rooms`
- `http://localhost:3000/room-detail/1`
- `http://localhost:3000/bookings/new`
- `http://localhost:3000/about`

预期: 所有页面导航栏一致，SimpleHeader 正常显示

- [ ] **Step 7: 测试未认证访问**

1. 打浏览器的无痕/隐私模式
2. 直接访问 `http://localhost:3000/about`
3. 验证页面正常显示（无需登录）

- [ ] **Step 8: 移动端响应式测试**

1. 打开浏览器开发者工具
2. 切换到移动设备视图（如 iPhone 14 Pro）
3. 访问 `http://localhost:3000/about`
4. 验证布局正常，卡片不溢出

- [ ] **Step 9: 测试返回首页功能**

1. 在关于我们页面
2. 点击"返回首页"按钮
3. 验证跳转到 `/browse-rooms`

- [ ] **Step 10: 最终提交**

```bash
git add -A
git commit -m "test: 完成关于我们页面端到端测试验证"
```

---

## 验收标准

### 功能验收
- [ ] 点击导航栏"关于我们"正确跳转到 /about 页面
- [ ] 页面正确显示从 API 获取的酒店信息（名称、简介、联系方式）
- [ ] 导航栏在所有公开页面（browse-rooms, room-detail, bookings/new, about）保持一致
- [ ] "联系方式"链接已从导航栏移除
- [ ] 未登录用户可以访问关于我们页面
- [ ] 返回首页按钮正常工作

### 视觉验收
- [ ] 页面使用与 RoomDetail 一致的卡片样式
- [ ] 移动端布局正常，无溢出
- [ ] 图标正确显示
- [ ] 加载动画正常显示

### API 验收
- [ ] GET /api/settings/public 返回正确的数据结构
- [ ] API 支持未认证访问
- [ ] 错误处理正常工作（API 失败时显示错误状态）

### 数据库验收
- [ ] system_settings 表包含 description 字段
- [ ] description 字段有默认值
- [ ] 可以通过后台设置页面修改 description（测试管理功能）

---

## 故障排查

### 后端问题

**问题**: API 返回 401/403
**解决**: 检查 SecurityConfig 中是否正确添加了 `.antMatchers(HttpMethod.GET, "/api/settings/public").permitAll()`

**问题**: description 字段为 null
**解决**: 检查数据库是否已有数据，如果没有需要手动插入或修改初始化逻辑

### 前端问题

**问题**: TypeScript 类型错误
**解决**: 确保 `frontend/src/types/settings.ts` 中导出了 `PublicSettings` 接口

**问题**: 页面空白
**解决**: 检查浏览器控制台错误，确认 API 是否正常返回数据

**问题**: 导航栏不一致
**解决**: 确认所有公开页面都使用了 `<SimpleHeader />` 组件

---

## 相关文档

- 设计文档: `docs/superpowers/specs/2026-03-18-about-page-design.md`
- 参考组件: `frontend/src/views/RoomDetail.vue`
- API 文档: 无（见控制器注释）

---

**开发注意事项:**
- 每个任务完成后立即提交，保持原子性
- 使用 TDD 方式：先编译/测试验证失败，再实现，最后验证通过
- 遵循现有代码风格（使用 Lombok，Builder 模式等）
- 所有日期使用 LocalDateTime
- 响应使用 ApiResponse 包装
