<template>
  <div class="dept-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header flex-between">
              <span>部门树</span>
              <el-button type="primary" :icon="Plus" size="small" @click="handleAddRoot">
                新增根部门
              </el-button>
            </div>
          </template>
          <el-tree
            ref="deptTreeRef"
            :data="deptTree"
            node-key="id"
            :props="{ label: 'deptName', children: 'children' }"
            default-expand-all
            :expand-on-click-node="false"
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <span class="tree-node">
                <span>{{ data.deptName }}</span>
                <span class="tree-actions">
                  <el-button link type="primary" size="small" :icon="Plus" @click.stop="handleAdd(data)">
                    新增
                  </el-button>
                  <el-button link type="primary" size="small" :icon="Edit" @click.stop="handleEdit(data)">
                    编辑
                  </el-button>
                  <el-button link type="danger" size="small" :icon="Delete" @click.stop="handleDelete(data)">
                    删除
                  </el-button>
                </span>
              </span>
            </template>
          </el-tree>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>{{ currentDept ? currentDept.deptName : '部门详情' }}</span>
            </div>
          </template>
          <el-empty v-if="!currentDept" description="请选择左侧部门查看详情" />
          <el-descriptions v-else :column="2" border>
            <el-descriptions-item label="部门名称">
              {{ currentDept.deptName }}
            </el-descriptions-item>
            <el-descriptions-item label="部门编码">
              {{ currentDept.deptCode || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="负责人">
              {{ currentDept.leader || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="排序">
              {{ currentDept.sortOrder }}
            </el-descriptions-item>
            <el-descriptions-item label="联系电话">
              {{ currentDept.phone || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="邮箱">
              {{ currentDept.email || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="currentDept.status === 1 ? 'success' : 'danger'">
                {{ currentDept.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ currentDept.createTime }}
            </el-descriptions-item>
          </el-descriptions>

          <el-divider>下属员工</el-divider>
          <el-table :data="deptEmployees" v-loading="empLoading" border>
            <el-table-column prop="empNo" label="员工编号" width="120" />
            <el-table-column prop="realName" label="姓名" width="100" />
            <el-table-column prop="position" label="职位" width="120" />
            <el-table-column prop="phone" label="联系电话" width="130" />
            <el-table-column prop="email" label="邮箱" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small">
                  {{ scope.row.status === 1 ? '在职' : '离职' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="deptForm" :rules="deptRules" ref="deptFormRef" label-width="80px">
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select
            v-model="deptForm.parentId"
            :data="deptTree"
            :props="{ label: 'deptName', value: 'id', children: 'children' }"
            placeholder="请选择上级部门"
            check-strictly
            clearable
            default-expand-all
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="deptForm.deptName" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="部门编码" prop="deptCode">
          <el-input v-model="deptForm.deptCode" placeholder="请输入部门编码" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="deptForm.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="负责人" prop="leader">
          <el-input v-model="deptForm.leader" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="deptForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="deptForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="deptForm.status">
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
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getDeptTree, createDept, updateDept, deleteDept, getEmployeesByDept } from '@/api/org'

const deptTreeRef = ref(null)
const deptTree = ref([])
const currentDept = ref(null)
const deptEmployees = ref([])
const empLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const deptFormRef = ref(null)

const deptForm = reactive({
  id: null,
  parentId: 0,
  deptName: '',
  deptCode: '',
  sortOrder: 0,
  leader: '',
  phone: '',
  email: '',
  status: 1
})

const deptRules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

async function loadDeptTree() {
  const res = await getDeptTree()
  deptTree.value = res.data
}

async function handleNodeClick(data) {
  currentDept.value = data
  await loadDeptEmployees(data.id)
}

async function loadDeptEmployees(deptId) {
  empLoading.value = true
  try {
    const res = await getEmployeesByDept(deptId)
    deptEmployees.value = res.data
  } finally {
    empLoading.value = false
  }
}

function handleAddRoot() {
  dialogTitle.value = '新增根部门'
  Object.assign(deptForm, {
    id: null,
    parentId: 0,
    deptName: '',
    deptCode: '',
    sortOrder: 0,
    leader: '',
    phone: '',
    email: '',
    status: 1
  })
  dialogVisible.value = true
}

function handleAdd(data) {
  dialogTitle.value = '新增子部门'
  Object.assign(deptForm, {
    id: null,
    parentId: data.id,
    deptName: '',
    deptCode: '',
    sortOrder: 0,
    leader: '',
    phone: '',
    email: '',
    status: 1
  })
  dialogVisible.value = true
}

function handleEdit(data) {
  dialogTitle.value = '编辑部门'
  Object.assign(deptForm, {
    id: data.id,
    parentId: data.parentId,
    deptName: data.deptName,
    deptCode: data.deptCode,
    sortOrder: data.sortOrder,
    leader: data.leader,
    phone: data.phone,
    email: data.email,
    status: data.status
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  await deptFormRef.value.validate(async (valid) => {
    if (!valid) return
    if (deptForm.id) {
      await handleUpdate()
    } else {
      await handleCreate()
    }
  })
}

async function handleCreate() {
  try {
    await createDept(deptForm)
    ElMessage.success('创建成功')
    dialogVisible.value = false
    await loadDeptTree()
  } catch (error) {
    // 后端校验错误会在这里被捕获并显示
    console.error('创建部门失败:', error)
  }
}

async function handleUpdate() {
  try {
    await updateDept(deptForm)
    ElMessage.success('更新成功')
    dialogVisible.value = false
    await loadDeptTree()
    if (currentDept.value && currentDept.value.id === deptForm.id) {
      currentDept.value = { ...currentDept.value, ...deptForm }
    }
  } catch (error) {
    // 后端校验错误会在这里被捕获并显示
    console.error('更新部门失败:', error)
  }
}

async function handleDelete(data) {
  await ElMessageBox.confirm(`确定要删除部门【${data.deptName}】吗？`, '提示', {
    type: 'warning'
  })
  await deleteDept(data.id)
  ElMessage.success('删除成功')
  await loadDeptTree()
  if (currentDept.value && currentDept.value.id === data.id) {
    currentDept.value = null
    deptEmployees.value = []
  }
}

onMounted(() => {
  loadDeptTree()
})
</script>

<style lang="scss" scoped>
.dept-container {
  .card-header {
    font-weight: 600;
    font-size: 15px;
    color: #303133;
  }

  .flex-between {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .tree-node {
    flex: 1;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-right: 8px;

    .tree-actions {
      display: none;
    }
  }

  :deep(.el-tree-node__content:hover) {
    .tree-actions {
      display: inline-block;
    }
  }
}
</style>
