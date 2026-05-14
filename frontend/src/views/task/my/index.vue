<template>
  <div class="task-my">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的任务</span>
          <el-radio-group v-model="taskType" @change="loadTasks">
            <el-radio-button label="assigned">我执行的</el-radio-button>
            <el-radio-button label="created">我创建的</el-radio-button>
          </el-radio-group>
        </div>
      </template>

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
        <el-form-item label="创建时间">
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
        <el-table-column prop="creatorName" label="创建人" width="120" />
        <el-table-column prop="assigneeName" label="执行人" width="120" />
        <el-table-column label="进度" width="120">
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
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">详情</el-button>
            <el-button v-if="taskType === 'assigned' && row.status === 1" link type="primary" @click="startTask(row)">开始</el-button>
            <el-button v-if="taskType === 'assigned' && row.status === 2" link type="primary" @click="showProgressDialog(row)">更新进度</el-button>
            <el-button v-if="taskType === 'assigned' && row.status === 2" link type="success" @click="completeTask(row)">完成</el-button>
            <el-button v-if="taskType === 'created' && row.status !== 3 && row.status !== 4" link type="danger" @click="cancelTask(row)">取消</el-button>
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

    <el-dialog v-model="progressDialogVisible" title="更新进度" width="500px">
      <el-form :model="progressForm" label-width="80px">
        <el-form-item label="进度">
          <el-slider v-model="progressForm.progress" :min="0" :max="100" show-input />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="progressForm.comment" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="progressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProgress">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { getAssignedTasks, getCreatedTasks, startTask as apiStartTask, updateTaskProgress, completeTask as apiCompleteTask, cancelTask as apiCancelTask } from '@/api/task'

const router = useRouter()
const loading = ref(false)
const taskType = ref('assigned')
const taskList = ref([])
const progressDialogVisible = ref(false)
const currentTask = ref(null)
const progressForm = ref({
  progress: 0,
  comment: ''
})

const searchForm = ref({
  status: null,
  priority: null,
  dateRange: []
})

const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString()
}

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

const resetSearch = () => {
  searchForm.value = {
    status: null,
    priority: null,
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
      startTime: searchForm.value.dateRange?.[0] || null,
      endTime: searchForm.value.dateRange?.[1] || null
    }
    const res = taskType.value === 'assigned'
      ? await getAssignedTasks(params)
      : await getCreatedTasks(params)
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

const startTask = async (row) => {
  try {
    await ElMessageBox.confirm('确定要开始执行此任务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await apiStartTask(row.id)
    ElMessage.success('任务已开始')
    loadTasks()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('开始任务失败')
    }
  }
}

const showProgressDialog = (row) => {
  currentTask.value = row
  progressForm.value.progress = row.progress
  progressForm.value.comment = ''
  progressDialogVisible.value = true
}

const submitProgress = async () => {
  try {
    await updateTaskProgress(currentTask.value.id, progressForm.value.progress, progressForm.value.comment)
    ElMessage.success('进度更新成功')
    progressDialogVisible.value = false
    await loadTasks()
  } catch (error) {
    ElMessage.error('进度更新失败')
  }
}

const completeTask = async (row) => {
  try {
    await ElMessageBox.confirm('确定要完成此任务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await apiCompleteTask(row.id, '任务完成')
    ElMessage.success('任务已完成')
    loadTasks()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('完成任务失败')
    }
  }
}

const cancelTask = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入取消原因', '取消任务', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '取消原因不能为空'
    })
    await apiCancelTask(row.id, value)
    ElMessage.success('任务已取消')
    loadTasks()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消任务失败')
    }
  }
}

onMounted(() => {
  loadTasks()
})
</script>

<style scoped lang="scss">
.task-my {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 16px;
    font-weight: 500;
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
