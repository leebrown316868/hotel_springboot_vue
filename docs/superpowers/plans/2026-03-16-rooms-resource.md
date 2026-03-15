# Rooms Resource 页面功能实现计划

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking。

**Goal:** 为 `/rooms-resource` 页面添加实际功能，包括房型配置管理和客房资源只读查看

**Architecture:** 使用 SystemSettings 表存储房型配置 JSON，SettingsService 负责配置读写，RoomService 负责统计计算

**Tech Stack:** Spring Boot 2.7.18, Vue 3, Element Plus, TypeScript

---

## 测试账号

**管理员**: admin@hotel.com / admin123 (角色: ADMIN)
**员工**: staff@hotel.com / staff123 (角色: STAFF)

**获取 Token**:
```
POST http://localhost:8080/api/auth/login
{"email": "admin@hotel.com", "password": "admin123"}
```

---

## Chunk 1: 后端 DTO 和实体

### Task 1: 创建 RoomTypeConfig DTO

- [ ] **创建文件** `src/main/java/com/hotel/dto/RoomTypeConfig.java`

```java
package com.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.math.BigDecimal;

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
```

- [ ] **提交**: `git add src/main/java/com/hotel/dto/RoomTypeConfig.java && git commit -m "feat(dto): 添加 RoomTypeConfig DTO"`

### Task 2: 创建 RoomTypeStats DTO

- [ ] **创建文件** `src/main/java/com/hotel/dto/RoomTypeStats.java`

```java
package com.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

- [ ] **提交**: `git add src/main/java/com/hotel/dto/RoomTypeStats.java && git commit -m "feat(dto): 添加 RoomTypeStats DTO"`

### Task 3: 更新 SystemSettings 实体

- [ ] **修改** `src/main/java/com/hotel/entity/SystemSettings.java`

在类中添加字段（updatedBy 字段前）:
```java
@Column(columnDefinition = "TEXT")
private String roomTypesConfig;
```

- [ ] **提交**: `git add src/main/java/com/hotel/entity/SystemSettings.java && git commit -m "feat(entity): SystemSettings 添加 roomTypesConfig 字段"`

### Task 4: 更新数据库迁移

- [ ] **修改** `src/main/java/com/hotel/init/DataInitializer.java` 的 `migrateDatabase()` 方法

添加房型配置字段的迁移：
```java
// Check if room_types_config column exists in system_settings table
ResultSet rs2 = conn.getMetaData().getColumns(null, null, "system_settings", "room_types_config");
if (!rs2.next()) {
    logger.info("Adding room_types_config column to system_settings table...");
    conn.createStatement().executeUpdate("ALTER TABLE system_settings ADD COLUMN room_types_config TEXT");
    logger.info("room_types_config column added successfully");
} else {
    logger.debug("room_types_config column already exists in system_settings table");
}
rs2.close();
```

- [ ] **提交**: `git add src/main/java/com/hotel/init/DataInitializer.java && git commit -m "feat(init): 添加 room_types_config 列迁移"`

---

## Chunk 2: 后端 Service 层

### Task 5: 扩展 SettingsService

- [ ] **修改** `src/main/java/com/hotel/service/SettingsService.java`

添加方法：
```java
import com.hotel.dto.RoomTypeConfig;
import java.util.Map;

Map<String, RoomTypeConfig> getRoomTypesConfig();
@Transactional
Map<String, RoomTypeConfig> updateRoomTypesConfig(Map<String, RoomTypeConfig> config);
```

- [ ] **提交**: `git add src/main/java/com/hotel/service/SettingsService.java && git commit -m "feat(service): SettingsService 添加房型配置方法"`

### Task 6: 实现 SettingsServiceImpl

- [ ] **修改** `src/main/java/com/hotel/service/impl/SettingsServiceImpl.java`

添加导入和字段：
```java
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.dto.RoomTypeConfig;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
private final ObjectMapper objectMapper;
```

添加方法实现：
```java
@Override
public Map<String, RoomTypeConfig> getRoomTypesConfig() {
    try {
        SystemSettings settings = repository.getSingleton();
        String configJson = settings.getRoomTypesConfig();
        if (configJson == null || configJson.isEmpty()) {
            return getDefaultRoomTypesConfig();
        }
        return objectMapper.readValue(configJson, new TypeReference<Map<String, RoomTypeConfig>>() {});
    } catch (Exception e) {
        log.error("Failed to parse room types config, returning default", e);
        return getDefaultRoomTypesConfig();
    }
}

