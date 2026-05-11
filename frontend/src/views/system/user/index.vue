<template>
  <div class="user-container">
    <el-card>
      <template #header>
        <div class="card-header flex-between">
          <span>用户管理</span>
          <el-button type="primary" :icon="Plus">
            新增用户
          </el-button>
        </div>
      </template>
      <el-empty description="用户管理功能开发中..." />
    </el-card>
  </div>
</template>

<script setup>
</script>

<style lang="scss" scoped>
.user-container {
  .card-header {
    font-weight: 600;
    font-size: 15px;
    color: #303133;
  }
}
</style>
