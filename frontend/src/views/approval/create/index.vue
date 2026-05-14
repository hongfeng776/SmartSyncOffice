<template>
  <div class="approval-create">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>发起审批</span>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="请假申请" name="leave">
          <el-form :model="leaveForm" label-width="100px" style="max-width: 600px">
            <el-form-item label="请假类型">
              <el-select v-model="leaveForm.leaveType" placeholder="请选择请假类型">
                <el-option label="事假" value="事假" />
                <el-option label="病假" value="病假" />
                <el-option label="年假" value="年假" />
                <el-option label="婚假" value="婚假" />
                <el-option label="产假" value="产假" />
              </el-select>
            </el-form-item>
            <el-form-item label="开始时间">
              <el-date-picker
                v-model="leaveForm.startTime"
                type="datetime"
                placeholder="选择开始时间"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="结束时间">
              <el-date-picker
                v-model="leaveForm.endTime"
                type="datetime"
                placeholder="选择结束时间"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="请假原因">
              <el-input
                v-model="leaveForm.reason"
                type="textarea"
                :rows="4"
                placeholder="请输入请假原因"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitLeave">提交申请</el-button>
              <el-button @click="resetLeaveForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="加班申请" name="overtime">
          <el-form :model="overtimeForm" label-width="100px" style="max-width: 600px">
            <el-form-item label="加班类型">
              <el-select v-model="overtimeForm.overtimeType" placeholder="请选择加班类型">
                <el-option label="工作日加班" value="工作日加班" />
                <el-option label="周末加班" value="周末加班" />
                <el-option label="节假日加班" value="节假日加班" />
              </el-select>
            </el-form-item>
            <el-form-item label="开始时间">
              <el-date-picker
                v-model="overtimeForm.startTime"
                type="datetime"
                placeholder="选择开始时间"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="结束时间">
              <el-date-picker
                v-model="overtimeForm.endTime"
                type="datetime"
                placeholder="选择结束时间"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="加班原因">
              <el-input
                v-model="overtimeForm.reason"
                type="textarea"
                :rows="4"
                placeholder="请输入加班原因"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitOvertime">提交申请</el-button>
              <el-button @click="resetOvertimeForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="报销申请" name="reimbursement">
          <el-form :model="reimbursementForm" label-width="100px" style="max-width: 600px">
            <el-form-item label="费用类型">
              <el-select v-model="reimbursementForm.expenseType" placeholder="请选择费用类型">
                <el-option label="差旅费" value="差旅费" />
                <el-option label="招待费" value="招待费" />
                <el-option label="办公费" value="办公费" />
                <el-option label="交通费" value="交通费" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
            <el-form-item label="费用日期">
              <el-date-picker
                v-model="reimbursementForm.expenseDate"
                type="date"
                placeholder="选择费用日期"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="总金额">
              <el-input-number v-model="reimbursementForm.totalAmount" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
            <el-form-item label="费用说明">
              <el-input
                v-model="reimbursementForm.reason"
                type="textarea"
                :rows="4"
                placeholder="请输入费用说明"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitReimbursement">提交申请</el-button>
              <el-button @click="resetReimbursementForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createLeaveApproval, createOvertimeApproval, createReimbursementApproval, submitApproval } from '@/api/approval'

const activeTab = ref('leave')

const leaveForm = ref({
  leaveType: '',
  startTime: '',
  endTime: '',
  reason: ''
})

const overtimeForm = ref({
  overtimeType: '',
  startTime: '',
  endTime: '',
  reason: ''
})

const reimbursementForm = ref({
  expenseType: '',
  expenseDate: '',
  totalAmount: 0,
  reason: ''
})

const handleTabChange = () => {
}

const submitLeave = async () => {
  if (!leaveForm.value.leaveType || !leaveForm.value.startTime || !leaveForm.value.endTime || !leaveForm.value.reason) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    const res = await createLeaveApproval(leaveForm.value)
    await submitApproval(res.data.id)
    ElMessage.success('申请提交成功')
    resetLeaveForm()
  } catch (error) {
    ElMessage.error('提交失败')
  }
}

const submitOvertime = async () => {
  if (!overtimeForm.value.overtimeType || !overtimeForm.value.startTime || !overtimeForm.value.endTime || !overtimeForm.value.reason) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    const res = await createOvertimeApproval(overtimeForm.value)
    await submitApproval(res.data.id)
    ElMessage.success('申请提交成功')
    resetOvertimeForm()
  } catch (error) {
    ElMessage.error('提交失败')
  }
}

const submitReimbursement = async () => {
  if (!reimbursementForm.value.expenseType || !reimbursementForm.value.expenseDate || !reimbursementForm.value.totalAmount || !reimbursementForm.value.reason) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    const res = await createReimbursementApproval(reimbursementForm.value)
    await submitApproval(res.data.id)
    ElMessage.success('申请提交成功')
    resetReimbursementForm()
  } catch (error) {
    ElMessage.error('提交失败')
  }
}

const resetLeaveForm = () => {
  leaveForm.value = {
    leaveType: '',
    startTime: '',
    endTime: '',
    reason: ''
  }
}

const resetOvertimeForm = () => {
  overtimeForm.value = {
    overtimeType: '',
    startTime: '',
    endTime: '',
    reason: ''
  }
}

const resetReimbursementForm = () => {
  reimbursementForm.value = {
    expenseType: '',
    expenseDate: '',
    totalAmount: 0,
    reason: ''
  }
}
</script>

<style scoped lang="scss">
.approval-create {
  .card-header {
    font-size: 16px;
    font-weight: 500;
  }
}
</style>