@Override
@Transactional
public Map<String, RoomTypeConfig> updateRoomTypesConfig(Map<String, RoomTypeConfig> config) {
    try {
        String configJson = objectMapper.writeValueAsString(config);
        SystemSettings settings = repository.getSingleton();
        settings.setRoomTypesConfig(configJson);
        repository.save(settings);
        log.info("Room types config updated successfully");
        return config;
    } catch (Exception e) {
        log.error("Failed to update room types config", e);
        throw new RuntimeException("Failed to update room types config: " + e.getMessage(), e);
    }
}

private Map<String, RoomTypeConfig> getDefaultRoomTypesConfig() {
    Map<String, RoomTypeConfig> config = new HashMap<>();
    config.put("SINGLE", new RoomTypeConfig("单人间", 1, new BigDecimal("150")));
    config.put("DOUBLE", new RoomTypeConfig("双人间", 2, new BigDecimal("220")));
    config.put("SUITE", new RoomTypeConfig("套房", 2, new BigDecimal("350")));
    config.put("EXECUTIVE_SUITE", new RoomTypeConfig("行政套房", 3, new BigDecimal("500")));
    config.put("PRESIDENTIAL_SUITE", new RoomTypeConfig("总统套房", 4, new BigDecimal("850")));
    return config;
}
```

- [ ] **验证编译**: `mvn compile`
- [ ] **提交**: `git add src/main/java/com/hotel/service/impl/SettingsServiceImpl.java && git commit -m "feat(service): 实现 SettingsService 房型配置方法"`

### Task 7: 扩展 RoomService

- [ ] **修改** `src/main/java/com/hotel/service/RoomService.java`

添加：
```java
import com.hotel.dto.RoomTypeStats;
RoomTypeStats getRoomTypeStats(String roomType);
```

- [ ] **修改** `src/main/java/com/hotel/service/impl/RoomServiceImpl.java`

添加实现：
```java
@Override
public RoomTypeStats getRoomTypeStats(String roomType) {
    List<Room> rooms = roomRepository.findByType(roomType);
    RoomTypeStats stats = new RoomTypeStats();
    stats.setCode(roomType);
    stats.setRoomCount(rooms.size());
    stats.setAvailableCount((int) rooms.stream().filter(r -> r.getStatus() == RoomStatus.AVAILABLE).count());
    stats.setOccupiedCount((int) rooms.stream().filter(r -> r.getStatus() == RoomStatus.OCCUPIED).count());
    stats.setCleaningCount((int) rooms.stream().filter(r -> r.getStatus() == RoomStatus.CLEANING).count());
    stats.setMaintenanceCount((int) rooms.stream().filter(r -> r.getStatus() == RoomStatus.MAINTENANCE).count());
    return stats;
}
```

- [ ] **修改** `src/main/java/com/hotel/repository/RoomRepository.java`

添加：
```java
List<Room> findByType(String type);
```

- [ ] **验证编译**: `mvn compile`
- [ ] **提交**: `git add src/main/java/com/hotel/service/RoomService.java src/main/java/com/hotel/service/impl/RoomServiceImpl.java src/main/java/com/hotel/repository/RoomRepository.java && git commit -m "feat(service): RoomService 添加房型统计方法"`

---

## Chunk 3: 后端 Controller 层

### Task 8: 更新 SettingsController

- [ ] **修改** `src/main/java/com/hotel/controller/SettingsController.java`

更新导入和类：
```java
import com.hotel.dto.RoomTypeConfig;
import com.hotel.dto.RoomTypeStats;
import com.hotel.service.RoomService;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.Map;

@RequiredArgsConstructor
private final RoomService roomService;
```

添加 API 端点：
```java
@GetMapping("/room-types")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public ResponseEntity<ApiResponse<Map<String, RoomTypeConfig>>> getRoomTypesConfig() {
    Map<String, RoomTypeConfig> config = settingsService.getRoomTypesConfig();
    return ResponseEntity.ok(ApiResponse.success(config));
}

