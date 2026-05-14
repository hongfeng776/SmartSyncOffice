<template>
  <div class="approval-detail">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>审批详情</span>
          <el-button link type="primary" @click="goBack">返回</el-button>
        </div>
      </template>

      <el-descriptions :column="2" border v-if="approval">
        <el-descriptions-item label="审批编号">{{ approval.approvalNo }}</el-descriptions-item>
        <el-descriptions-item label="审批类型">
          <el-tag :type="getTypeTagType(approval.approvalType)">
            {{ getTypeLabel(approval.approvalType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="标题">{{ approval.title }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTagType(approval.status)">
            {{ getStatusLabel(approval.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请人">{{ approval.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ approval.deptName }}</el-descriptions-item>
        <el-descriptions-item label="审批人">{{ approval.approverName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ approval.submitTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审批时间">{{ approval.approveTime || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">详细信息</el-divider>

      <el-descriptions :column="2" border v-if="businessDetail">
        <template v-if="approval?.approvalType === 'LEAVE'">
          <el-descriptions-item label="请假类型">{{ businessDetail.leaveType }}</el-descriptions-item>
          <el-descriptions-item label="请假天数">{{ businessDetail.leaveDays }} 天</el-descriptions-item>
          <el-descriptions-item label="开始时间" :span="2">{{ businessDetail.startTime }}</el-descriptions-item>
          <el-descriptions-item label="结束时间" :span="2">{{ businessDetail.endTime }}</el-descriptions-item>
          <el-descriptions-item label="请假原因" :span="2">{{ businessDetail.reason }}</el-descriptions-item>
        </template>
        <template v-else-if="approval?.approvalType === 'OVERTIME'">
          <el-descriptions-item label="加班类型">{{ businessDetail.overtimeType }}</el-descriptions-item>
          <el-descriptions-item label="加班时长">{{ businessDetail.overtimeHours }} 小时</el-descriptions-item>
          <el-descriptions-item label="开始时间" :span="2">{{ businessDetail.startTime }}</el-descriptions-item>
          <el-descriptions-item label="结束时间" :span="2">{{ businessDetail.endTime }}</el-descriptions-item>
          <el-descriptions-item label="加班原因" :span="2">{{ businessDetail.reason }}</el-descriptions-item>
        </template>
        <template v-else-if="approval?.approvalType === 'REIMBURSEMENT'">
          <el-descriptions-item label="费用类型">{{ businessDetail.expenseType }}</el-descriptions-item>
          <el-descriptions-item label="总金额">¥{{ businessDetail.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="费用日期" :span="2">{{ businessDetail.expenseDate }}</el-descriptions-item>
          <el-descriptions-item label="费用说明" :span="2">{{ businessDetail.reason }}</el-descriptions-item>
        </template>
      </el-descriptions>

      <el-divider content-position="left">审批记录</el-divider>

      <el-timeline>
        <el-timeline-item
          v-for="(record, index) in records"
          :key="index"
          :timestamp="formatDateTime(record.operateTime)"
          :type="getRecordType(record.action)"
          placement="top"
        >
          <div class="record-item">
            <div class="record-header">
              <el-tag :type="getRecordType(record.action)" size="small">
                {{ getRecordActionLabel(record.action) }}
              </el-tag>
              <span class="record-user" v-if="record.approverName">
                <el-icon><User /></el-icon>
                {{ record.approverName }}
              </span>
            </div>
            <div class="record-node" v-if="record.node">
              <span>审批节点：{{ getNodeLabel(record.node) }}</span>
            </div>
            <div class="record-opinion" v-if="record.opinion">
              <div class="opinion-label">审批意见：</div>
              <div class="opinion-content">{{ record.opinion }}</div>
            </div>
          </div>
        </el-timeline-item>
      </el-timeline>

      <div class="action-buttons" v-if="approval?.status === 1">
        <template v-if="isApprover">
          <el-button type="success" @click="handleApprove">通过</el-button>
          <el-button type="danger" @click="handleReject">驳回</el-button>
        </template>
        <template v-else>
          <el-button type="warning" @click="handleUrge">催办</el-button>
          <el-button type="danger" @click="handleWithdraw">撤回</el-button>
        </template>
      </div>
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
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import { getApprovalDetail, approveApproval, rejectApproval, urgeApproval, withdrawApproval } from '@/api/approval'

const router = useRouter()
const route = useRoute()

const approval = ref(null)
const businessDetail = ref(null)
const records = ref([])

const isApprover = computed(() => {
  // 这里可以根据实际业务逻辑判断是否是审批人
  // 暂时返回 false，表示显示催办和撤回按钮
  return false
})

const approveDialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const approveOpinion = ref('')
const rejectOpinion = ref('')

const fetchData = async () => {
  const id = route.query.id
  if (!id) {
    ElMessage.error('参数错误')
    return
  }
  try {
    const res = await getApprovalDetail(id)
    approval.value = res.data.approval
    businessDetail.value = res.data.businessDetail
    records.value = res.data.records
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
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

const getRecordType = (action) => {
  const map = {
    'SUBMIT': 'primary',
    'APPROVE': 'success',
    'REJECT': 'danger',
    'CANCEL': 'info',
    'URGE': 'warning',
    'WITHDRAW': 'info'
  }
  return map[action] || ''
}

const getRecordActionLabel = (action) => {
  const map = {
    'SUBMIT': '提交申请',
    'APPROVE': '审批通过',
    'REJECT': '审批驳回',
    'CANCEL': '撤销申请',
    'URGE': '催办',
    'WITHDRAW': '撤回申请',
    'RESUBMIT': '重新提交'
  }
  return map[action] || action
}

const getNodeLabel = (node) => {
  const map = {
    'DEPT_MANAGER': '部门经理审批',
    'HR': '人事审批',
    'FINANCE': '财务审批',
    'GM': '总经理审批'
  }
  return map[node] || node
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const goBack = () => {
  router.back()
}

const handleApprove = () => {
  approveOpinion.value = ''
  approveDialogVisible.value = true
}

const handleReject = () => {
  rejectOpinion.value = ''
  rejectDialogVisible.value = true
}

const confirmApprove = async () => {
  try {
    await approveApproval(approval.value.id, approveOpinion.value)
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
    await rejectApproval(approval.value.id, rejectOpinion.value)
    ElMessage.success('已驳回')
    rejectDialogVisible.value = false
    fetchData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleUrge = async () => {
  try {
    await ElMessageBox.confirm('确认要催办该审批吗？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await urgeApproval(approval.value.id)
    ElMessage.success('催办成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('催办失败')
    }
  }
}

const handleWithdraw = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请输入撤回原因', '撤回申请', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '撤回原因不能为空'
    })
    await withdrawApproval(approval.value.id, value)
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
.approval-detail {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 16px;
    font-weight: 500;
  }

  .record-item {
    padding: 12px;
    background: #f5f7fa;
    border-radius: 8px;

    .record-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 8px;

      .record-user {
        display: flex;
        align-items: center;
        gap: 4px;
        color: #606266;
        font-size: 14px;
      }
    }

    .record-node {
      color: #909399;
      font-size: 13px;
      margin-bottom: 8px;
    }

    .record-opinion {
      background: #fff;
      border-radius: 6px;
      padding: 10px 12px;
      border-left: 3px solid #409eff;

      .opinion-label {
        font-size: 13px;
        color: #909399;
        margin-bottom: 4px;
      }

      .opinion-content {
        color: #303133;
        font-size: 14px;
        line-height: 1.6;
      }
    }
  }

  .action-buttons {
    margin-top: 20px;
    text-align: center;
  }

  .search-form {
    background: #f5f7fa;
    padding: 16px;
    border-radius: 8px;
  }
}
</style>
