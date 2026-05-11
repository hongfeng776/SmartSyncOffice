<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6" v-for="item in statCards" :key="item.title">
        <el-card class="stat-card" :body-style="{ padding: '20px' }">
          <div class="stat-content flex-between">
            <div>
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-label">{{ item.title }}</div>
            </div>
            <div class="stat-icon" :style="{ backgroundColor: item.color }">
              <el-icon size="36" color="#fff">
                <component :is="item.icon" />
              </el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>快捷入口</span>
            </div>
          </template>
          <el-row :gutter="15">
            <el-col :span="8" v-for="item in quickActions" :key="item.title">
              <div class="quick-action" @click="handleQuickAction(item.path)">
                <el-icon :size="32" :color="item.color">
                  <component :is="item.icon" />
                </el-icon>
                <span class="action-title">{{ item.title }}</span>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>系统信息</span>
            </div>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="系统名称">SmartSyncOffice</el-descriptions-item>
            <el-descriptions-item label="系统版本">v1.0.0</el-descriptions-item>
            <el-descriptions-item label="技术架构">Vue3 + Spring Cloud</el-descriptions-item>
            <el-descriptions-item label="当前用户">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</el-descriptions-item>
            <el-descriptions-item label="用户角色">{{ userStore.roles.join(', ') }}</el-descriptions-item>
            <el-descriptions-item label="登录时间">{{ currentTime }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>欢迎使用 SmartSyncOffice 分布式智能办公协同系统</span>
            </div>
          </template>
          <el-alert
            title="系统说明"
            type="info"
            :closable="false"
            show-icon
          >
            <template #default>
              <p>这是一个完整的企业级办公协同系统骨架，包含以下核心功能：</p>
              <ul style="margin: 10px 0 0 20px;">
                <li>用户管理 - 支持用户的增删改查</li>
                <li>角色权限管理 - 基于角色的权限控制</li>
                <li>分布式架构 - 基于 Spring Cloud Alibaba</li>
                <li>JWT认证 - 无状态的Token认证机制</li>
              </ul>
            </template>
          </el-alert>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/modules/user'

const router = useRouter()
const userStore = useUserStore()

const currentTime = ref('')
let timer = null

const statCards = [
  { title: '待办事项', value: '12', icon: 'TodoList', color: '#409EFF' },
  { title: '文件数量', value: '256', icon: 'Folder', color: '#67C23A' },
  { title: '用户数量', value: '36', icon: 'User', color: '#E6A23C' },
  { title: '消息通知', value: '8', icon: 'Bell', color: '#F56C6C' }
]

const quickActions = [
  { title: '待办事项', icon: 'TodoList', color: '#409EFF', path: '/todo' },
  { title: '文件管理', icon: 'Folder', color: '#67C23A', path: '/file' },
  { title: '个人中心', icon: 'User', color: '#E6A23C', path: '/profile' }
]

function updateTime() {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

function handleQuickAction(path) {
  router.push(path)
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  .stat-card {
    border: none;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  }

  .stat-content {
    .stat-value {
      font-size: 28px;
      font-weight: 700;
      color: #303133;
      line-height: 1.2;
    }

    .stat-label {
      font-size: 14px;
      color: #909399;
      margin-top: 8px;
    }

    .stat-icon {
      width: 64px;
      height: 64px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .card-header {
    font-weight: 600;
    font-size: 15px;
    color: #303133;
  }

  .quick-action {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px 0;
    cursor: pointer;
    border-radius: 8px;
    transition: all 0.3s;

    &:hover {
      background-color: #f5f7fa;
      transform: translateY(-2px);
    }

    .action-title {
      margin-top: 10px;
      font-size: 13px;
      color: #606266;
    }
  }

  .mt-20 {
    margin-top: 20px;
  }
}
</style>
