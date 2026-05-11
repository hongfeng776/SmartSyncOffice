<template>
  <div class="role-container">
    <el-card>
      <template #header>
        <div class="card-header flex-between">
          <span>角色管理</span>
          <el-button type="primary" :icon="Plus">
            新增角色
          </el-button>
        </div>
      </template>
      <el-table :data="roleList" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" />
        <el-table-column prop="roleCode" label="角色编码" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small">编辑</el-button>
            <el-button link type="danger" size="small">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const roleList = ref([])

async function getRoleList() {
  loading.value = true
  try {
    const res = await request.get('/api/roles')
    roleList.value = res.data
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getRoleList()
})
</script>

<style lang="scss" scoped>
.role-container {
  .card-header {
    font-weight: 600;
    font-size: 15px;
    color: #303133;
  }
}
</style>
