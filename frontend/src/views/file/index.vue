<template>
  <div class="file-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="never" class="folder-card">
          <template #header>
            <div class="card-header flex-between">
              <span>文件夹</span>
              <el-button type="primary" :icon="Plus" size="small" circle @click="handleCreateFolder" />
            </div>
          </template>

          <el-tree
            ref="folderTreeRef"
            :data="folderTree"
            :props="treeProps"
            node-key="id"
            default-expand-all
            highlight-current
            :expand-on-click-node="false"
            @node-click="handleFolderClick"
          >
            <template #default="{ node, data }">
              <span class="custom-tree-node">
                <el-icon class="folder-icon">
                  <FolderOpened v-if="node.expanded" />
                  <Folder v-else />
                </el-icon>
                <span class="folder-name">{{ node.label }}</span>
                <span class="folder-actions">
                  <el-dropdown trigger="click" @command="(command) => handleFolderAction(command, data)">
                    <el-icon class="action-icon" :size="14"><MoreFilled /></el-icon>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="create">新建子文件夹</el-dropdown-item>
                        <el-dropdown-item command="rename">重命名</el-dropdown-item>
                        <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </span>
              </span>
            </template>
          </el-tree>
        </el-card>
      </el-col>

      <el-col :span="18">
        <el-card shadow="never">
          <template #header>
            <div class="card-header flex-between">
              <div class="header-left">
                <el-breadcrumb separator="/">
                  <el-breadcrumb-item :to="{ path: '/file' }" @click.native="goToRoot">
                    <el-icon><FolderOpened /></el-icon>
                    全部文件
                  </el-breadcrumb-item>
                  <el-breadcrumb-item v-if="currentFolder">
                    {{ currentFolder.folderName }}
                  </el-breadcrumb-item>
                </el-breadcrumb>
              </div>
              <div class="header-actions">
                <el-select v-model="queryParams.fileType" placeholder="按类型筛选" size="small" clearable style="width: 120px" @change="loadFileList">
                  <el-option label="图片" value="image" />
                  <el-option label="文档" value="document" />
                  <el-option label="视频" value="video" />
                  <el-option label="音频" value="audio" />
                  <el-option label="其他" value="other" />
                </el-select>
                <el-button type="primary" :icon="Upload" @click="handleUploadClick">
                  上传文件
                </el-button>
              </div>
            </div>
          </template>

          <div class="search-bar">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索文件名..."
              size="small"
              style="width: 300px"
              :prefix-icon="Search"
              clearable
              @keyup.enter="loadFileList"
            >
              <template #append>
                <el-button :icon="Search" @click="loadFileList" />
              </template>
            </el-input>
            <span class="file-count">共 {{ total }} 个文件</span>
          </div>

          <el-table :data="fileList" v-loading="loading" style="width: 100%">
            <el-table-column prop="originalFilename" label="文件名" min-width="250">
              <template #default="scope">
                <div class="file-name-cell" @click="handlePreview(scope.row)">
                  <el-icon class="file-icon" :color="getFileIconColor(scope.row.fileType)">
                    <component :is="getFileIcon(scope.row.fileType)" />
                  </el-icon>
                  <span class="file-name-text">{{ scope.row.originalFilename }}</span>
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
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="scope">
                <el-button type="primary" link size="small" @click="handlePreview(scope.row)">
                  预览
                </el-button>
                <el-button type="success" link size="small" @click="handleDownload(scope.row)">
                  下载
                </el-button>
                <el-button type="danger" link size="small" @click="handleDeleteFile(scope.row)">
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
      </el-col>
    </el-row>

    <el-dialog v-model="uploadDialogVisible" title="上传文件" width="600px" :close-on-click-modal="false">
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :multiple="true"
        :limit="20"
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
            支持批量上传，单个文件最大500MB，超过10MB的文件将后台异步处理
          </div>
        </template>
      </el-upload>

      <div class="file-list-container" v-if="fileListToUpload.length > 0">
        <div v-for="(file, index) in fileListToUpload" :key="index" class="upload-file-item">
          <div class="file-info">
            <el-icon class="file-icon-small">
              <component :is="getFileIconByName(file.name)" />
            </el-icon>
            <span class="file-name">{{ file.name }}</span>
            <span class="file-size">{{ formatFileSize(file.size) }}</span>
          </div>
          <div class="progress-container" v-if="file.status !== 'success' && file.status !== 'exception'">
            <el-progress
              :percentage="file.percentage || 0"
              :stroke-width="8"
              :show-text="true"
            />
          </div>
          <div class="status-container" v-else>
            <el-tag v-if="file.status === 'success'" type="success" size="small">上传成功</el-tag>
            <el-tag v-else-if="file.status === 'exception'" type="danger" size="small">上传失败</el-tag>
            <el-tag v-else type="info" size="small">等待上传</el-tag>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="uploadDialogVisible = false" :disabled="uploading">取消</el-button>
        <el-button type="primary" @click="submitUpload" :loading="uploading" :disabled="fileListToUpload.length === 0">
          开始上传
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="folderDialogVisible" :title="folderDialogTitle" width="400px">
      <el-form ref="folderFormRef" :model="folderForm" :rules="folderRules" label-width="80px">
        <el-form-item label="文件夹名称" prop="folderName">
          <el-input v-model="folderForm.folderName" placeholder="请输入文件夹名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="folderDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveFolder">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewDialogVisible" title="文件预览" width="80%" top="5vh" :close-on-click-modal="false">
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
            @load="previewLoading = false"
            @error="previewLoading = false"
          />
          <video
            v-else-if="currentPreviewFile.fileType === 'video'"
            :src="previewUrl"
            class="preview-video"
            controls
            preload="metadata"
          >
            您的浏览器不支持视频播放
          </video>
          <template v-else-if="currentPreviewFile.fileType === 'document'">
            <iframe
              v-if="currentPreviewFile.fileExtension === 'pdf'"
              :src="previewUrl + '#toolbar=1&navpanes=0&scrollbar=1'"
              class="preview-document"
              @load="previewLoading = false"
              @error="previewLoading = false"
            />
            <div v-else class="preview-unsupported">
              <el-icon size="60" color="#909399"><Document /></el-icon>
              <p>该文档类型暂不支持在线预览，请下载后查看</p>
              <el-button type="primary" @click="handleDownload(currentPreviewFile)">
                下载文件
              </el-button>
            </div>
          </template>
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
import { ref, reactive, onMounted } from 'vue'
import {
  Upload,
  Search,
  Picture,
  Document,
  VideoCamera,
  FolderOpened,
  Folder,
  UploadFilled,
  Loading,
  Plus,
  MoreFilled,
  Headset
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  uploadFile,
  uploadFileAsync,
  downloadFile,
  deleteFile,
  getFileList,
  getFilePreviewUrl,
  getFolderTree,
  createFolder,
  updateFolder,
  deleteFolder,
  checkChunk,
  uploadChunk,
  mergeChunks
} from '@/api/file'

