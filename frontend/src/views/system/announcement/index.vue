<template>
  <div class="announcement-container">
    <el-card>
      <template #header>
        <div class="card-header flex-between">
          <span>系统公告管理</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">
            新增公告
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="全部" :value="null" />
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已撤回" :value="2" />
          </el-select>
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

      <el-table :data="announcementList" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="scope">
            <el-tag :type="getPriorityType(scope.row.priority)">
              {{ getPriorityLabel(scope.row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="180" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleView(scope.row)">查看</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(scope.row)" v-if="scope.row.status === 0">编辑</el-button>
            <el-button link type="success" size="small" @click="handlePublish(scope.row)" v-if="scope.row.status === 0">发布</el-button>
            <el-button link type="warning" size="small" @click="handleWithdraw(scope.row)" v-if="scope.row.status === 1">撤回</el-button>
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
        @size-change="getAnnouncementList"
        @current-change="getAnnouncementList"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="announcementForm" :rules="announcementRules" ref="announcementFormRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="announcementForm.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-radio-group v-model="announcementForm.priority">
            <el-radio :label="0">普通</el-radio>
            <el-radio :label="1">重要</el-radio>
            <el-radio :label="2">紧急</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="announcementForm.content"
            type="textarea"
            :rows="8"
            placeholder="请输入公告内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存草稿</el-button>
        <el-button type="success" @click="handleSubmitAndPublish">保存并发布</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="viewDialogVisible" title="查看公告" width="700px">
      <div class="announcement-detail">
        <div class="detail-header">
          <h3>{{ currentAnnouncement.title }}</h3>
          <div class="detail-meta">
            <el-tag :type="getPriorityType(currentAnnouncement.priority)" size="small">
              {{ getPriorityLabel(currentAnnouncement.priority) }}
            </el-tag>
            <el-tag :type="getStatusType(currentAnnouncement.status)" size="small" style="margin-left: 10px">
              {{ getStatusLabel(currentAnnouncement.status) }}
            </el-tag>
            <span class="meta-time" style="margin-left: 10px">
              创建时间：{{ currentAnnouncement.createTime }}
            </span>
          </div>
        </div>
        <div class="detail-content">
          {{ currentAnnouncement.content }}
        </div>
      </div>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import {
  getAnnouncementList,
  createAnnouncement,
  updateAnnouncement,
  publishAnnouncement,
  withdrawAnnouncement,
  deleteAnnouncement
} from '@/api/notification'

const loading = ref(false)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const dialogTitle = ref('')
const announcementFormRef = ref(null)
const announcementList = ref([])
const total = ref(0)
const currentAnnouncement = ref({})

const queryParams = reactive({
  page: 1,
  size: 10,
  status: null
})

const announcementForm = reactive({
  id: null,
  title: '',
  content: '',
  priority: 0
})

const announcementRules = {
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { min: 2, max: 200, message: '标题长度在 2 到 200 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' },
    { min: 2, max: 5000, message: '内容长度在 2 到 5000 个字符', trigger: 'blur' }
  ]
}

const getPriorityType = (priority) => {
  const map = { 0: 'info', 1: 'warning', 2: 'danger' }
  return map[priority] || 'info'
}

const getPriorityLabel = (priority) => {
  const map = { 0: '普通', 1: '重要', 2: '紧急' }
  return map[priority] || '普通'
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'warning' }
  return map[status] || 'info'
}

const getStatusLabel = (status) => {
  const map = { 0: '草稿', 1: '已发布', 2: '已撤回' }
  return map[status] || '未知'
}

const getAnnouncementList = async () => {
  loading.value = true
  try {
    const res = await getAnnouncementList(queryParams)
    announcementList.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error('获取公告列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.page = 1
  getAnnouncementList()
}

const handleReset = () => {
  queryParams.status = null
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增公告'
  announcementForm.id = null
  announcementForm.title = ''
  announcementForm.content = ''
  announcementForm.priority = 0
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑公告'
  announcementForm.id = row.id
  announcementForm.title = row.title
  announcementForm.content = row.content
  announcementForm.priority = row.priority
  dialogVisible.value = true
}

const handleView = (row) => {
  currentAnnouncement.value = row
  viewDialogVisible.value = true
}

const handleSubmit = async () => {
  if (!announcementFormRef.value) return
  await announcementFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (announcementForm.id) {
          await updateAnnouncement(announcementForm)
          ElMessage.success('更新成功')
        } else {
          await createAnnouncement(announcementForm)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        getAnnouncementList()
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }
  })
}

const handleSubmitAndPublish = async () => {
  if (!announcementFormRef.value) return
  await announcementFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (announcementForm.id) {
          await updateAnnouncement(announcementForm)
          await publishAnnouncement(announcementForm.id)
        } else {
          const res = await createAnnouncement(announcementForm)
          if (res.data && res.data.id) {
            await publishAnnouncement(res.data.id)
          }
        }
        ElMessage.success('发布成功，通知已批量发送')
        dialogVisible.value = false
        getAnnouncementList()
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }
  })
}

const handlePublish = async (row) => {
  try {
    await ElMessageBox.confirm(`确认发布公告【${row.title}】吗？发布后将通知所有用户`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await publishAnnouncement(row.id)
    ElMessage.success('发布成功，通知已批量发送')
    getAnnouncementList()
  } catch {
  }
}

const handleWithdraw = async (row) => {
  try {
    await ElMessageBox.confirm(`确认撤回公告【${row.title}】吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await withdrawAnnouncement(row.id)
    ElMessage.success('撤回成功')
    getAnnouncementList()
  } catch {
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除公告【${row.title}】吗？删除后不可恢复`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteAnnouncement(row.id)
    ElMessage.success('删除成功')
    getAnnouncementList()
  } catch {
  }
}

getAnnouncementList()
</script>

<style scoped lang="scss">
.announcement-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-form {
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    justify-content: flex-end;
  }

  .announcement-detail {
    .detail-header {
      margin-bottom: 20px;
      padding-bottom: 15px;
      border-bottom: 1px solid #ebeef5;

      h3 {
        margin: 0 0 10px 0;
        font-size: 18px;
        color: #303133;
      }

      .detail-meta {
        display: flex;
        align-items: center;
        color: #909399;
        font-size: 13px;

        .meta-time {
          color: #909399;
        }
      }
    }

    .detail-content {
      line-height: 1.8;
      color: #303133;
      white-space: pre-wrap;
      min-height: 100px;
    }
  }
}
</style>
