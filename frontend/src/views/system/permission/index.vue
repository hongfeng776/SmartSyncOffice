<template>
  <div class="permission-container">
    <el-card>
      <template #header>
        <div class="card-header flex-between">
          <span>权限管理</span>
          <el-button type="primary" :icon="Plus">
            新增权限
          </el-button>
        </div>
      </template>
      <el-table :data="permissionList" v-loading="loading" border row-key="id">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="permissionName" label="权限名称" />
        <el-table-column prop="permissionCode" label="权限编码" />
        <el-table-column prop="resourceType" label="资源类型" width="100">
          <template #default="scope">
            <el-tag>{{ scope.row.resourceType === 'menu' ? '菜单' : '按钮' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" />
        <el-table-column prop="icon" label="图标" width="80" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
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
const permissionList = ref([])

async function getPermissionList() {
  loading.value = true
  try {
    const res = await request.get('/api/permissions')
    permissionList.value = res.data
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getPermissionList()
})
</script>

<style lang="scss" scoped>
.permission-container {
  .card-header {
    font-weight: 600;
    font-size: 15px;
    color: #303133;
  }
}
</style>
