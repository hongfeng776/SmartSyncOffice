<template>
  <div class="file-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header flex-between">
          <span>文件管理</span>
          <div class="header-actions">
            <el-radio-group v-model="currentFileType" size="small" @change="handleTypeChange">
              <el-radio-button label="">全部</el-radio-button>
              <el-radio-button label="image">图片</el-radio-button>
              <el-radio-button label="document">文档</el-radio-button>
              <el-radio-button label="video">视频</el-radio-button>
            </el-radio-group>
            <el-button type="primary" :icon="Upload" @click="handleUploadClick">
              上传文件
            </el-button>
          </div>
        </div>
      </template>

      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索文件名"
          size="small"
          style="width: 300px"
          :prefix-icon="Search"
          clearable
          @keyup.enter="loadFileList"
        />
      </div>

      <el-table :data="fileList" v-loading="loading" style="width: 100%">
        <el-table-column prop="originalFilename" label="文件名" min-width="200">
          <template #default="scope">
            <div class="file-name-cell" @click="handlePreview(scope.row)">
              <el-icon class="file-icon" :color="getFileIconColor(scope.row.fileType)">
                <component :is="getFileIcon(scope.row.fileType)" />
              </el-icon>
              <span>{{ scope.row.originalFilename }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="fileSize" label="大小" width="100" />
        <el-table-column prop="fileType" label="类型" width="100">
          <template #default="scope">
            <el-tag :type="getFileTagType(scope.row.fileType)" size="small">
              {{ getFileTypeText(scope.row.fileType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="uploaderName" label="上传者" width="120" />
        <el-table-column prop="downloadCount" label="下载次数" width="100" />
        <el-table-column prop="createTime" label="上传时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="handlePreview(scope.row)">
              预览
            </el-button>
            <el-button type="success" link size="small" @click="handleDownload(scope.row)">
              下载
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadFileList"
          @current-change="loadFileList"
        />
      </div>
    </el-card>

    <el-dialog v-model="uploadDialogVisible" title="上传文件" width="600px">
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :multiple="true"
        :limit="10"
        :on-change="handleFileChange"
        :on-remove="handleFileRemove"
        drag
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持图片、文档、视频等格式，单个文件最大500MB，超过10MB的文件将后台异步处理
          </div>
        </template>
      </el-upload>

      <div class="file-list" v-if="fileListToUpload.length > 0">
        <div v-for="(file, index) in fileListToUpload" :key="index" class="file-item">
          <span class="file-name">{{ file.name }}</span>
          <el-progress
            v-if="file.percentage !== undefined"
            :percentage="file.percentage"
            :status="file.status"
            style="width: 200px"
          />
          <span class="file-size">{{ formatFileSize(file.size) }}</span>
        </div>
      </div>

      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitUpload" :loading="uploading">
          开始上传
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewDialogVisible" title="文件预览" width="80%" top="5vh">
      <div class="preview-container">
        <template v-if="previewLoading">
          <div class="loading-preview">
            <el-icon class="is-loading" size="40"><Loading /></el-icon>
            <p>加载中...</p>
          </div>
        </template>
        <template v-else-if="currentPreviewFile">
          <img
            v-if="currentPreviewFile.fileType === 'image'"
            :src="previewUrl"
            class="preview-image"
            alt="预览"
          />
          <video
            v-else-if="currentPreviewFile.fileType === 'video'"
            :src="previewUrl"
            class="preview-video"
            controls
          >
            您的浏览器不支持视频播放
          </video>
          <iframe
            v-else-if="currentPreviewFile.fileType === 'document'"
            :src="previewUrl"
            class="preview-document"
          />
          <div v-else class="preview-unsupported">
            <el-icon size="60" color="#909399"><Document /></el-icon>
            <p>该文件类型暂不支持在线预览，请下载后查看</p>
            <el-button type="primary" @click="handleDownload(currentPreviewFile)">
              下载文件
            </el-button>
          </div>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h } from 'vue'
import {
  Upload,
  Search,
  Picture,
  Document,
  VideoCamera,
  FolderOpened,
  UploadFilled,
  Loading
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  uploadFile,
  uploadFileAsync,
  downloadFile,
  deleteFile,
  getFileList,
  getFilePreviewUrl
} from '@/api/file'

const uploadRef = ref(null)
const uploadDialogVisible = ref(false)
const previewDialogVisible = ref(false)
const loading = ref(false)
const uploading = ref(false)
const previewLoading = ref(false)
const fileList = ref([])
const fileListToUpload = ref([])
const searchKeyword = ref('')
const currentFileType = ref('')
const currentPreviewFile = ref(null)
const previewUrl = ref('')
const total = ref(0)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  fileType: ''
})

onMounted(() => {
  loadFileList()
})

const loadFileList = () => {
  loading.value = true
  queryParams.keyword = searchKeyword.value
  getFileList(queryParams).then(response => {
    if (response.code === 200) {
      fileList.value = response.data.records
      total.value = response.data.total
    }
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

const handleUploadClick = () => {
  fileListToUpload.value = []
  uploadDialogVisible.value = true
}

const handleFileChange = (file) => {
  file.percentage = 0
  file.status = undefined
  fileListToUpload.value.push(file)
}

const handleFileRemove = (file) => {
  const index = fileListToUpload.value.findIndex(f => f.uid === file.uid)
  if (index > -1) {
    fileListToUpload.value.splice(index, 1)
  }
}

const submitUpload = async () => {
  if (fileListToUpload.value.length === 0) {
    ElMessage.warning('请选择要上传的文件')
    return
  }

  uploading.value = true

  for (let i = 0; i < fileListToUpload.value.length; i++) {
    const file = fileListToUpload.value[i]
    try {
      const onProgress = (event) => {
        if (event.total > 0) {
          file.percentage = Math.round((event.loaded * 100) / event.total)
        }
      }

      let response
      if (file.size > 10 * 1024 * 1024) {
        response = await uploadFileAsync(file.raw, onProgress)
      } else {
        response = await uploadFile(file.raw, onProgress)
      }

      if (response.code === 200) {
        file.percentage = 100
        file.status = 'success'
      } else {
        file.status = 'exception'
      }
    } catch (error) {
      file.status = 'exception'
    }
  }

  uploading.value = false
  ElMessage.success('上传任务已完成')
  loadFileList()

  setTimeout(() => {
    uploadDialogVisible.value = false
  }, 1000)
}

const handlePreview = async (file) => {
  currentPreviewFile.value = file
  previewDialogVisible.value = true
  previewLoading.value = true

  try {
    const response = await getFilePreviewUrl(file.id)
    if (response.code === 200) {
      previewUrl.value = response.data
    }
  } catch (error) {
    console.error('获取预览地址失败', error)
  } finally {
    previewLoading.value = false
  }
}

const handleDownload = async (file) => {
  try {
    const response = await downloadFile(file.id)
    const blob = new Blob([response])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = file.originalFilename
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch (error) {
    ElMessage.error('下载失败')
  }
}

const handleDelete = (file) => {
  ElMessageBox.confirm(`确定要删除文件"${file.originalFilename}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await deleteFile(file.id)
      if (response.code === 200) {
        ElMessage.success('删除成功')
        loadFileList()
      } else {
        ElMessage.error(response.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const handleTypeChange = () => {
  queryParams.fileType = currentFileType.value
  queryParams.pageNum = 1
  loadFileList()
}

const getFileIcon = (type) => {
  const iconMap = {
    image: Picture,
    document: Document,
    video: VideoCamera,
    audio: VideoCamera,
    other: FolderOpened
  }
  return iconMap[type] || FolderOpened
}

const getFileIconColor = (type) => {
  const colorMap = {
    image: '#67C23A',
    document: '#409EFF',
    video: '#E6A23C',
    audio: '#909399',
    other: '#909399'
  }
  return colorMap[type] || '#909399'
}

const getFileTagType = (type) => {
  const tagMap = {
    image: 'success',
    document: 'primary',
    video: 'warning',
    audio: 'info',
    other: 'info'
  }
  return tagMap[type] || 'info'
}

const getFileTypeText = (type) => {
  const textMap = {
    image: '图片',
    document: '文档',
    video: '视频',
    audio: '音频',
    other: '其他'
  }
  return textMap[type] || '其他'
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i]
}
</script>

<style lang="scss" scoped>
.file-container {
  .card-header {
    font-weight: 600;
    font-size: 15px;
    color: #303133;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-actions {
      display: flex;
      gap: 12px;
      align-items: center;
    }
  }

  .search-bar {
    margin-bottom: 20px;
  }

  .file-name-cell {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;

    &:hover {
      color: #409eff;
    }

    .file-icon {
      font-size: 18px;
    }
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .file-list {
    max-height: 300px;
    overflow-y: auto;
    margin-top: 20px;

    .file-item {
      display: flex;
      align-items: center;
      padding: 10px;
      background: #f5f7fa;
      border-radius: 4px;
      margin-bottom: 10px;

      .file-name {
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        margin-right: 10px;
      }

      .file-size {
        margin-left: 10px;
        color: #909399;
        font-size: 12px;
      }
    }
  }

  .preview-container {
    min-height: 400px;
    display: flex;
    justify-content: center;
    align-items: center;

    .loading-preview {
      text-align: center;
      color: #909399;

      p {
        margin-top: 10px;
      }
    }

    .preview-image {
      max-width: 100%;
      max-height: 600px;
      object-fit: contain;
    }

    .preview-video {
      width: 100%;
      max-height: 600px;
    }

    .preview-document {
      width: 100%;
      height: 600px;
      border: none;
    }

    .preview-unsupported {
      text-align: center;
      color: #909399;

      p {
        margin: 20px 0;
      }
    }
  }
}
</style>
