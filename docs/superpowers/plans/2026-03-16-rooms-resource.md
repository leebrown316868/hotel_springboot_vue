# Rooms Resource 页面功能实现计划

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 `/rooms-resource` 页面添加实际功能，包括房型配置管理和客房资源只读查看

**Architecture:** 使用 SystemSettings 表存储房型配置 JSON，SettingsService 负责配置读写，RoomService 负责统计计算，前端使用 Vue 3 + Element Plus

**Tech Stack:** Spring Boot 2.7.18, Vue 3, Element Plus, TypeScript

---

## Chunk 1: 后端 DTO 类定义

**Files:**
- Create: `src/main/java/com/hotel/dto/RoomTypeConfig.java`
- Create: `src/main/java/com/hotel/dto/RoomTypeStats.java`

---

### Task 1: 创建 RoomTypeConfig DTO

**Files:**
- Create: `src/main/java/com/hotel/dto/RoomTypeConfig.java`

- [ ] **Step 1: 创建 RoomTypeConfig DTO 类**

```java
package com.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 房型配置 DTO
 * 用于存储单个房型的配置信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomTypeConfig {

    /**
     * 房型中文名称
     */
    @NotBlank(message = "房型名称不能为空")
    @Size(min = 1, max = 50, message = "房型名称长度必须在1-50之间")
    private String name;

    /**
     * 容纳人数
     */
    @Min(value = 1, message = "容纳人数至少为1人")
    @Max(value = 10, message = "容纳人数最多为10人")
    private Integer capacity;

    /**
     * 基础价格
     */
    @Min(value = 0, message = "基础价格不能为负数")
    @DecimalMax(value = "10000.00", message = "基础价格不能超过10000元")
    private BigDecimal basePrice;
}
```

- [ ] **Step 2: 验证编译**

Run: `mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/hotel/dto/RoomTypeConfig.java
git commit -m "feat(dto): 添加 RoomTypeConfig DTO

- 添加房型名称、容量、基础价格字段
- 添加完整的验证注解
- 支持 JSON 序列化"
```

---

### Task 2: 创建 RoomTypeStats DTO

**Files:**
- Create: `src/main/java/com/hotel/dto/RoomTypeStats.java`

- [ ] **Step 1: 创建 RoomTypeStats DTO 类**

```java
package com.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 房型统计信息 DTO
 * 用于返回指定房型的统计数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomTypeStats {

    /**
     * 房型代码 (SINGLE, DOUBLE, etc.)
     */
    private String code;

    /**
     * 该房型房间总数
     */
    private Integer roomCount;

    /**
     * 空闲房间数
     */
    private Integer availableCount;

    /**
     * 已入住房间数
     */
    private Integer occupiedCount;

    /**
     * 清洁中房间数
     */
    private Integer cleaningCount;

    /**
     * 维护中房间数
     */
    private Integer maintenanceCount;
}
```

- [ ] **Step 2: 验证编译**

Run: `mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/hotel/dto/RoomTypeStats.java
git commit -m "feat(dto): 添加 RoomTypeStats DTO

- 添加房型代码和各状态房间数量字段
- 支持房型统计信息返回"
```

---

## Chunk 2: 后端 Service 层实现

**Files:**
- Modify: `src/main/java/com/hotel/service/SettingsService.java`
- Modify: `src/main/java/com/hotel/service/impl/SettingsServiceImpl.java`
- Modify: `src/main/java/com/hotel/service/RoomService.java`
- Modify: `src/main/java/com/hotel/service/impl/RoomServiceImpl.java`

---

### Task 3: 扩展 SettingsService 接口

**Files:**
- Modify: `src/main/java/com/hotel/service/SettingsService.java`

- [ ] **Step 1: 添加房型配置方法**

在 `SettingsService.java` 中添加以下方法：

```java
package com.hotel.service;

import com.hotel.dto.RoomTypeConfig;
import com.hotel.dto.SettingsRequest;
import com.hotel.dto.SettingsResponse;

import java.util.Map;

public interface SettingsService {
    SettingsResponse getSettings();
    SettingsResponse updateSettings(SettingsRequest request, String updatedBy);

    /**
     * 获取房型配置
     * @return 房型配置 Map，key 为房型代码，value 为房型配置
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

- [ ] **Step 2: 验证编译**

Run: `mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/hotel/service/SettingsService.java
git commit -m "feat(service): SettingsService 添加房型配置方法

