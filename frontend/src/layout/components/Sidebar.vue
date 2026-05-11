<template>
  <div class="sidebar">
    <div class="logo">
      <el-icon size="32" color="#fff">
        <OfficeBuilding />
      </el-icon>
      <transition name="showSidebarLogo">
        <span v-show="!appStore.sidebar.collapsed" class="logo-text">
          SmartSyncOffice
        </span>
      </transition>
    </div>
    <el-menu
      :default-active="activeMenu"
      :collapse="appStore.sidebar.collapsed"
      :collapse-transition="false"
      class="sidebar-menu"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409EFF"
      unique-opened
      router
    >
      <sidebar-item
        v-for="route in userStore.menus"
        :key="route.path"
        :item="route"
        :base-path="route.path"
      />
    </el-menu>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import SidebarItem from './SidebarItem.vue'
import { useAppStore } from '@/store/modules/app'
import { useUserStore } from '@/store/modules/user'
import request from '@/utils/request'

const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const defaultMenus = [
  {
    path: '/dashboard',
    name: 'Dashboard',
    meta: { title: '系统首页', icon: 'HomeFilled' }
  },
  { path: '/todo', meta: { title: '待办事项', icon: 'TodoList' } },
  { path: '/file', meta: { title: '文件管理', icon: 'Folder' } },
  { path: '/profile', meta: { title: '个人中心', icon: 'User' } }
]

onMounted(async () => {
  if (userStore.token && userStore.menus.length === 0) {
    try {
      const res = await request.get('/api/user/menus')
      if (res.data && res.data.length > 0) {
        userStore.setMenus(res.data)
      }
    } catch (error) {
      console.error('获取菜单失败:', error)
    }
  }
})
</script>

<style lang="scss" scoped>
.sidebar {
  height: 100%;
}

.logo {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2b2f3a;
  overflow: hidden;

  .logo-text {
    margin-left: 12px;
    color: #fff;
    font-size: 16px;
    font-weight: 600;
    white-space: nowrap;
  }
}

.sidebar-menu {
  border: none;
  height: calc(100vh - 50px);
  overflow-y: auto;

  :deep(.el-sub-menu__title) {
    padding: 0 20px !important;
  }

  :deep(.el-menu-item) {
    padding: 0 20px !important;
  }
}

.showSidebarLogo-enter-active,
.showSidebarLogo-leave-active {
  transition: opacity 0.3s;
}

.showSidebarLogo-enter-from,
.showSidebarLogo-leave-to {
  opacity: 0;
}
</style>
