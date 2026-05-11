<template>
  <div class="user-container">
    <el-card>
      <template #header>
        <div class="card-header flex-between">
          <span>用户管理</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">
            新增用户
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索用户名/姓名"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">
            搜索
          </el-button>
          <el-button :icon="Refresh" @click="handleReset">
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <el-table :data="userList" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="真实姓名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'>
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="warning" size="small" @click="handleResetPwd(scope.row)">重置密码</el-button>
            <el-button link type="danger" size="small" @click="handleStatus(scope.row)">
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="getUserList"
        @current-change="getUserList"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="userForm" :rules="userRules" ref="userFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" :disabled="!!userForm.id" />
        </el-form-item>
        <el-form-item v-if="!userForm.id" label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="userForm.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="userForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const userList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const userFormRef = ref(null)

const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: ''
})

const userForm = reactive({
  id: null,
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  status: 1
})

const userRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

async function getUserList() {
  loading.value = true
  try {
    const res = await request.get('/api/users', { params: queryParams })
    userList.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.page = 1
  getUserList()
}

function handleReset() {
  queryParams.keyword = ''
  handleSearch()
}

function handleAdd() {
  dialogTitle.value = '新增用户'
  Object.assign(userForm, {
    id: null,
    username: '',
    password: '',
    realName: '',
    email: '',
    phone: '',
    status: 1
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑用户'
  Object.assign(userForm, {
    id: row.id,
    username: row.username,
    password: '',
    realName: row.realName,
    email: row.email,
    phone: row.phone,
    status: row.status
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  await userFormRef.value.validate(async (valid) => {
    if (!valid) return
    if (userForm.id) {
      await handleUpdate()
    } else {
      await handleCreate()
    }
  })
}

async function handleCreate() {
  await request.post('/api/users', userForm)
  ElMessage.success('创建成功')
  dialogVisible.value = false
  getUserList()
}

async function handleUpdate() {
  await request.put('/api/users', userForm)
  ElMessage.success('更新成功')
  dialogVisible.value = false
  getUserList()
}

async function handleResetPwd(row) {
  await ElMessageBox.confirm(`确定要重置用户【${row.username}】的密码为123456吗？`, '提示', {
    type: 'warning'
  })
  await request.put(`/api/users/${row.id}/reset-password`)
  ElMessage.success('密码重置成功')
}

async function handleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  await request.put(`/api/users/${row.id}/status?status=${newStatus}`)
  ElMessage.success('状态更新成功')
  getUserList()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定要删除用户【${row.username}】吗？`, '提示', {
    type: 'warning'
  })
  await request.delete(`/api/users/${row.id}`)
  ElMessage.success('删除成功')
  getUserList()
}

onMounted(() => {
  getUserList()
})
</script>

<style lang="scss" scoped>
.user-container {
  .card-header {
    font-weight: 600;
    font-size: 15px;
    color: #303133;
  }

  .search-form {
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    justify-content: flex-end;
  }
}
</style>
