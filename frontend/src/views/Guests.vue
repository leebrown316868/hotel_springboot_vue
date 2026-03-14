<script setup lang="ts">
import { ref, computed } from 'vue'
import Layout from '../components/Layout.vue'

const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const guests = ref([
  { id: 1, name: '张三', email: 'zhangsan@example.com', phone: '13800138000', country: '中国', totalBookings: 5, lastStay: '2023-10-24', status: '活跃' },
  { id: 2, name: '李四', email: 'lisi@example.com', phone: '13900139000', country: '中国', totalBookings: 2, lastStay: '2023-10-25', status: '活跃' },
  { id: 3, name: '王五', email: 'wangwu@example.com', phone: '13700137000', country: '中国', totalBookings: 1, lastStay: '2023-10-25', status: '活跃' },
  { id: 4, name: '赵六', email: 'zhaoliu@example.com', phone: '13600136000', country: '中国', totalBookings: 8, lastStay: '2023-10-26', status: 'VIP' },
  { id: 5, name: '田中裕子', email: 'y.tanaka@example.jp', phone: '+81 3 1234 5678', country: '日本', totalBookings: 3, lastStay: '2023-09-15', status: '活跃' },
  { id: 6, name: '汉斯·穆勒', email: 'h.mueller@example.de', phone: '+49 30 1234567', country: '德国', totalBookings: 4, lastStay: '2023-08-20', status: '活跃' },
  { id: 7, name: '苏菲·马丁', email: 's.martin@example.fr', phone: '+33 1 23 45 67 89', country: '法国', totalBookings: 2, lastStay: '2023-10-10', status: '不活跃' },
  { id: 8, name: '陈伟', email: 'chen.wei@example.cn', phone: '13500135000', country: '中国', totalBookings: 12, lastStay: '2023-11-01', status: 'VIP' },
])

const filteredGuests = computed(() => {
  return guests.value.filter(guest => 
    guest.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
    guest.email.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

const paginatedGuests = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredGuests.value.slice(start, end)
})

const getStatusType = (status: string) => {
  switch(status) {
    case 'VIP': return 'warning'
    case '活跃': return 'success'
    case '不活跃': return 'info'
    default: return ''
  }
}
</script>

<template>
  <Layout>
    <div class="max-w-7xl mx-auto">
      <header class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">客户管理</h1>
          <p class="text-sm text-gray-500 mt-1">查看并管理您的酒店客户资料</p>
        </div>
        <el-button type="primary" size="large" icon="Plus">添加新客户</el-button>
      </header>

      <div class="bg-white p-4 rounded-xl shadow-sm border border-gray-100 mb-6">
        <div class="max-w-md">
          <el-input
            v-model="searchQuery"
            placeholder="按姓名或邮箱搜索客户..."
            prefix-icon="Search"
            clearable
          />
        </div>
      </div>

      <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <el-table :data="paginatedGuests" style="width: 100%">
          <el-table-column label="客户" min-width="200">
            <template #default="scope">
              <div class="flex items-center gap-3">
                <el-avatar :size="32" class="bg-blue-100 text-blue-600 font-bold">
                  {{ scope.row.name[0] }}
                </el-avatar>
                <div>
                  <div class="font-bold text-gray-900">{{ scope.row.name }}</div>
                  <div class="text-xs text-gray-500">{{ scope.row.email }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="phone" label="电话" width="160" />
          <el-table-column prop="country" label="国家/地区" width="120" />
          <el-table-column prop="totalBookings" label="预订次数" width="100" align="center" />
          <el-table-column prop="lastStay" label="上次入住" width="120" />
          <el-table-column label="状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)" effect="light" round>
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="right" width="120">
            <template #default="scope">
              <el-dropdown trigger="click">
                <el-button link icon="MoreFilled" />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item icon="View">查看资料</el-dropdown-item>
                    <el-dropdown-item icon="Edit">编辑详情</el-dropdown-item>
                    <el-dropdown-item icon="Calendar">预订历史</el-dropdown-item>
                    <el-dropdown-item divided type="danger" icon="Delete">删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>

        <div class="p-4 border-t border-gray-100 flex justify-end">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="filteredGuests.length"
            layout="total, prev, pager, next"
          />
        </div>
      </div>
    </div>
  </Layout>
</template>
