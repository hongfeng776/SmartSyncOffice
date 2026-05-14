<template>
  <div class="header-container">
    <div class="left">
      <el-icon
        class="toggle-sidebar"
        @click="appStore.toggleSidebar()"
        :style="{ cursor: 'pointer' }"
      >
        <Fold v-if="!appStore.sidebar.collapsed" />
        <Expand v-else />
      </el-icon>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
          {{ item.meta.title }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="right">
      <div class="notification-icon" @click="goToNotification">
        <el-badge :value="notificationStore.unreadCount" :hidden="notificationStore.unreadCount === 0" class="item">
          <el-icon class="bell-icon"><Bell /></el-icon>
        </el-badge>
      </div>

      <el-dropdown @command="handleCommand">
        <div class="user-info">
          <el-avatar :size="32">
            <el-icon><User /></el-icon>
          </el-avatar>
          <span class="username">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
          <el-icon><CaretBottom /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="notification">
              <el-icon><Bell /></el-icon>
              <span>消息中心</span>
              <el-tag v-if="notificationStore.unreadCount > 0" type="danger" size="small">{{ notificationStore.unreadCount }}</el-tag>
            </el-dropdown-item>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>
              <span>个人中心</span>
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>
              <span>退出登录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { Bell, Fold, Expand, User, CaretBottom, SwitchButton } from '@element-plus/icons-vue'
import { useAppStore } from '@/store/modules/app'
import { useUserStore } from '@/store/modules/user'
import { useNotificationStore } from '@/store/modules/notification'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()
const notificationStore = useNotificationStore()

let refreshInterval = null

const breadcrumbs = computed(() => {
  return route.matched.filter(item => item.meta && item.meta.title)
})

function goToNotification() {
  router.push('/notification')
}

async function handleCommand(command) {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      if (refreshInterval) {
        clearInterval(refreshInterval)
      }
      await userStore.logout()
      router.push('/login')
    } catch {
    }
  } else if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'notification') {
    router.push('/notification')
  }
}

onMounted(() => {
  notificationStore.fetchUnreadCount()
  refreshInterval = setInterval(() => {
    notificationStore.fetchUnreadCount()
  }, 30000)
})

onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
})
</script>

<style lang="scss" scoped>
.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.left {
  display: flex;
  align-items: center;

  .toggle-sidebar {
    font-size: 20px;
    margin-right: 20px;
    color: #606266;
    transition: color 0.3s;

    &:hover {
      color: #409EFF;
    }
  }
}

.right {
  display: flex;
  align-items: center;

  .notification-icon {
    cursor: pointer;
    padding: 0 15px;
    height: 50px;
    display: flex;
    align-items: center;
    transition: background-color 0.3s;

    &:hover {
      background-color: #f5f7fa;
    }

    .bell-icon {
      font-size: 20px;
      color: #606266;
    }
  }

  .user-info {
    display: flex;
    align-items: center;
    cursor: pointer;
    padding: 0 10px;
    height: 50px;
    transition: background-color 0.3s;

    &:hover {
      background-color: #f5f7fa;
    }

    .username {
      margin: 0 8px;
      color: #606266;
      font-size: 14px;
    }
  }
}
</style>
