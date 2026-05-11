<template>
  <div class="profile-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名">
          {{ userStore.userInfo?.username }}
        </el-descriptions-item>
        <el-descriptions-item label="真实姓名">
          {{ userStore.userInfo?.realName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="邮箱">
          {{ userStore.userInfo?.email || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="手机号">
          {{ userStore.userInfo?.phone || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="用户角色" :span="2">
          <el-tag v-for="role in userStore.roles" :key="role" style="margin-right: 8px;">
            {{ roleMap[role] || role }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="权限列表" :span="2">
          <el-tag
            v-for="perm in displayPermissions"
            :key="perm"
            type="success"
            style="margin-right: 8px; margin-bottom: 8px;"
          >
            {{ permissionMap[perm] || perm }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()

const roleMap = {
  'ADMIN': '超级管理员',
  'EMPLOYEE': '普通员工'
}

const permissionMap = {
  'dashboard': '系统首页',
  'system': '系统管理',
  'system:user': '用户管理',
  'system:role': '角色管理',
  'system:permission': '权限管理',
  'profile': '个人中心',
  'todo': '待办事项',
  'file': '文件管理'
}

const displayPermissions = computed(() => {
  return userStore.permissions.filter(p => !p.startsWith('ROLE_'))
})
</script>

<style lang="scss" scoped>
.profile-container {
  .card-header {
    font-weight: 600;
    font-size: 15px;
    color: #303133;
  }
}
</style>
