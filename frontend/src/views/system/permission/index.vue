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
      <el-empty description="权限管理功能开发中..." />
    </el-card>
  </div>
</template>

<script setup>
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