@PutMapping("/room-types")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<ApiResponse<Map<String, RoomTypeConfig>>> updateRoomTypesConfig(
        @Valid @RequestBody Map<String, RoomTypeConfig> config) {
    Map<String, RoomTypeConfig> updated = settingsService.updateRoomTypesConfig(config);
    return ResponseEntity.ok(ApiResponse.success(updated));
}

@GetMapping("/room-types/{code}/stats")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public ResponseEntity<ApiResponse<RoomTypeStats>> getRoomTypeStats(@PathVariable String code) {
    RoomTypeStats stats = roomService.getRoomTypeStats(code);
    return ResponseEntity.ok(ApiResponse.success(stats));
}
```

- [ ] **验证编译**: `mvn compile`
- [ ] **提交**: `git add src/main/java/com/hotel/controller/SettingsController.java && git commit -m "feat(controller): SettingsController 添加房型配置 API"`

---

## Chunk 4: 前端类型和 API

### Task 9: 创建前端类型定义

- [ ] **创建文件** `frontend/src/types/settings.ts`

```typescript
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

export interface RoomTypeWithStats extends RoomTypeConfig {
  code: string
  roomCount: number
  availableCount: number
}
```

- [ ] **提交**: `git add frontend/src/types/settings.ts && git commit -m "feat(types): 添加房型配置类型定义"`

### Task 10: 扩展前端 API

- [ ] **修改** `frontend/src/api/settings.ts`

```typescript
import type { RoomTypeConfig, RoomTypeStats } from '@/types/settings'

export const settingsApi = {
  // ... 现有方法

  getRoomTypesConfig: async (): Promise<Record<string, RoomTypeConfig>> => {
    const response = await api.get<ApiResponse<Record<string, RoomTypeConfig>>>('/api/settings/room-types')
    return response.data.data
  },

  updateRoomTypesConfig: async (config: Record<string, RoomTypeConfig>): Promise<Record<string, RoomTypeConfig>> => {
    const response = await api.put<ApiResponse<Record<string, RoomTypeConfig>>>('/api/settings/room-types', config)
    return response.data.data
  },

  getRoomTypeStats: async (code: string): Promise<RoomTypeStats> => {
    const response = await api.get<ApiResponse<RoomTypeStats>>(`/api/settings/room-types/${code}/stats`)
    return response.data.data
  }
}
```

- [ ] **提交**: `git add frontend/src/api/settings.ts && git commit -m "feat(api): settings API 添加房型配置方法"`

---

## Chunk 5: 前端页面实现

### Task 11: 实现 RoomsResource 页面

- [ ] **替换** `frontend/src/views/RoomsResource.vue` 完整内容

```vue
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import Layout from '../components/Layout.vue'
import { settingsApi } from '../api/settings'
import { getRooms } from '../api/room'
import type { RoomTypeConfig, RoomTypeWithStats } from '../types/settings'
import type { RoomResponse } from '../types/room'
import { getUserFromStorage } from '@/utils/auth'

const activeTab = ref('types')
const loading = ref(false)
const roomTypesWithStats = ref<RoomTypeWithStats[]>([])
const rooms = ref<RoomResponse[]>([])
const editDialogVisible = ref(false)
const editingRoomType = ref<{ code: string } & RoomTypeConfig | null>(null)

const searchQuery = ref('')
const filterFloor = ref('')
const filterType = ref('')
const filterStatus = ref('')

const isAdmin = computed(() => getUserFromStorage()?.role === 'ADMIN')

const roomTypeCodeNames: Record<string, string> = {
  SINGLE: '单人间', DOUBLE: '双人间', SUITE: '套房',
  EXECUTIVE_SUITE: '行政套房', PRESIDENTIAL_SUITE: '总统套房'
}

