<template>
  <div v-if="!item.meta.hidden">
    <template v-if="hasOneShowingChild(item.children, item) && (!onlyOneChild.children || onlyOneChild.meta.noShowingChildren)">
      <el-menu-item :index="resolvePath(onlyOneChild.path)">
        <el-icon v-if="onlyOneChild.meta.icon">
          <component :is="onlyOneChild.meta.icon" />
        </el-icon>
        <span v-if="!onlyOneChild.meta.icon && item.meta.icon">
          <el-icon><component :is="item.meta.icon" /></el-icon>
        </span>
        <template #title>
          <span>{{ getItemTitle(onlyOneChild) }}</span>
        </template>
      </el-menu-item>
    </template>
    <el-sub-menu v-else ref="subMenu" :index="resolvePath(item.path)" popper-append-to-body>
      <template #title>
        <el-icon v-if="item.meta.icon">
          <component :is="item.meta.icon" />
        </el-icon>
        <span>{{ item.meta.title }}</span>
      </template>
      <sidebar-item
        v-for="child in item.children"
        :key="child.path"
        :item="child"
        :base-path="resolvePath(child.path)"
        class="nest-menu"
      />
    </el-sub-menu>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import path from 'path-browserify'

const props = defineProps({
  item: {
    type: Object,
    required: true
  },
  basePath: {
    type: String,
    default: ''
  }
})

const onlyOneChild = ref(null)

function resolvePath(routePath) {
  if (path.isAbsolute(routePath)) {
    return routePath
  }
  return path.resolve(props.basePath, routePath)
}

function getItemTitle(item) {
  return item.meta && item.meta.title
}

function hasOneShowingChild(children = [], parent) {
  const showingChildren = children.filter(item => {
    if (item.meta.hidden) {
      return false
    } else {
      onlyOneChild.value = item
      return true
    }
  })

  if (showingChildren.length === 1) {
    return true
  }

  if (showingChildren.length === 0) {
    onlyOneChild.value = { ...parent, path: '', noShowingChildren: true }
    return true
  }

  return false
}
</script>

<style lang="scss" scoped>
.nest-menu {
  :deep(.el-menu-item) {
    min-width: 200px !important;
    padding: 0 !important;
  }
}
</style>