const uploadRef = ref(null)
const folderTreeRef = ref(null)
const folderFormRef = ref(null)
const uploadDialogVisible = ref(false)
const folderDialogVisible = ref(false)
const previewDialogVisible = ref(false)
const loading = ref(false)
const uploading = ref(false)
const previewLoading = ref(false)
const fileList = ref([])
const fileListToUpload = ref([])
const folderTree = ref([])
const searchKeyword = ref('')
const currentFolder = ref(null)
const currentPreviewFile = ref(null)
const previewUrl = ref('')
const total = ref(0)
const folderDialogTitle = ref('新建文件夹')
const folderActionType = ref('')

const treeProps = {
  label: 'folderName',
  children: 'children'
}

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  fileType: '',
  folderId: null
})

const folderForm = reactive({
  id: null,
  folderName: '',
  parentId: null
})

const folderRules = {
  folderName: [
    { required: true, message: '请输入文件夹名称', trigger: 'blur' }
  ]
}

onMounted(() => {
  loadFolderTree()
  loadFileList()
})

const loadFolderTree = () => {
  getFolderTree().then(response => {
    if (response.code === 200) {
      folderTree.value = response.data || []
    }
  }).catch(() => {})
}

const loadFileList = () => {
  loading.value = true
  queryParams.keyword = searchKeyword.value
  getFileList(queryParams).then(response => {
    if (response.code === 200) {
      fileList.value = response.data.records || []
      total.value = response.data.total || 0
    }
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

const handleFolderClick = (data) => {
  currentFolder.value = data
  queryParams.folderId = data.id
  queryParams.pageNum = 1
  loadFileList()
}

const goToRoot = () => {
  currentFolder.value = null
  queryParams.folderId = null
  folderTreeRef.value?.setCurrentKey(null)
  queryParams.pageNum = 1
  loadFileList()
}

const handleCreateFolder = () => {
  folderActionType.value = 'create'
  folderDialogTitle.value = '新建文件夹'
  folderForm.id = null
  folderForm.folderName = ''
  folderForm.parentId = currentFolder.value?.id || 0
  folderDialogVisible.value = true
}

const handleFolderAction = (command, data) => {
  if (command === 'create') {
    folderActionType.value = 'create'
    folderDialogTitle.value = '新建子文件夹'
    folderForm.id = null
    folderForm.folderName = ''
    folderForm.parentId = data.id
    folderDialogVisible.value = true
  } else if (command === 'rename') {
    folderActionType.value = 'rename'
    folderDialogTitle.value = '重命名文件夹'
    folderForm.id = data.id
    folderForm.folderName = data.folderName
    folderForm.parentId = data.parentId
    folderDialogVisible.value = true
  } else if (command === 'delete') {
    ElMessageBox.confirm(`确定要删除文件夹"${data.folderName}"吗？此操作会删除所有子文件夹。`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      try {
        const response = await deleteFolder(data.id)
        if (response.code === 200) {
          ElMessage.success('删除成功')
          loadFolderTree()
          if (currentFolder.value?.id === data.id) {
            goToRoot()
          }
        } else {
          ElMessage.error(response.message || '删除失败')
        }
      } catch (error) {
        ElMessage.error('删除失败')
      }
    }).catch(() => {})
  }
}

const handleSaveFolder = () => {
  folderFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let response
        if (folderActionType.value === 'create') {
          response = await createFolder(folderForm)
        } else {
          response = await updateFolder(folderForm)
        }
        if (response.code === 200) {
          ElMessage.success(folderActionType.value === 'create' ? '创建成功' : '修改成功')
          folderDialogVisible.value = false
          loadFolderTree()
        } else {
          ElMessage.error(response.message || '操作失败')
        }
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }
  })
}

