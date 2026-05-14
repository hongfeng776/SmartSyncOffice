<template>
  <div class="employee-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card>
          <template #header>
            <span>部门树</span>
          </template>
          <el-tree
            ref="deptTreeRef"
            :data="deptTree"
            node-key="id"
            :props="{ label: 'deptName', children: 'children' }"
            default-expand-all
            :highlight-current="true"
            @node-click="handleDeptClick"
          />
        </el-card>
      </el-col>
      <el-col :span="18">
        <el-card>
          <template #header>
            <div class="card-header flex-between">
              <span>{{ currentDept ? currentDept.deptName + ' - 员工列表' : '员工列表' }}</span>
              <div>
                <el-button
                  type="warning"
                  :icon="Transfer"
                  @click="handleBatchDept"
                  :disabled="selectedIds.length === 0"
                >
                  批量调整部门
                </el-button>
                <el-button type="primary" :icon="Plus" @click="handleAdd">
                  新增员工
                </el-button>
              </div>
            </div>
          </template>

          <el-form :inline="true" :model="queryParams" class="search-form">
            <el-form-item label="部门">
              <el-tree-select
                v-model="queryParams.deptId"
                :data="deptTree"
                :props="{ label: 'deptName', value: 'id', children: 'children' }"
                placeholder="请选择部门"
                check-strictly
                clearable
                default-expand-all
                @change="handleSearch"
                style="width: 200px"
              />
            </el-form-item>
            <el-form-item label="姓名">
              <el-input
                v-model="queryParams.keyword"
                placeholder="请输入姓名搜索"
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

          <el-table
            :data="employeeList"
            v-loading="loading"
            border
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="empNo" label="员工编号" width="120" />
            <el-table-column prop="realName" label="姓名" width="100" />
            <el-table-column prop="deptName" label="所属部门" width="150" />
            <el-table-column prop="position" label="职位" width="120" />
            <el-table-column prop="phone" label="联系电话" width="130" />
            <el-table-column prop="email" label="邮箱" />
            <el-table-column prop="entryDate" label="入职日期" width="120" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small">
                  {{ scope.row.status === 1 ? '在职' : '离职' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="scope">
                <el-button link type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
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
            @size-change="getEmployeeList"
            @current-change="getEmployeeList"
          />
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="employeeForm" :rules="employeeRules" ref="employeeFormRef" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="员工编号" prop="empNo">
              <el-input v-model="employeeForm.empNo" placeholder="请输入员工编号" :disabled="!!employeeForm.id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属部门" prop="deptId">
              <el-tree-select
                v-model="employeeForm.deptId"
                :data="deptTree"
                :props="{ label: 'deptName', value: 'id', children: 'children' }"
                placeholder="请选择部门"
                check-strictly
                default-expand-all
                clearable
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="employeeForm.realName" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="employeeForm.gender">
                <el-radio :label="1">男</el-radio>
                <el-radio :label="2">女</el-radio>
                <el-radio :label="0">未知</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="职位" prop="position">
              <el-input v-model="employeeForm.position" placeholder="请输入职位" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入职日期" prop="entryDate">
              <el-date-picker
                v-model="employeeForm.entryDate"
                type="date"
                placeholder="请选择入职日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="employeeForm.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="employeeForm.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="employeeForm.idCard" placeholder="请输入身份证号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出生日期" prop="birthday">
              <el-date-picker
                v-model="employeeForm.birthday"
                type="date"
                placeholder="请选择出生日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工作地点" prop="workPlace">
              <el-input v-model="employeeForm.workPlace" placeholder="请输入工作地点" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="employeeForm.status">
                <el-radio :label="1">在职</el-radio>
                <el-radio :label="0">离职</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="employeeForm.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchDeptDialogVisible" title="批量调整部门" width="500px">
      <el-form :model="batchDeptForm" label-width="100px">
        <el-form-item label="已选择员工">
          <el-tag
            v-for="emp in selectedEmployees"
            :key="emp.id"
            closable
            @close="removeEmployee(emp.id)"
            style="margin: 5px"
          >
            {{ emp.realName }}
          </el-tag>
        </el-form-item>
        <el-form-item label="目标部门" required>
          <el-tree-select
            v-model="batchDeptForm.newDeptId"
            :data="deptTree"
            :props="{ label: 'deptName', value: 'id', children: 'children' }"
            placeholder="请选择目标部门"
            check-strictly
            default-expand-all
            clearable
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDeptDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBatchDept" :disabled="!batchDeptForm.newDeptId">
          确定调整
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Transfer } from '@element-plus/icons-vue'
import {
  getDeptTree,
  getEmployeePage,
  createEmployee,
  updateEmployee,
  deleteEmployee,
  batchUpdateDept
} from '@/api/org'

const deptTreeRef = ref(null)
const deptTree = ref([])
const currentDept = ref(null)
const loading = ref(false)
const employeeList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const batchDeptDialogVisible = ref(false)
const dialogTitle = ref('')
const employeeFormRef = ref(null)
const selectedIds = ref([])
const selectedRows = ref([])

const STORAGE_KEY = 'employee_selected_dept'

const queryParams = reactive({
  page: 1,
  size: 10,
  deptId: null,
  keyword: ''
})

const employeeForm = reactive({
  id: null,
  empNo: '',
  deptId: null,
  realName: '',
  gender: 1,
  birthday: null,
  idCard: '',
  phone: '',
  email: '',
  position: '',
  entryDate: null,
  workPlace: '',
  status: 1,
  remark: ''
})

const batchDeptForm = reactive({
  newDeptId: null
})

const selectedEmployees = computed(() => {
  return selectedRows.value
})

const employeeRules = {
  empNo: [{ required: true, message: '请输入员工编号', trigger: 'blur' }],
  deptId: [{ required: true, message: '请选择所属部门', trigger: 'change' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

function findDeptById(tree, id) {
  for (const node of tree) {
    if (node.id === id) {
      return node
    }
    if (node.children && node.children.length > 0) {
      const found = findDeptById(node.children, id)
      if (found) {
        return found
      }
    }
  }
  return null
}

async function loadDeptTree() {
  const res = await getDeptTree()
  deptTree.value = res.data
  
  // 从 localStorage 恢复选中状态
  const savedDeptId = localStorage.getItem(STORAGE_KEY)
  if (savedDeptId) {
    const deptId = parseInt(savedDeptId)
    const dept = findDeptById(deptTree.value, deptId)
    if (dept) {
      nextTick(() => {
        deptTreeRef.value?.setCurrentKey(deptId)
      })
      currentDept.value = dept
      queryParams.deptId = deptId
    }
  }
}

async function getEmployeeList() {
  loading.value = true
  try {
    const res = await getEmployeePage(queryParams)
    const list = res.data.list
    const totalCount = res.data.total
    
    // 处理最后一页为空的情况：如果当前页数据为空且不是第一页，跳回上一页
    if ((!list || list.length === 0) && queryParams.page > 1) {
      const totalPages = Math.ceil(totalCount / queryParams.size)
      queryParams.page = Math.max(1, totalPages)
      // 如果跳回上一页，需要重新请求数据
      if (queryParams.page !== 1) {
        const resRetry = await getEmployeePage(queryParams)
        employeeList.value = resRetry.data.list
        total.value = resRetry.data.total
      } else {
        employeeList.value = []
        total.value = totalCount
      }
    } else {
      employeeList.value = list
      total.value = totalCount
    }
  } finally {
    loading.value = false
  }
}

function handleDeptClick(data) {
  currentDept.value = data
  queryParams.deptId = data.id
  queryParams.page = 1
  // 保存选中状态到 localStorage
  localStorage.setItem(STORAGE_KEY, data.id.toString())
  getEmployeeList()
}

function handleSearch() {
  queryParams.page = 1
  selectedIds.value = []
  selectedRows.value = []
  getEmployeeList()
}

function handleReset() {
  queryParams.keyword = ''
  currentDept.value = null
  queryParams.deptId = null
  selectedIds.value = []
  selectedRows.value = []
  handleSearch()
}

function handleSelectionChange(rows) {
  selectedRows.value = rows
  selectedIds.value = rows.map(row => row.id)
}

function handleAdd() {
  dialogTitle.value = '新增员工'
  Object.assign(employeeForm, {
    id: null,
    empNo: '',
    deptId: currentDept.value ? currentDept.value.id : null,
    realName: '',
    gender: 1,
    birthday: null,
    idCard: '',
    phone: '',
    email: '',
    position: '',
    entryDate: null,
    workPlace: '',
    status: 1,
    remark: ''
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑员工'
  Object.assign(employeeForm, {
    id: row.id,
    empNo: row.empNo,
    deptId: row.deptId,
    realName: row.realName,
    gender: row.gender,
    birthday: row.birthday,
    idCard: row.idCard,
    phone: row.phone,
    email: row.email,
    position: row.position,
    entryDate: row.entryDate,
    workPlace: row.workPlace,
    status: row.status,
    remark: row.remark
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  await employeeFormRef.value.validate(async (valid) => {
    if (!valid) return
    if (employeeForm.id) {
      await handleUpdate()
    } else {
      await handleCreate()
    }
  })
}

async function handleCreate() {
  await createEmployee(employeeForm)
  ElMessage.success('创建成功')
  dialogVisible.value = false
  getEmployeeList()
}

async function handleUpdate() {
  await updateEmployee(employeeForm)
  ElMessage.success('更新成功')
  dialogVisible.value = false
  getEmployeeList()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定要删除员工【${row.realName}】吗？`, '提示', {
    type: 'warning'
  })
  await deleteEmployee(row.id)
  ElMessage.success('删除成功')
  getEmployeeList()
}

function handleBatchDept() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择要调整部门的员工')
    return
  }
  batchDeptForm.newDeptId = null
  batchDeptDialogVisible.value = true
}

function removeEmployee(id) {
  selectedRows.value = selectedRows.value.filter(row => row.id !== id)
  selectedIds.value = selectedIds.value.filter(i => i !== id)
}

async function confirmBatchDept() {
  if (!batchDeptForm.newDeptId) {
    ElMessage.warning('请选择目标部门')
    return
  }
  await batchUpdateDept({
    newDeptId: batchDeptForm.newDeptId,
    employeeIds: selectedIds.value
  })
  ElMessage.success('批量调整部门成功')
  batchDeptDialogVisible.value = false
  selectedIds.value = []
  selectedRows.value = []
  // 调整部门后，当前页可能没有数据了，重置到第一页
  queryParams.page = 1
  await getEmployeeList()
}

onMounted(() => {
  loadDeptTree()
  getEmployeeList()
})
</script>

<style lang="scss" scoped>
.employee-container {
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

  .search-form {
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    justify-content: flex-end;
  }
}
</style>
