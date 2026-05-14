<template>
  <div class="task-create">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>发布任务</span>
        </div>
      </template>

      <el-form :model="taskForm" label-width="100px" style="max-width: 800px">
        <el-form-item label="任务标题" required>
          <el-input v-model="taskForm.title" placeholder="请输入任务标题" maxlength="200" show-word-limit />
        </el-form-item>

        <el-form-item label="任务描述">
          <el-input
            v-model="taskForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入任务描述"
          />
        </el-form-item>

        <el-form-item label="任务类型">
          <el-select v-model="taskForm.taskType" placeholder="请选择任务类型" style="width: 100%">
            <el-option label="开发任务" value="开发任务" />
            <el-option label="测试任务" value="测试任务" />
            <el-option label="运维任务" value="运维任务" />
            <el-option label="文档任务" value="文档任务" />
            <el-option label="其他任务" value="其他任务" />
          </el-select>
        </el-form-item>

        <el-form-item label="优先级">
          <el-radio-group v-model="taskForm.priority">
            <el-radio :label="1">低</el-radio>
            <el-radio :label="2">中</el-radio>
            <el-radio :label="3">高</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="执行人">
          <el-select v-model="taskForm.assigneeId" placeholder="请选择执行人" style="width: 100%" filterable>
            <el-option
              v-for="user in userList"
              :key="user.id"
              :label="user.realName"
              :value="user.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="截止时间">
          <el-date-picker
            v-model="taskForm.deadline"
            type="datetime"
            placeholder="选择截止时间"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="taskForm.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注信息"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitTask">发布任务</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { createTask } from '@/api/task'
import request from '@/utils/request'

const taskForm = ref({
  title: '',
  description: '',
  taskType: '',
  priority: 2,
  assigneeId: null,
  deadline: '',
  remark: ''
})

const userList = ref([])

const loadUserList = async () => {
  try {
    const res = await request({
      url: '/api/users',
      method: 'get'
    })
    userList.value = res.data || []
  } catch (error) {
    console.error('加载用户列表失败:', error)
  }
}

const submitTask = async () => {
  if (!taskForm.value.title) {
    ElMessage.warning('请输入任务标题')
    return
  }
  try {
    await createTask(taskForm.value)
    ElMessage.success('任务发布成功')
    resetForm()
  } catch (error) {
    ElMessage.error('任务发布失败')
  }
}

const resetForm = () => {
  taskForm.value = {
    title: '',
    description: '',
    taskType: '',
    priority: 2,
    assigneeId: null,
    deadline: '',
    remark: ''
  }
}

onMounted(() => {
  loadUserList()
})
</script>

<style scoped lang="scss">
.task-create {
  .card-header {
    font-size: 16px;
    font-weight: 500;
  }
}
</style>