const handleUploadClick = () => {
  fileListToUpload.value = []
  uploadDialogVisible.value = true
}

const handleFileChange = (file) => {
  file.percentage = 0
  file.status = 'pending'
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
  const folderId = currentFolder.value?.id || null
  const CHUNK_SIZE = 5 * 1024 * 1024

  for (let i = 0; i < fileListToUpload.value.length; i++) {
    const fileItem = fileListToUpload.value[i]
    const file = fileItem.raw

    try {
      if (file.size > 10 * 1024 * 1024) {
        const totalChunks = Math.ceil(file.size / CHUNK_SIZE)
        
        let uploadId = ''
        const checkResponse = await checkChunk('', file.name, file.size, folderId)
        if (checkResponse.code === 200) {
          uploadId = checkResponse.data.uploadId
        }

        let uploadedCount = 0
        for (let chunkIndex = 1; chunkIndex <= totalChunks; chunkIndex++) {
          const start = (chunkIndex - 1) * CHUNK_SIZE
          const end = Math.min(chunkIndex * CHUNK_SIZE, file.size)
          const chunk = file.slice(start, end)

          const formData = new FormData()
          formData.append('file', chunk)
          formData.append('uploadId', uploadId)
          formData.append('chunkNumber', chunkIndex)
          formData.append('totalChunks', totalChunks)
          formData.append('totalSize', file.size)
          formData.append('filename', file.name)
          if (folderId) {
            formData.append('folderId', folderId)
          }

          await uploadChunk(formData)
          uploadedCount++
          fileItem.percentage = Math.round((uploadedCount * 100) / totalChunks)
        }

        const mergeResponse = await mergeChunks(uploadId)
        if (mergeResponse.code === 200) {
          fileItem.percentage = 100
          fileItem.status = 'success'
        } else {
          fileItem.status = 'exception'
        }
      } else {
        const onProgress = (event) => {
          if (event.total > 0) {
            fileItem.percentage = Math.round((event.loaded * 100) / event.total)
          }
        }
        const response = await uploadFile(file, folderId, onProgress)
        if (response.code === 200) {
          fileItem.percentage = 100
          fileItem.status = 'success'
        } else {
          fileItem.status = 'exception'
        }
      }
    } catch (error) {
      console.error('上传失败:', error)
      fileItem.status = 'exception'
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
    previewUrl.value = `/auth/file/stream/${file.id}`
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

const handleDeleteFile = (file) => {
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

const getFileIcon = (type) => {
  const iconMap = {
    image: Picture,
    document: Document,
    video: VideoCamera,
    audio: Headset,
    other: FolderOpened
  }
  return iconMap[type] || FolderOpened
}

const getFileIconByName = (filename) => {
  const ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase()
  const imageExts = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg']
  const docExts = ['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'pdf', 'txt']
  const videoExts = ['mp4', 'avi', 'mov', 'wmv', 'flv', 'mkv']
  const audioExts = ['mp3', 'wav', 'flac', 'aac']

  if (imageExts.includes(ext)) return Picture
  if (docExts.includes(ext)) return Document
  if (videoExts.includes(ext)) return VideoCamera
  if (audioExts.includes(ext)) return Headset
  return FolderOpened
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
  .folder-card {
    height: calc(100vh - 140px);
    overflow-y: auto;
  }

  .card-header {
    font-weight: 600;
    font-size: 15px;
    color: #303133;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-left {
      flex: 1;
    }

    .header-actions {
      display: flex;
      gap: 12px;
      align-items: center;
    }
  }

  .custom-tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    padding-right: 8px;

    .folder-icon {
      margin-right: 8px;
      color: #409eff;
    }

    .folder-name {
      flex: 1;
    }

    .folder-actions {
      opacity: 0;
      transition: opacity 0.2s;

      .action-icon {
        cursor: pointer;
        color: #909399;

        &:hover {
          color: #409eff;
        }
      }
    }

    &:hover .folder-actions {
      opacity: 1;
    }
  }

  .search-bar {
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    gap: 16px;

    .file-count {
      color: #909399;
      font-size: 13px;
    }
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

    .file-name-text {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .file-list-container {
    max-height: 400px;
    overflow-y: auto;
    margin-top: 20px;

    .upload-file-item {
      padding: 12px;
      background: #f5f7fa;
      border-radius: 6px;
      margin-bottom: 10px;

      .file-info {
        display: flex;
        align-items: center;
        margin-bottom: 8px;

        .file-icon-small {
          font-size: 16px;
          margin-right: 8px;
          color: #409eff;
        }

        .file-name {
          flex: 1;
          font-size: 14px;
          color: #303133;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .file-size {
          color: #909399;
          font-size: 12px;
          margin-left: 12px;
        }
      }

      .progress-container {
        width: 100%;
      }

      .status-container {
        text-align: right;
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
