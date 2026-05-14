<template>
  <div class="approval-todo">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>待我审批</span>
          <el-button type="primary" link @click="handleRefresh">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="审批类型">
          <el-select v-model="searchForm.approvalType" placeholder="全部" clearable>
            <el-option label="请假" value="LEAVE" />
            <el-option label="加班" value="OVERTIME" />
            <el-option label="报销" value="REIMBURSEMENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="待提交" :value="0" />
            <el-option label="待审批" :value="1" />
            <el-option label="已通过" :value="2" />
            <el-option label="已驳回" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="提交时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border style="width: 100%; margin-top: 20px">
        <el-table-column prop="approvalNo" label="审批编号" width="180" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="approvalType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.approvalType)">
              {{ getTypeLabel(row.approvalType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" width="100" />
        <el-table-column prop="deptName" label="部门" width="120" />
        <el-table-column prop="submitTime" label="提交时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row.id)">查看</el-button>
            <el-button link type="success" @click="handleApprove(row.id)" v-if="row.status === 1">通过</el-button>
            <el-button link type="danger" @click="handleReject(row.id)" v-if="row.status === 1">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog v-model="approveDialogVisible" title="审批意见" width="500px">
      <el-input
        v-model="approveOpinion"
        type="textarea"
        :rows="4"
        placeholder="请输入审批意见（可选）"
      />
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmApprove">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectDialogVisible" title="驳回原因" width="500px">
      <el-input
        v-model="rejectOpinion"
        type="textarea"
        :rows="4"
        placeholder="请输入驳回原因"
      />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReject">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getTodoApprovalList, approveApproval, rejectApproval } from '@/api/approval'
import { useApprovalRefresh } from '@/composables/useApprovalRefresh'

const router = useRouter()
const tableData = ref([])
const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

const searchForm = ref({
  approvalType: '',
  status: '',
  dateRange: []
})

const approveDialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const approveOpinion = ref('')
const rejectOpinion = ref('')
const currentApprovalId = ref(null)
const autoRefreshTimer = ref(null)
const { refreshTriggers, triggerRefresh } = useApprovalRefresh()

const fetchData = async () => {
  try {
    const params = {
      page: pagination.value.page,
      size: pagination.value.size,
      approvalType: searchForm.value.approvalType || undefined,
      status: searchForm.value.status !== '' ? searchForm.value.status : undefined
    }
    if (searchForm.value.dateRange && searchForm.value.dateRange.length === 2) {
      params.startTime = searchForm.value.dateRange[0]
      params.endTime = searchForm.value.dateRange[1]
    }
    const res = await getTodoApprovalList(params)
    tableData.value = res.data.list
    pagination.value.total = res.data.total
  } catch (error) {
    ElMessage.error('获取数据失败')
  }
}

const resetSearch = () => {
  searchForm.value = {
    approvalType: '',
    status: '',
    dateRange: []
  }
  pagination.value.page = 1
  fetchData()
}

const getTypeLabel = (type) => {
  const map = {
    'LEAVE': '请假',
    'OVERTIME': '加班',
    'REIMBURSEMENT': '报销'
  }
  return map[type] || type
}

const getTypeTagType = (type) => {
  const map = {
    'LEAVE': 'primary',
    'OVERTIME': 'warning',
    'REIMBURSEMENT': 'success'
  }
  return map[type] || ''
}

const getStatusLabel = (status) => {
  const map = {
    0: '待提交',
    1: '待审批',
    2: '已通过',
    3: '已驳回',
    4: '已撤销'
  }
  return map[status] || status
}

const getStatusTagType = (status) => {
  const map = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger',
    4: 'info'
  }
  return map[status] || ''
}

const viewDetail = (id) => {
  router.push(`/approval/detail?id=${id}`)
}

const handleApprove = (id) => {
  currentApprovalId.value = id
  approveOpinion.value = ''
  approveDialogVisible.value = true
}

const handleReject = (id) => {
  currentApprovalId.value = id
  rejectOpinion.value = ''
  rejectDialogVisible.value = true
}

const confirmApprove = async () => {
  try {
    await approveApproval(currentApprovalId.value, approveOpinion.value)
    ElMessage.success('审批通过')
    approveDialogVisible.value = false
    fetchData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const confirmReject = async () => {
  if (!rejectOpinion.value.trim()) {
    ElMessage.warning('请输入驳回原因')
    return
  }
  try {
    await rejectApproval(currentApprovalId.value, rejectOpinion.value)
    ElMessage.success('已驳回')
    rejectDialogVisible.value = false
    fetchData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

watch(refreshTriggers, () => {
  fetchData()
})

const startAutoRefresh = () => {
  if (autoRefreshTimer.value) {
    clearInterval(autoRefreshTimer.value)
  }
  autoRefreshTimer.value = setInterval(() => {
    fetchData()
  }, 30000)
}

const stopAutoRefresh = () => {
  if (autoRefreshTimer.value) {
    clearInterval(autoRefreshTimer.value)
    autoRefreshTimer.value = null
  }
}

onMounted(() => {
  fetchData()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped lang="scss">
.approval-todo {
  .card-header {
    font-size: 16px;
    font-weight: 500;
  }
}
</style>