const fetchRoomTypesConfig = async () => {
  loading.value = true
  try {
    const config = await settingsApi.getRoomTypesConfig()
    const typesWithStats: RoomTypeWithStats[] = []
    for (const [code, typeConfig] of Object.entries(config)) {
      try {
        const stats = await settingsApi.getRoomTypeStats(code)
        typesWithStats.push({ code, ...typeConfig, roomCount: stats.roomCount, availableCount: stats.availableCount })
      } catch {
        typesWithStats.push({ code, ...typeConfig, roomCount: 0, availableCount: 0 })
      }
    }
    roomTypesWithStats.value = typesWithStats
  } catch {
    ElMessage.error('加载房型配置失败')
  } finally {
    loading.value = false
  }
}

const handleEditRoomType = (code: string) => {
  const config = roomTypesWithStats.value.find(t => t.code === code)
  if (config) {
    editingRoomType.value = { code: config.code, name: config.name, capacity: config.capacity, basePrice: config.basePrice }
    editDialogVisible.value = true
  }
}

const handleSaveRoomType = async () => {
  if (!editingRoomType.value) return
  loading.value = true
  try {
    const { code, ...config } = editingRoomType.value
    const allConfig = Object.fromEntries(roomTypesWithStats.value.map(t => [t.code, { name: t.name, capacity: t.capacity, basePrice: t.basePrice }]))
    allConfig[code] = config
    await settingsApi.updateRoomTypesConfig(allConfig)
    ElMessage.success('更新成功')
    editDialogVisible.value = false
    await fetchRoomTypesConfig()
  } catch {
    ElMessage.error('更新失败')
  } finally {
    loading.value = false
  }
}

const fetchRooms = async () => {
  loading.value = true
  try {
    const response = await getRooms({ page: 0, size: 100, number: searchQuery.value || undefined, floor: filterFloor.value || undefined, status: filterStatus.value || undefined })
    if (response.code === 200) {
      rooms.value = response.data.rooms.filter(r => !filterType.value || r.type === filterType.value).sort((a, b) => parseInt(a.number) - parseInt(b.number))
    }
  } finally {
    loading.value = false
  }
}

const resetFilters = () => { searchQuery.value = ''; filterFloor.value = ''; filterType.value = ''; filterStatus.value = ''; fetchRooms() }

const getStatusType = (s: string) => ({ AVAILABLE: 'success', OCCUPIED: 'danger', CLEANING: 'warning', MAINTENANCE: 'info' }[s] || 'info')
const getStatusLabel = (s: string) => ({ AVAILABLE: '空闲', OCCUPIED: '已入住', CLEANING: '清洁中', MAINTENANCE: '维护中' }[s] || s)
const getTypeLabel = (t: string) => roomTypeCodeNames[t] || t

onMounted(() => { fetchRoomTypesConfig(); fetchRooms() })
</script>

