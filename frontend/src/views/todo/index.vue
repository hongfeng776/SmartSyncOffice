<template>
  <div class="todo-container">
    <el-card>
      <template #header>
        <div class="card-header flex-between">
          <span>待办事项</span>
          <el-button type="primary" :icon="Plus">
            新增待办
          </el-button>
        </div>
      </template>
      <el-empty description="暂无待办事项，功能开发中..." />
    </el-card>
  </div>
</template>

<script setup>
</script>

<style lang="scss" scoped>
.todo-container {
  .card-header {
    font-weight: 600;
    font-size: 15px;
    color: #303133;
  }
}
</style>
