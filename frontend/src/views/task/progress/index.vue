<template>
  <div class="task-progress">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>任务进度</span>
        </div>
      </template>

      <el-row :gutter="20" class="stat-row">
        <el-col :span="4">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-label">待分配</div>
              <div class="stat-value">{{ stats.pendingAssign }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-label">待执行</div>
              <div class="stat-value">{{ stats.pendingStart }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-label">进行中</div>
              <div class="stat-value">{{ stats.inProgress }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-label">已完成</div>
              <div class="stat-value">{{ stats.completed }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card stat-urgent">
            <div class="stat-content">
              <div class="stat-label">即将到期</div>
              <div class="stat-value">{{ stats.urgent }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card stat-overdue">
            <div class="stat-content">
              <div class="stat-label">已逾期</div>
              <div class="stat-value">{{ stats.overdue }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable @change="loadTasks">
            <el-option label="待分配" :value="0" />
            <el-option label="待执行" :value="1" />
            <el-option label="进行中" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="searchForm.priority" placeholder="全部" clearable @change="loadTasks">
            <el-option label="低" :value="1" />
            <el-option label="中" :value="2" />
            <el-option label="高" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="searchForm.deptId" placeholder="全部" clearable @change="loadTasks">
            <el-option
              v-for="dept in deptList"
              :key="dept.id"
              :label="dept.deptName"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            @change="loadTasks"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadTasks">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="taskList" v-loading="loading" :row-class-name="getTableRowClassName" style="width: 100%">
        <el-table-column prop="taskNo" label="任务编号" width="180" />
        <el-table-column prop="title" label="任务标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="taskType" label="任务类型" width="120" />
        <el-table-column label="优先级" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.priority === 3" type="danger" effect="dark" class="priority-high">
              <el-icon><Warning /></el-icon> 高
            </el-tag>
            <el-tag v-else-if="row.priority === 2" type="warning">中</el-tag>
            <el-tag v-else type="info">低</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deptName" label="所属部门" width="120" />
        <el-table-column prop="creatorName" label="创建人" width="120" />
        <el-table-column prop="assigneeName" label="执行人" width="120" />
        <el-table-column label="进度" width="150">
          <template #default="{ row }">
            <el-progress :percentage="row.progress" :stroke-width="10" :color="getProgressColor(row)" />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="info">待分配</el-tag>
            <el-tag v-else-if="row.status === 1" type="warning">待执行</el-tag>
            <el-tag v-else-if="row.status === 2" type="primary">进行中</el-tag>
            <el-tag v-else-if="row.status === 3" type="success">已完成</el-tag>
            <el-tag v-else type="danger">已取消</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deadline" label="截止时间" width="220">
          <template #default="{ row }">
            <div v-if="row.deadline">
              <span :class="getDeadlineClass(row)">{{ formatDateTime(row.deadline) }}</span>
              <el-tag v-if="isOverdue(row) && row.status !== 3 && row.status !== 4" type="danger" size="small" class="ml-2">已逾期</el-tag>
              <el-tag v-else-if="isUrgent(row) && row.status !== 3 && row.status !== 4" type="warning" size="small" class="ml-2">即将到期</el-tag>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadTasks"
        @current-change="loadTasks"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { getAllTasks } from '@/api/task'
import request from '@/utils/request'

const router = useRouter()
const loading = ref(false)
const taskList = ref([])
const deptList = ref([])

const searchForm = ref({
  status: null,
  priority: null,
  deptId: null,
  dateRange: []
})

const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

const isOverdue = (row) => {
  if (!row.deadline || row.status === 3 || row.status === 4) return false
  const deadline = new Date(row.deadline)
  const now = new Date()
  return deadline.getTime() < now.getTime()
}

const isUrgent = (row) => {
  if (!row.deadline || row.status === 3 || row.status === 4) return false
  if (isOverdue(row)) return false
  const deadline = new Date(row.deadline)
  const now = new Date()
  const diffHours = (deadline.getTime() - now.getTime()) / (1000 * 60 * 60)
  return diffHours > 0 && diffHours <= 24
}

const getDeadlineClass = (row) => {
  if (isOverdue(row)) return 'text-danger'
  if (isUrgent(row)) return 'text-warning'
  return ''
}

const getProgressColor = (row) => {
  if (row.progress === 100) return '#67c23a'
  if (isOverdue(row)) return '#f56c6c'
  if (row.progress >= 70) return '#67c23a'
  if (row.progress >= 30) return '#e6a23c'
  return '#409eff'
}

const getTableRowClassName = ({ row }) => {
  if (isOverdue(row)) return 'row-overdue'
  if (isUrgent(row)) return 'row-urgent'
  if (row.priority === 3) return 'row-high-priority'
  return ''
}

const stats = computed(() => {
  const result = {
    pendingAssign: 0,
    pendingStart: 0,
    inProgress: 0,
    completed: 0,
    urgent: 0,
    overdue: 0
  }
  taskList.value.forEach(task => {
    if (task.status === 0) result.pendingAssign++
    else if (task.status === 1) result.pendingStart++
    else if (task.status === 2) result.inProgress++
    else if (task.status === 3) result.completed++
    
    if (isOverdue(task)) result.overdue++
    else if (isUrgent(task)) result.urgent++
  })
  return result
})

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString()
}

const loadDeptList = async () => {
  try {
    const res = await request({
      url: '/api/depts',
      method: 'get'
    })
    deptList.value = res.data || []
  } catch (error) {
    console.error('加载部门列表失败:', error)
  }
}

const resetSearch = () => {
  searchForm.value = {
    status: null,
    priority: null,
    deptId: null,
    dateRange: []
  }
  loadTasks()
}

const loadTasks = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.page,
      size: pagination.value.size,
      status: searchForm.value.status,
      priority: searchForm.value.priority,
      deptId: searchForm.value.deptId,
      startTime: searchForm.value.dateRange?.[0] || null,
      endTime: searchForm.value.dateRange?.[1] || null
    }
    const res = await getAllTasks(params)
    taskList.value = res.data.list
    pagination.value.total = res.data.total
  } catch (error) {
    ElMessage.error('加载任务列表失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = (row) => {
  router.push(`/task/detail?id=${row.id}`)
}

onMounted(() => {
  loadDeptList()
  loadTasks()
})
</script>

<style scoped lang="scss">
.task-progress {
  .card-header {
    font-size: 16px;
    font-weight: 500;
  }

  .stat-row {
    margin-bottom: 20px;

    .stat-card {
      :deep(.el-card__body) {
        padding: 20px;
      }

      &.stat-urgent {
        :deep(.el-card__body) {
          background-color: #fdf6ec;
        }
        .stat-value {
          color: #e6a23c;
        }
      }

      &.stat-overdue {
        :deep(.el-card__body) {
          background-color: #fef0f0;
        }
        .stat-value {
          color: #f56c6c;
        }
      }

      .stat-content {
        text-align: center;

        .stat-label {
          font-size: 14px;
          color: #909399;
          margin-bottom: 10px;
        }

        .stat-value {
          font-size: 32px;
          font-weight: 600;
          color: #303133;
        }
      }
    }
  }

  .search-form {
    margin-bottom: 20px;
  }

  .priority-high {
    .el-icon {
      margin-right: 2px;
    }
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

  :deep(.el-table) {
    .row-overdue {
      background-color: #fef0f0;
    }

    .row-urgent {
      background-color: #fdf6ec;
    }

    .row-high-priority {
      background-color: #fef0f0;
    }
  }
}
</style>
