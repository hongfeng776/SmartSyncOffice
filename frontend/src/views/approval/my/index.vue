<template>
  <div class="approval-my">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我发起的</span>
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
        <el-table-column prop="approverName" label="审批人" width="100" />
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
            <el-button link type="warning" @click="handleUrge(row.id)" v-if="row.status === 1">催办</el-button>
            <el-button link type="danger" @click="handleWithdraw(row.id)" v-if="row.status === 1">撤回</el-button>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { getMyApprovalList, urgeApproval, withdrawApproval } from '@/api/approval'

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
    const res = await getMyApprovalList(params)
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
    4: '已撤回'
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

const handleUrge = async (id) => {
  try {
    await ElMessageBox.confirm('确认要催办该审批吗？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await urgeApproval(id)
    ElMessage.success('催办成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('催办失败')
    }
  }
}

const handleWithdraw = async (id) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入撤回原因', '撤回申请', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '撤回原因不能为空'
    })
    await withdrawApproval(id, value)
    ElMessage.success('撤回成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('撤回失败')
    }
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.approval-my {
  .card-header {
    font-size: 16px;
    font-weight: 500;
  }
}
</style>
