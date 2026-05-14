<template>
  <div class="task-detail">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>任务详情</span>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="任务编号">{{ task.taskNo }}</el-descriptions-item>
        <el-descriptions-item label="任务标题">{{ task.title }}</el-descriptions-item>
        <el-descriptions-item label="任务类型">{{ task.taskType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag v-if="task.priority === 3" type="danger">高</el-tag>
          <el-tag v-else-if="task.priority === 2" type="warning">中</el-tag>
          <el-tag v-else type="info">低</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建人">{{ task.creatorName }}</el-descriptions-item>
        <el-descriptions-item label="执行人">{{ task.assigneeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="所属部门">{{ task.deptName }}</el-descriptions-item>
        <el-descriptions-item label="当前进度">
          <el-progress :percentage="task.progress" :stroke-width="10" :color="getProgressColor()" style="width: 200px" />
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="task.status === 0" type="info">待分配</el-tag>
          <el-tag v-else-if="task.status === 1" type="warning">待执行</el-tag>
          <el-tag v-else-if="task.status === 2" type="primary">进行中</el-tag>
          <el-tag v-else-if="task.status === 3" type="success">已完成</el-tag>
          <el-tag v-else type="danger">已取消</el-tag>
          <el-tag v-if="isOverdue()" type="danger" size="small" class="ml-2">已逾期</el-tag>
          <el-tag v-else-if="isUrgent()" type="warning" size="small" class="ml-2">即将到期</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="截止时间" :class="getDeadlineClass()">{{ task.deadline ? formatDateTime(task.deadline) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ task.startTime ? formatDateTime(task.startTime) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ task.completeTime ? formatDateTime(task.completeTime) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(task.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="任务描述" :span="2">{{ task.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ task.remark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">操作记录</el-divider>

      <el-timeline>
        <el-timeline-item
          v-for="record in records"
          :key="record.id"
          :timestamp="formatDateTime(record.operateTime)"
          placement="top"
        >
          <div class="record-content">
            <span class="operator">{{ record.operatorName }}</span>
            <span class="action">{{ getActionText(record.action) }}</span>
            <span v-if="record.progress !== null" class="progress-change">
              (进度变化: +{{ record.progress }}%)
            </span>
            <span v-if="record.comment" class="comment">- {{ record.comment }}</span>
          </div>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getTaskDetail } from '@/api/task'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const task = ref({})
const records = ref([])

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString()
}

const isOverdue = () => {
  if (!task.value.deadline || task.value.status === 3 || task.value.status === 4) return false
  const deadline = new Date(task.value.deadline)
  const now = new Date()
  return deadline.getTime() < now.getTime()
}

const isUrgent = () => {
  if (!task.value.deadline || task.value.status === 3 || task.value.status === 4) return false
  if (isOverdue()) return false
  const deadline = new Date(task.value.deadline)
  const now = new Date()
  const diffHours = (deadline.getTime() - now.getTime()) / (1000 * 60 * 60)
  return diffHours > 0 && diffHours <= 24
}

const getDeadlineClass = () => {
  if (isOverdue()) return 'text-danger'
  if (isUrgent()) return 'text-warning'
  return ''
}

const getProgressColor = () => {
  if (task.value.progress === 100) return '#67c23a'
  if (isOverdue()) return '#f56c6c'
  if (task.value.progress >= 70) return '#67c23a'
  if (task.value.progress >= 30) return '#e6a23c'
  return '#409eff'
}

const getActionText = (action) => {
  const actionMap = {
    'CREATE': '创建了任务',
    'ASSIGN': '分配了任务',
    'START': '开始执行任务',
    'UPDATE': '更新了任务进度',
    'COMPLETE': '完成了任务',
    'CANCEL': '取消了任务'
  }
  return actionMap[action] || action
}

const loadDetail = async () => {
  const taskId = route.query.id
  if (!taskId) {
    ElMessage.error('任务ID不存在')
    return
  }
  loading.value = true
  try {
    const res = await getTaskDetail(taskId)
    task.value = res.data.task
    records.value = res.data.records || []
  } catch (error) {
    ElMessage.error('加载任务详情失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped lang="scss">
.task-detail {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 16px;
    font-weight: 500;
  }

  .text-danger {
    color: #f56c6c;
    font-weight: 500;
  }

  .text-warning {
    color: #e6a23c;
    font-weight: 500;
  }

  .ml-2 {
    margin-left: 8px;
  }

  .record-content {
    .operator {
      font-weight: 500;
      color: #303133;
    }

    .action {
      color: #606266;
    }

    .progress-change {
      color: #409eff;
    }

    .comment {
      color: #909399;
    }
  }
}
</style>
