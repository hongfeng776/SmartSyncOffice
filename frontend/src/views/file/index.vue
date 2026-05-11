<template>
  <div class="file-container">
    <el-card>
      <template #header>
        <div class="card-header flex-between">
          <span>文件管理</span>
          <el-button type="primary" :icon="Upload">
            上传文件
          </el-button>
        </div>
      </template>
      <el-empty description="暂无文件，功能开发中..." />
    </el-card>
  </div>
</template>

<script setup>
</script>

<style lang="scss" scoped>
.file-container {
  .card-header {
    font-weight: 600;
    font-size: 15px;
    color: #303133;
  }
}
</style>
