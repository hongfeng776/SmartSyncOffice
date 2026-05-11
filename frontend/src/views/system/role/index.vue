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
      <el-empty description="角色管理功能开发中..." />
    </el-card>
  </div>
</template>

<script setup>
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