<template>
  <Layout>
    <div class="max-w-7xl mx-auto p-6">
      <h1 class="text-2xl font-bold mb-6">客房资源管理</h1>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="房型配置" name="types">
          <el-table :data="roomTypesWithStats" v-loading="loading">
            <el-table-column prop="code" label="代码" width="180" />
            <el-table-column prop="name" label="名称" />
            <el-table-column prop="capacity" label="容量" width="100">
              <template #default="{ row }">{{ row.capacity }} 人</template>
            </el-table-column>
            <el-table-column prop="basePrice" label="价格" width="120">
              <template #default="{ row }">¥{{ row.basePrice }}</template>
            </el-table-column>
            <el-table-column label="房间数" width="120">
              <template #default="{ row }">{{ row.availableCount }}/{{ row.roomCount }}</template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button size="small" :disabled="!isAdmin" @click="handleEditRoomType(row.code)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="客房列表" name="inventory">
          <div class="flex gap-2 mb-4">
            <el-input v-model="searchQuery" placeholder="搜索房间号" clearable @input="fetchRooms" style="width: 200px" />
            <el-select v-model="filterFloor" placeholder="楼层" clearable @change="fetchRooms" style="width: 120px">
              <el-option label="1楼" value="1" /><el-option label="2楼" value="2" /><el-option label="3楼" value="3" /><el-option label="4楼" value="4" /><el-option label="5楼" value="5" />
            </el-select>
            <el-select v-model="filterType" placeholder="房型" clearable @change="fetchRooms" style="width: 140px">
              <el-option label="单人间" value="SINGLE" /><el-option label="双人间" value="DOUBLE" /><el-option label="套房" value="SUITE" /><el-option label="行政套房" value="EXECUTIVE_SUITE" /><el-option label="总统套房" value="PRESIDENTIAL_SUITE" />
            </el-select>
            <el-select v-model="filterStatus" placeholder="状态" clearable @change="fetchRooms" style="width: 120px">
              <el-option label="空闲" value="AVAILABLE" /><el-option label="已入住" value="OCCUPIED" /><el-option label="清洁中" value="CLEANING" /><el-option label="维护中" value="MAINTENANCE" />
            </el-select>
            <el-button @click="resetFilters">重置</el-button>
          </div>
          <el-table :data="rooms" v-loading="loading">
            <el-table-column prop="number" label="房间号" width="100" />
            <el-table-column prop="type" label="房型" width="120">
              <template #default="{ row }">{{ getTypeLabel(row.type) }}</template>
            </el-table-column>
            <el-table-column prop="floor" label="楼层" width="80">
              <template #default="{ row }">{{ row.floor }}楼</template>
            </el-table-column>
            <el-table-column prop="price" label="价格" width="100">
              <template #default="{ row }">¥{{ row.price }}</template>
            </el-table-column>
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>
    <el-dialog v-model="editDialogVisible" title="编辑房型" width="400px">
      <el-form v-if="editingRoomType" label-width="100px">
        <el-form-item label="代码"><el-input v-model="editingRoomType.code" disabled /></el-form-item>
        <el-form-item label="名称"><el-input v-model="editingRoomType.name" /></el-form-item>
        <el-form-item label="容量"><el-input-number v-model="editingRoomType.capacity" :min="1" :max="10" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="editingRoomType.basePrice" :min="0" :max="10000" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="editDialogVisible = false">取消</el-button><el-button type="primary" :loading="loading" @click="handleSaveRoomType">确定</el-button></template>
    </el-dialog>
  </Layout>
</template>
```

- [ ] **验证前端**: `cd frontend && npm run build`
- [ ] **提交**: `git add frontend/src/views/RoomsResource.vue && git commit -m "feat(view): 实现 RoomsResource 页面功能"`

---

## Chunk 6: 测试验证

### Task 12: 测试后端 API

- [ ] **启动后端**: `mvn spring-boot:run`
- [ ] **测试获取房型**: `curl -H "Authorization: Bearer <admin_token>" http://localhost:8080/api/settings/room-types`
- [ ] **测试更新房型**: `curl -X PUT -H "Authorization: Bearer <admin_token>" -H "Content-Type: application/json" -d '{"SINGLE":{"name":"单人间","capacity":1,"basePrice":160},"DOUBLE":{"name":"双人间","capacity":2,"basePrice":220},"SUITE":{"name":"套房","capacity":2,"basePrice":350},"EXECUTIVE_SUITE":{"name":"行政套房","capacity":3,"basePrice":500},"PRESIDENTIAL_SUITE":{"name":"总统套房","capacity":4,"basePrice":850}}' http://localhost:8080/api/settings/room-types`
- [ ] **测试统计**: `curl -H "Authorization: Bearer <admin_token>" http://localhost:8080/api/settings/room-types/SINGLE/stats`
- [ ] **测试权限**: 使用 staff_token 测试更新 API，应返回 403

### Task 13: 测试前端功能

- [ ] **启动前端**: `cd frontend && npm run dev`
- [ ] **测试房型配置**: 登录管理员 → 访问 /rooms-resource → 编辑房型 → 验证更新
- [ ] **测试客房列表**: 切换标签 → 测试搜索筛选 → 验证数据显示
- [ ] **测试权限**: 使用 staff 登录 → 验证编辑按钮禁用

---

## 完成标志

- [ ] GET /api/settings/room-types 返回 5 种房型配置
- [ ] PUT /api/settings/room-types 更新房型配置成功
- [ ] GET /api/settings/room-types/{code}/stats 返回统计信息
- [ ] 前端房型配置标签可编辑（仅管理员）
- [ ] 前端客房列表标签可搜索筛选
- [ ] 权限控制正确生效