- 添加 getRoomTypesConfig() 获取房型配置
- 添加 updateRoomTypesConfig() 更新房型配置"
```

---

### Task 4: 实现 SettingsService 房型配置方法

**Files:**
- Modify: `src/main/java/com/hotel/service/impl/SettingsServiceImpl.java`

- [ ] **Step 1: 添加默认房型配置常量和实现方法**

在 `SettingsServiceImpl.java` 中添加以下代码：

```java
package com.hotel.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.dto.RoomTypeConfig;
import com.hotel.dto.SettingsRequest;
import com.hotel.dto.SettingsResponse;
import com.hotel.entity.SystemSettings;
import com.hotel.mapper.SettingsMapper;
import com.hotel.repository.SystemSettingsRepository;
import com.hotel.service.SettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettingsServiceImpl implements SettingsService {

    private final SystemSettingsRepository repository;
    private final SettingsMapper mapper;
    private final ObjectMapper objectMapper;

    private static final String ROOM_TYPES_CONFIG_KEY = "room_types_config";

    @Override
    public SettingsResponse getSettings() {
        SystemSettings settings = repository.getSingleton();
        return mapper.toResponse(settings);
    }

    @Override
    @Transactional
    public SettingsResponse updateSettings(SettingsRequest request, String updatedBy) {
        SystemSettings settings = repository.getSingleton();
        mapper.updateEntityFromRequest(request, settings);
        settings.setUpdatedBy(updatedBy);
        SystemSettings saved = repository.save(settings);
        return mapper.toResponse(saved);
    }

    @Override
    public Map<String, RoomTypeConfig> getRoomTypesConfig() {
        try {
            SystemSettings settings = repository.getSingleton();
            String configJson = settings.getRoomTypesConfig();

            if (configJson == null || configJson.isEmpty()) {
                log.info("Room types config not found, returning default config");
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

    /**
     * 获取默认房型配置
     */
    private Map<String, RoomTypeConfig> getDefaultRoomTypesConfig() {
        Map<String, RoomTypeConfig> config = new HashMap<>();

        config.put("SINGLE", new RoomTypeConfig("单人间", 1, new BigDecimal("150")));
        config.put("DOUBLE", new RoomTypeConfig("双人间", 2, new BigDecimal("220")));
        config.put("SUITE", new RoomTypeConfig("套房", 2, new BigDecimal("350")));
        config.put("EXECUTIVE_SUITE", new RoomTypeConfig("行政套房", 3, new BigDecimal("500")));
        config.put("PRESIDENTIAL_SUITE", new RoomTypeConfig("总统套房", 4, new BigDecimal("850")));

        return config;
    }
}
```

- [ ] **Step 2: 在 SystemSettings 实体中添加字段**

修改 `src/main/java/com/hotel/entity/SystemSettings.java`，添加 `roomTypesConfig` 字段：

```java
@Column(columnDefinition = "TEXT")
private String roomTypesConfig;
```

- [ ] **Step 3: 验证编译**

Run: `mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/hotel/service/impl/SettingsServiceImpl.java
git add src/main/java/com/hotel/entity/SystemSettings.java
git commit -m "feat(service): 实现 SettingsService 房型配置方法

- 添加 getRoomTypesConfig() 读取配置
- 添加 updateRoomTypesConfig() 更新配置
- 添加默认房型配置
- SystemSettings 添加 roomTypesConfig 字段"
```

---

### Task 5: 扩展 RoomService 添加统计方法

**Files:**
- Modify: `src/main/java/com/hotel/service/RoomService.java`
- Modify: `src/main/java/com/hotel/service/impl/RoomServiceImpl.java`

- [ ] **Step 1: 在 RoomService 接口中添加方法**

在 `RoomService.java` 中添加：

```java
import com.hotel.dto.RoomTypeStats;

/**
 * 获取指定房型的统计信息
 * @param roomType 房型代码
 * @return 房型统计信息
 */
RoomTypeStats getRoomTypeStats(String roomType);
```

- [ ] **Step 2: 实现 RoomServiceImpl 统计方法**

在 `RoomServiceImpl.java` 中添加实现：

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

- [ ] **Step 3: 在 RoomRepository 中添加查询方法**

在 `RoomRepository.java` 中添加：

```java
List<Room> findByType(String type);
```

- [ ] **Step 4: 验证编译**

Run: `mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 5: 提交**

```bash
git add src/main/java/com/hotel/service/RoomService.java
git add src/main/java/com/hotel/service/impl/RoomServiceImpl.java
git add src/main/java/com/hotel/repository/RoomRepository.java
git commit -m "feat(service): RoomService 添加房型统计方法

- 添加 getRoomTypeStats() 获取房型统计信息
- 支持统计各状态房间数量"
```

---

## Chunk 3: 后端 Controller 层实现

**Files:**
- Modify: `src/main/java/com/hotel/controller/SettingsController.java`

---

### Task 6: 在 SettingsController 添加房型配置 API

**Files:**
- Modify: `src/main/java/com/hotel/controller/SettingsController.java`

- [ ] **Step 1: 添加房型配置 API 端点**

在 `SettingsController.java` 中添加以下代码：

```java
package com.hotel.controller;

import com.hotel.dto.ApiResponse;
import com.hotel.dto.RoomTypeConfig;
import com.hotel.dto.RoomTypeStats;
import com.hotel.dto.SettingsRequest;
import com.hotel.dto.SettingsResponse;
import com.hotel.entity.UserRole;
import com.hotel.security.UserDetailsImpl;
import com.hotel.service.RoomService;
import com.hotel.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;
    private final RoomService roomService;

    @GetMapping
    public ResponseEntity<ApiResponse<SettingsResponse>> getSettings() {
        SettingsResponse response = settingsService.getSettings();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping
    @Secured("ROLE_ADMIN")
    public ResponseEntity<ApiResponse<SettingsResponse>> updateSettings(
            @Valid @RequestBody SettingsRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        SettingsResponse response = settingsService.updateSettings(request, userDetails.getEmail());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

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
            @Valid @RequestBody Map<String, RoomTypeConfig> config) {
        Map<String, RoomTypeConfig> updated = settingsService.updateRoomTypesConfig(config);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * 获取房型统计
     */
    @GetMapping("/room-types/{code}/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<RoomTypeStats>> getRoomTypeStats(
            @PathVariable String code) {
        RoomTypeStats stats = roomService.getRoomTypeStats(code);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/hotel/controller/SettingsController.java
git commit -m "feat(controller): SettingsController 添加房型配置 API

- GET /api/settings/room-types 获取房型配置
- PUT /api/settings/room-types 更新房型配置
- GET /api/settings/room-types/{code}/stats 获取房型统计
- 添加权限控制注解"
```

---

## Chunk 4: 数据初始化

**Files:**
- Modify: `src/main/java/com/hotel/init/DataInitializer.java`

---

### Task 7: 初始化默认房型配置

**Files:**
- Modify: `src/main/java/com/hotel/init/DataInitializer.java`

- [ ] **Step 1: 在 initializeSystemSettings 方法中添加房型配置初始化**

修改 `DataInitializer.java` 中的 `initializeSystemSettings()` 方法：

```java
private void initializeSystemSettings() {
    try {
        if (systemSettingsRepository.count() > 0) {
            logger.info("System settings already exist, checking room types config...");

            // 检查是否需要初始化房型配置
            SystemSettings settings = systemSettingsRepository.getSingleton();
            if (settings.getRoomTypesConfig() == null || settings.getRoomTypesConfig().isEmpty()) {
                initializeDefaultRoomTypesConfig(settings);
            }

            logger.info("System settings already exist, skipping full initialization");
            return;
        }

        SystemSettings settings = new SystemSettings();
        initializeDefaultRoomTypesConfig(settings);
        systemSettingsRepository.save(settings);
        logger.info("Default system settings initialized successfully");
    } catch (Exception e) {
        logger.warn("System settings initialization failed: {}", e.getMessage());
    }
}

private void initializeDefaultRoomTypesConfig(SystemSettings settings) {
    try {
        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Map<String, Object>> defaultConfig = new HashMap<>();
        defaultConfig.put("SINGLE", Map.of("name", "单人间", "capacity", 1, "basePrice", 150));
        defaultConfig.put("DOUBLE", Map.of("name", "双人间", "capacity", 2, "basePrice", 220));
        defaultConfig.put("SUITE", Map.of("name", "套房", "capacity", 2, "basePrice", 350));
        defaultConfig.put("EXECUTIVE_SUITE", Map.of("name", "行政套房", "capacity", 3, "basePrice", 500));
        defaultConfig.put("PRESIDENTIAL_SUITE", Map.of("name", "总统套房", "capacity", 4, "basePrice", 850));

        String configJson = objectMapper.writeValueAsString(defaultConfig);
        settings.setRoomTypesConfig(configJson);

        logger.info("Default room types config initialized");
    } catch (Exception e) {
        logger.warn("Failed to initialize default room types config: {}", e.getMessage());
    }
}
```

需要添加导入：

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
```

- [ ] **Step 2: 验证编译**

Run: `mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/hotel/init/DataInitializer.java
git commit -m "feat(init): 添加默认房型配置初始化

- 系统启动时自动初始化房型配置
- 支持已有系统的配置更新"
```

---

## Chunk 5: 前端类型定义和 API

**Files:**
- Create: `frontend/src/types/settings.ts`
- Modify: `frontend/src/api/settings.ts`

---

### Task 8: 创建前端类型定义

**Files:**
- Create: `frontend/src/types/settings.ts`

- [ ] **Step 1: 创建 settings 类型文件**

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

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}
```

- [ ] **Step 2: 提交**

```bash
git add frontend/src/types/settings.ts
git commit -m "feat(types): 添加房型配置相关类型定义

- RoomTypeConfig 房型
- RoomTypeStats 房型统计
- RoomTypeWithStats 带统计的房型
- ApiResponse 通用 API 响应"
```

---

### Task 9: 扩展前端 settings API

**Files:**
- Modify: `frontend/src/api/settings.ts`

- [ ] **Step 1: 添加房型配置 API 方法**

```typescript
import api from '@/utils/api'
import type { Settings, SettingsRequest } from '@/types/settings'
import type { ApiResponse } from '@/types/api'
import type { RoomTypeConfig, RoomTypeStats } from '@/types/settings'

export const settingsApi = {
  // 获取系统设置
  getSettings: async (): Promise<Settings> => {
    const response = await api.get<ApiResponse<Settings>>('/api/settings')
    return response.data.data
  },

  // 更新系统设置
  updateSettings: async (request: SettingsRequest): Promise<Settings> => {
    const response = await api.put<ApiResponse<Settings>>('/api/settings', request)
    return response.data.data
  },

  // 获取房型配置
  getRoomTypesConfig: async (): Promise<Record<string, RoomTypeConfig>> => {
    const response = await api.get<ApiResponse<Record<string, RoomTypeConfig>>>(
      '/api/settings/room-types'
    )
    return response.data.data
  },

  // 更新房型配置
  updateRoomTypesConfig: async (
    config: Record<string, RoomTypeConfig>
  ): Promise<Record<string, RoomTypeConfig>> => {
    const response = await api.put<ApiResponse<Record<string, RoomTypeConfig>>>(
      '/api/settings/room-types',
      config
    )
    return response.data.data
  },

  // 获取房型统计
  getRoomTypeStats: async (code: string): Promise<RoomTypeStats> => {
    const response = await api.get<ApiResponse<RoomTypeStats>>(
      `/api/settings/room-types/${code}/stats`
    )
    return response.data.data
  }
}
```

- [ ] **Step 2: 提交**

```bash
git add frontend/src/api/settings.ts
git commit -m "feat(api): settings API 添加房型配置方法

- getRoomTypesConfig() 获取房型配置
- updateRoomTypesConfig() 更新房型配置
- getRoomTypeStats() 获取房型统计"
```

---

## Chunk 6: 前端 RoomsResource 页面实现

**Files:**
- Modify: `frontend/src/views/RoomsResource.vue`

---

### Task 10: 实现 RoomsResource 页面完整功能

**Files:**
- Modify: `frontend/src/views/RoomsResource.vue`

- [ ] **Step 1: 替换 RoomsResource.vue 完整代码**

```vue
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import Layout from '../components/Layout.vue'
import { settingsApi } from '../api/settings'
import { getRooms } from '../api/room'
import type { RoomTypeConfig, RoomTypeStats, RoomTypeWithStats } from '../types/settings'
import type { RoomResponse } from '../types/room'
import { getUserFromStorage } from '@/utils/auth'

const activeTab = ref('types')
const loading = ref(false)

// 房型配置相关
const roomTypesConfig = ref<Record<string, RoomTypeConfig>>({})
const roomTypesWithStats = ref<RoomTypeWithStats[]>([])
const editDialogVisible = ref(false)
const editingRoomType = ref<{ code: string } & RoomTypeConfig | null>(null)

// 客房列表相关
const rooms = ref<RoomResponse[]>([])
const searchQuery = ref('')
const filterFloor = ref('')
const filterType = ref('')
const filterStatus = ref('')

// 用户权限
const isAdmin = computed(() => {
  const user = getUserFromStorage()
  return user?.role === 'ADMIN'
})

// 房型代码显示名称映射
const roomTypeCodeNames: Record<string, string> = {
  SINGLE: '单人间',
  DOUBLE: '双人间',
  SUITE: '套房',
  EXECUTIVE_SUITE: '行政套房',
  PRESIDENTIAL_SUITE: '总统套房'
}

// 获取房型配置
const fetchRoomTypesConfig = async () => {
  loading.value = true
  try {
    const config = await settingsApi.getRoomTypesConfig()
    roomTypesConfig.value = config

    // 获取各房型统计
    const typesWithStats: RoomTypeWithStats[] = []
    for (const [code, typeConfig] of Object.entries(config)) {
      try {
        const stats = await settingsApi.getRoomTypeStats(code)
        typesWithStats.push({
          code,
          ...typeConfig,
          roomCount: stats.roomCount,
          availableCount: stats.availableCount
        })
      } catch (error) {
        console.error(`Failed to fetch stats for ${code}:`, error)
        typesWithStats.push({
          code,
          ...typeConfig,
          roomCount: 0,
          availableCount: 0
        })
      }
    }
    roomTypesWithStats.value = typesWithStats
  } catch (error: any) {
    console.error('Failed to fetch room types config:', error)
    ElMessage.error('加载房型配置失败')
  } finally {
    loading.value = false
  }
}

// 编辑房型
const handleEditRoomType = (code: string) => {
  const config = roomTypesConfig.value[code]
  if (config) {
    editingRoomType.value = { code, ...config }
    editDialogVisible.value = true
  }
}

// 保存房型配置
const handleSaveRoomType = async () => {
  if (!editingRoomType.value) return

  loading.value = true
  try {
    const { code, ...config } = editingRoomType.value
    const updatedConfig = {
      ...roomTypesConfig.value,
      [code]: config
    }
    await settingsApi.updateRoomTypesConfig(updatedConfig)
    ElMessage.success('房型配置更新成功')
    editDialogVisible.value = false
    await fetchRoomTypesConfig()
  } catch (error: any) {
    console.error('Failed to update room type config:', error)
    ElMessage.error(error.response?.data?.message || '更新房型配置失败')
  } finally {
    loading.value = false
  }
}

// 获取客房列表
const fetchRooms = async () => {
  loading.value = true
  try {
    const response = await getRooms({
      page: 0,
      size: 100,
      number: searchQuery.value || undefined,
      floor: filterFloor.value || undefined,
      status: filterStatus.value || undefined
    })

    if (response.code === 200 && response.data) {
      let roomsList = response.data.rooms

      // 前端按房型筛选
      if (filterType.value) {
        roomsList = roomsList.filter(r => r.type === filterType.value)
      }

      // 按房间号排序
      rooms.value = roomsList.sort((a, b) => {
        const numA = parseInt(a.number)
        const numB = parseInt(b.number)
        if (!isNaN(numA) && !isNaN(numB)) {
          return numA - numB
        }
        return a.number.localeCompare(b.number)
      })
    }
  } catch (error) {
    console.error('Failed to fetch rooms:', error)
    ElMessage.error('加载客房列表失败')
  } finally {
    loading.value = false
  }
}

// 重置筛选
const resetFilters = () => {
  searchQuery.value = ''
  filterFloor.value = ''
  filterType.value = ''
  filterStatus.value = ''
  fetchRooms()
}

// 获取状态标签类型
const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    'AVAILABLE': 'success',
    'OCCUPIED': 'danger',
    'CLEANING': 'warning',
    'MAINTENANCE': 'info'
  }
  return map[status] || 'info'
}

// 获取状态标签文本
const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    'AVAILABLE': '空闲',
    'OCCUPIED': '已入住',
    'CLEANING': '清洁中',
    'MAINTENANCE': '维护中'
  }
  return map[status] || status
}

// 获取房型标签文本
const getTypeLabel = (type: string) => {
  return roomTypeCodeNames[type] || type
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const hours = Math.floor(diff / (1000 * 60 * 60))

  if (hours < 1) return '刚刚'
  if (hours < 24) return `${hours} 小时前`
  const days = Math.floor(hours / 24)
  if (days < 30) return `${days} 天前`
  return date.toLocaleDateString('zh-CN')
}

// 查看房间详情
const viewRoomDetails = (room: RoomResponse) => {
  ElMessage.info(`房间 ${room.number} 的详细信息：${getTypeLabel(room.type)}，${getStatusLabel(room.status)}`)
}

onMounted(() => {
  fetchRoomTypesConfig()
  fetchRooms()
})
</script>

<template>
  <Layout>
    <div class="max-w-7xl mx-auto">
      <header class="mb-8">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">客房资源管理</h1>
          <p class="text-sm text-gray-500 mt-1">管理房型配置、客房详细信息及维护记录</p>
        </div>
      </header>

      <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <el-tabs v-model="activeTab" class="px-6 pt-4">
          <!-- 房型配置标签 -->
          <el-tab-pane label="房型配置" name="types">
            <el-table :data="roomTypesWithStats" style="width: 100%" v-loading="loading" class="mt-4 mb-6">
              <el-table-column prop="code" label="房型代码" width="180" />
              <el-table-column prop="name" label="名称" />
              <el-table-column prop="capacity" label="容量" width="100">
                <template #default="{ row }">
                  {{ row.capacity }} 人
                </template>
              </el-table-column>
              <el-table-column prop="basePrice" label="基础价格" width="120">
                <template #default="{ row }">
                  ¥{{ row.basePrice }}
                </template>
              </el-table-column>
              <el-table-column label="房间数" width="120">
                <template #default="{ row }">
                  {{ row.availableCount }}/{{ row.roomCount }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template #default="{ row }">
                  <el-button
                    size="small"
                    :type="isAdmin ? 'primary' : 'info'"
                    :disabled="!isAdmin"
                    @click="handleEditRoomType(row.code)"
                  >
                    编辑
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <!-- 客房列表标签 -->
          <el-tab-pane label="客房列表" name="inventory">
            <div class="flex gap-4 mb-4 mt-2 flex-wrap">
              <el-input
                v-model="searchQuery"
                placeholder="搜索房间号..."
                prefix-icon="Search"
                class="w-64"
                clearable
                @input="fetchRooms"
              />
              <el-select v-model="filterFloor" placeholder="楼层" clearable class="w-32" @change="fetchRooms">
                <el-option label="1 楼" value="1" />
                <el-option label="2 楼" value="2" />
                <el-option label="3 楼" value="3" />
                <el-option label="4 楼" value="4" />
                <el-option label="5 楼" value="5" />
              </el-select>
              <el-select v-model="filterType" placeholder="房型" clearable class="w-40" @change="fetchRooms">
                <el-option label="单人间" value="SINGLE" />
                <el-option label="双人间" value="DOUBLE" />
                <el-option label="套房" value="SUITE" />
                <el-option label="行政套房" value="EXECUTIVE_SUITE" />
                <el-option label="总统套房" value="PRESIDENTIAL_SUITE" />
              </el-select>
              <el-select v-model="filterStatus" placeholder="状态" clearable class="w-32" @change="fetchRooms">
                <el-option label="空闲" value="AVAILABLE" />
                <el-option label="已入住" value="OCCUPIED" />
                <el-option label="清洁中" value="CLEANING" />
                <el-option label="维护中" value="MAINTENANCE" />
              </el-select>
              <el-button @click="resetFilters">重置筛选</el-button>
            </div>
            <el-table :data="rooms" style="width: 100%" v-loading="loading" class="mb-6">
              <el-table-column prop="number" label="房间号" width="100" />
              <el-table-column prop="type" label="房型" width="120">
                <template #default="{ row }">
                  {{ getTypeLabel(row.type) }}
                </template>
              </el-table-column>
              <el-table-column prop="floor" label="楼层" width="80">
                <template #default="{ row }">
                  {{ row.floor }} 楼
                </template>
              </el-table-column>
              <el-table-column prop="price" label="价格" width="100">
                <template #default="{ row }">
                  ¥{{ row.price }}
                </template>
              </el-table-column>
              <el-table-column label="状态" width="120">
                <template #default="{ row }">
                  <el-tag :type="getStatusType(row.status)">
                    {{ getStatusLabel(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="最后更新" width="120">
                <template #default="{ row }">
                  {{ formatDate(row.updatedAt) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template #default="{ row }">
                  <el-button size="small" @click="viewRoomDetails(row)">查看</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- 编辑房型对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑房型配置"
      width="400px"
    >
      <el-form v-if="editingRoomType" label-width="100px">
        <el-form-item label="房型代码">
          <el-input v-model="editingRoomType.code" disabled />
        </el-form-item>
        <el-form-item label="房型名称" required>
          <el-input v-model="editingRoomType.name" placeholder="请输入房型名称" />
        </el-form-item>
        <el-form-item label="容纳人数" required>
          <el-input-number v-model="editingRoomType.capacity" :min="1" :max="10" class="w-full" />
        </el-form-item>
        <el-form-item label="基础价格" required>
          <el-input-number v-model="editingRoomType.basePrice" :min="0" :max="10000" :precision="2" class="w-full" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSaveRoomType">确定</el-button>
      </template>
    </el-dialog>
  </Layout>
</template>
```

- [ ] **Step 2: 验证前端编译**

Run: `cd frontend && npm run build` (或检查开发服务器无错误)
Expected: 编译成功，无错误

- [ ] **Step 3: 提交**

```bash
git add frontend/src/views/RoomsResource.vue
git commit -m "feat(view): 实现 RoomsResource 页面完整功能

- 房型配置：显示、编辑房型信息
- 客房列表：搜索、筛选、查看详情
- 权限控制：仅管理员可编辑房型
- 统计信息：显示各房型房间数量"
```

---

## Chunk 7: 测试和验证

---

### Task 11: 后端 API 测试

**Files:**
- (No file changes)

- [ ] **Step 1: 启动后端服务**

Run: `mvn spring-boot:run`
Expected: 服务启动成功，无错误日志

- [ ] **Step 2: 测试获取房型配置 API**

Run (使用 curl 或 Postman):
```
GET http://localhost:8080/api/settings/room-types
Authorization: Bearer <admin_token>
```
Expected: 返回 5 种房型配置

- [ ] **Step 3: 测试更新房型配置 API**

Run:
```
PUT http://localhost:8080/api/settings/room-types
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "SINGLE": {
    "name": "单人间",
    "capacity": 1,
    "basePrice": 160
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
Expected: 返回更新后的配置，SINGLE 基础价格变为 160

- [ ] **Step 4: 测试获取房型统计 API**

Run:
```
GET http://localhost:8080/api/settings/room-types/SINGLE/stats
Authorization: Bearer <admin_token>
```
Expected: 返回 SINGLE 房型的统计信息

- [ ] **Step 5: 测试权限控制**

使用 STAFF 角色测试更新 API：
```
PUT http://localhost:8080/api/settings/room-types
Authorization: Bearer <staff_token>
```
Expected: 返回 403 权限不足

---

### Task 12: 前端功能测试

**Files:**
- (No file changes)

- [ ] **Step 1: 启动前端服务**

Run: `cd frontend && npm run dev`
Expected: 服务启动成功

- [ ] **Step 2: 测试房型配置页面**

1. 登录管理员账号
2. 访问 http://localhost:3000/rooms-resource
3. 检查房型配置表格显示正确
4. 点击"编辑"按钮，修改房型信息
5. 保存后验证数据已更新

- [ ] **Step 3: 测试客房列表页面**

1. 切换到"客房列表"标签
2. 测试搜索功能
3. 测试筛选功能（楼层、房型、状态）
4. 测试重置筛选按钮
5. 点击"查看"按钮查看房间详情

- [ ] **Step 4: 测试权限控制**

1. 使用 STAFF 账号登录
2. 访问 /rooms-resource
3. 验证"编辑"按钮显示为禁用状态
4. 验证客房列表可以正常查看

---

## 最终验证清单

- [ ] 后端 API 全部实现并测试通过
- [ ] 前端页面功能完整
- [ ] 权限控制正确生效
- [ ] 错误处理正常工作
- [ ] 数据持久化正确
- [ ] UI 交互流畅

---

## 完成标志

当所有任务完成后，`/rooms-resource` 页面应该：

1. **房型配置标签**：
   - 显示所有房型配置和统计信息
   - 管理员可以编辑房型配置
   - 非管理员编辑按钮禁用

2. **客房列表标签**：
   - 可以搜索房间号
   - 可以按楼层、房型、状态筛选
   - 可以查看房间详情
   - 状态显示为标签，不可编辑

3. **API 端点**：
   - GET /api/settings/room-types - 获取房型配置
   - PUT /api/settings/room-types - 更新房型配置
   - GET /api/settings/room-types/{code}/stats - 获取房型统计
