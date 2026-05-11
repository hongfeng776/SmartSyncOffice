<template>
  <el-container class="app-container">
    <el-aside :width="sidebarWidth" class="sidebar">
      <Sidebar />
    </el-aside>
    <el-container>
      <el-header class="header">
        <Header />
      </el-header>
      <el-main class="main">
        <router-view v-slot="{ Component, route }">
          <transition name="fade-transform" mode="out-in">
            <keep-alive :include="cachedViews">
              <component :is="Component" :key="route.path" />
            </keep-alive>
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import Sidebar from './components/Sidebar.vue'
import Header from './components/Header.vue'
import { useAppStore } from '@/store/modules/app'

const appStore = useAppStore()

const sidebarWidth = computed(() => {
  return appStore.sidebar.collapsed ? '64px' : '200px'
})

const cachedViews = ['Dashboard']
</script>

<style lang="scss" scoped>
.app-container {
  height: 100vh;
  width: 100%;
}

.sidebar {
  transition: width 0.3s;
  background-color: #304156;
  overflow-x: hidden;
}

.header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  height: 50px !important;
  line-height: 50px;
  padding: 0 20px;
}

.main {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s ease;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
