<template>
  <div class="notification-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>消息中心</span>
          <div class="header-actions">
            <el-button type="primary" link @click="handleClearRead" :disabled="!hasRead">
              <el-icon><Delete /></el-icon>
              清空已读
            </el-button>
            <el-button type="primary" link @click="handleMarkAllRead">
              <el-icon><Check /></el-icon>
              全部已读
            </el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" type="border-card" @tab-change="handleTabChange">
        <el-tab-pane label="全部消息" name="all">
          <div class="tab-header">
            <span class="tab-tip">所有类型的消息通知</span>
            <el-button type="primary" link size="small" @click="handleMarkTabRead" v-if="unreadCountByTab > 0">
              本类全部已读
            </el-button>
          </div>
        </el-tab-pane>
        
        <el-tab-pane name="SYSTEM">
          <template #label>
            <span class="tab-label">
              <el-icon><Bell /></el-icon>
              系统通知
              <el-badge v-if="unreadCountMap.SYSTEM" :value="unreadCountMap.SYSTEM" :hidden="!unreadCountMap.SYSTEM" class="tab-badge" />
            </span>
          </template>
          <div class="tab-header">
            <span class="tab-tip">系统公告、重要通知等</span>
            <el-button type="primary" link size="small" @click="handleMarkTabRead" v-if="unreadCountMap.SYSTEM > 0">
              本类全部已读
            </el-button>
          </div>
        </el-tab-pane>
        
        <el-tab-pane name="APPROVAL">
          <template #label>
            <span class="tab-label">
              <el-icon><Tickets /></el-icon>
              审批通知
              <el-badge v-if="unreadCountMap.APPROVAL" :value="unreadCountMap.APPROVAL" :hidden="!unreadCountMap.APPROVAL" class="tab-badge" />
            </span>
          </template>
          <div class="tab-header">
            <span class="tab-tip">审批流程、待办事项等</span>
            <el-button type="primary" link size="small" @click="handleMarkTabRead" v-if="unreadCountMap.APPROVAL > 0">
              本类全部已读
            </el-button>
          </div>
        </el-tab-pane>
        
        <el-tab-pane name="FILE">
          <template #label>
            <span class="tab-label">
              <el-icon><Folder /></el-icon>
              文件动态
              <el-badge v-if="unreadCountMap.FILE" :value="unreadCountMap.FILE" :hidden="!unreadCountMap.FILE" class="tab-badge" />
            </span>
          </template>
          <div class="tab-header">
            <span class="tab-tip">文件上传、分享、更新等</span>
            <el-button type="primary" link size="small" @click="handleMarkTabRead" v-if="unreadCountMap.FILE > 0">
              本类全部已读
            </el-button>
          </div>
        </el-tab-pane>
      </el-tabs>

      <div class="notification-list" v-loading="loading">
        <el-empty v-if="notificationList.length === 0" description="暂无消息" :image-size="100" />
        
        <div 
          v-for="item in notificationList" 
          :key="item.id" 
          class="notification-item"
          :class="{ 'unread': item.isRead === 0 }"
          @click="handleView(item)"
        >
          <div class="item-icon" :class="getTypeClass(item.type)">
            <el-icon>
              <Bell v-if="item.type === 'SYSTEM'" />
              <Tickets v-else-if="item.type === 'APPROVAL'" />
              <Folder v-else />
            </el-icon>
          </div>
          
          <div class="item-content">
            <div class="item-title">
              <span class="title-text">{{ item.title }}</span>
              <span class="item-time">{{ formatTime(item.createTime) }}</span>
            </div>
            <div class="item-desc">{{ item.content }}</div>
          </div>
          
          <div class="item-actions" @click.stop>
            <el-button 
              v-if="item.isRead === 0" 
              type="primary" 
              link 
              size="small" 
              @click="handleMarkRead(item)"
            >
              标为已读
            </el-button>
            <el-button 
              type="danger" 
              link 
              size="small" 
              @click="handleDelete(item)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
          
          <div class="unread-dot" v-if="item.isRead === 0"></div>
        </div>
      </div>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
        class="pagination"
        v-if="pagination.total > 0"
      />
    </el-card>

    <el-dialog v-model="detailVisible" title="消息详情" width="500px" center>
      <div class="detail-content">
        <div class="detail-header">
          <div class="detail-icon" :class="getTypeClass(currentNotification.type)">
            <el-icon>
              <Bell v-if="currentNotification.type === 'SYSTEM'" />
              <Tickets v-else-if="currentNotification.type === 'APPROVAL'" />
              <Folder v-else />
            </el-icon>
          </div>
          <div class="detail-title-wrap">
            <h3>{{ currentNotification.title }}</h3>
            <p class="detail-type">{{ getTypeLabel(currentNotification.type) }}</p>
          </div>
        </div>
        <div class="detail-body">
          <p class="detail-text">{{ currentNotification.content }}</p>
        </div>
        <div class="detail-footer">
          <span class="detail-time">{{ currentNotification.createTime }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onActivated } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bell, Tickets, Folder, Delete, Check } from '@element-plus/icons-vue'
import {
  getNotificationList,
  markAsRead,
  markAllAsRead,
  markAllAsReadByType,
  deleteNotification,
  clearReadNotifications,
  getUnreadCountByType
} from '@/api/notification'
import { useNotificationStore } from '@/store/modules/notification'

const notificationStore = useNotificationStore()
const loading = ref(false)
const activeTab = ref('all')
const detailVisible = ref(false)
const currentNotification = ref({})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const notificationList = ref([])
const unreadCountMap = reactive({
  SYSTEM: 0,
  APPROVAL: 0,
  FILE: 0
})

const unreadCountByTab = computed(() => {
  if (activeTab.value === 'all') {
    return Object.values(unreadCountMap).reduce((a, b) => a + b, 0)
  }
  return unreadCountMap[activeTab.value] || 0
})

const hasRead = computed(() => {
  return notificationList.value.some(item => item.isRead === 1)
})

const formatTime = (time) => {
  if (!time) return ''
  const now = new Date()
  const date = new Date(time)
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return time.slice(0, 10)
}

const getTypeLabel = (type) => {
  const map = {
    'SYSTEM': '系统通知',
    'APPROVAL': '审批通知',
    'FILE': '文件动态'
  }
  return map[type] || type
}

const getTypeClass = (type) => {
  const map = {
    'SYSTEM': 'icon-system',
    'APPROVAL': 'icon-approval',
    'FILE': 'icon-file'
  }
  return map[type] || 'icon-system'
}

const fetchUnreadCountMap = async () => {
  try {
    await notificationStore.fetchUnreadCountByType(true)
    Object.assign(unreadCountMap, notificationStore.unreadCountByType)
  } catch (error) {
    console.error('获取未读数量失败', error)
  }
}

const fetchData = async () => {
  if (loading.value) return
  loading.value = true
  
  const timeoutId = setTimeout(() => {
    if (loading.value) {
      loading.value = false
      ElMessage.error('加载超时，请重试')
    }
  }, 10000)
  
  try {
    const params = {
      page: pagination.page,
      size: pagination.size
    }
    if (activeTab.value !== 'all') {
      params.type = activeTab.value
    }
    
    const res = await getNotificationList(params)
    notificationList.value = res.data?.list || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    console.error('获取消息列表失败', error)
    ElMessage.error('获取消息列表失败')
    notificationList.value = []
    pagination.total = 0
  } finally {
    clearTimeout(timeoutId)
    loading.value = false
  }
}

const handleTabChange = () => {
  pagination.page = 1
  fetchData()
}

const handleView = async (item) => {
  currentNotification.value = item
  detailVisible.value = true
  if (item.isRead === 0) {
    try {
      await markAsRead(item.id)
      item.isRead = 1
      notificationStore.decrementByType(item.type)
      if (unreadCountMap[item.type]) {
        unreadCountMap[item.type]--
      }
    } catch (error) {
      console.error('标记已读失败', error)
    }
  }
}

const handleMarkRead = async (item) => {
  try {
    await markAsRead(item.id)
    item.isRead = 1
    notificationStore.decrementByType(item.type)
    if (unreadCountMap[item.type]) {
      unreadCountMap[item.type]--
    }
    ElMessage.success('已标记为已读')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleMarkAllRead = async () => {
  try {
    await ElMessageBox.confirm('确定将所有消息标记为已读吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await markAllAsRead()
    notificationList.value.forEach(item => {
      item.isRead = 1
    })
    Object.keys(unreadCountMap).forEach(key => {
      unreadCountMap[key] = 0
    })
    notificationStore.markAllAsRead()
    ElMessage.success('全部已标记为已读')
  } catch {
  }
}

const handleMarkTabRead = async () => {
  const type = activeTab.value === 'all' ? '' : activeTab.value
  try {
    await markAllAsReadByType(type)
    notificationList.value.forEach(item => {
      if (activeTab.value === 'all' || item.type === activeTab.value) {
        item.isRead = 1
      }
    })
    if (activeTab.value === 'all') {
      Object.keys(unreadCountMap).forEach(key => {
        unreadCountMap[key] = 0
      })
      notificationStore.markAllAsRead()
    } else {
      unreadCountMap[activeTab.value] = 0
      notificationStore.markTypeAsRead(activeTab.value)
    }
    ElMessage.success('已标记为已读')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (item) => {
  try {
    await ElMessageBox.confirm('确定删除这条消息吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteNotification(item.id)
    notificationList.value = notificationList.value.filter(i => i.id !== item.id)
    pagination.total--
    if (item.isRead === 0) {
      notificationStore.decrementByType(item.type)
      if (unreadCountMap[item.type]) {
        unreadCountMap[item.type]--
      }
    }
    ElMessage.success('删除成功')
  } catch {
  }
}

const handleClearRead = async () => {
  try {
    await ElMessageBox.confirm('确定清空所有已读消息吗？此操作不可恢复！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    })
    await clearReadNotifications()
    notificationList.value = notificationList.value.filter(item => item.isRead === 0)
    pagination.total = notificationList.value.length
    ElMessage.success('已清空已读消息')
    fetchData()
  } catch {
  }
}

onMounted(() => {
  fetchData()
  fetchUnreadCountMap()
})

onActivated(() => {
  fetchUnreadCountMap()
})
</script>

<style scoped lang="scss">
.notification-page {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-actions {
      display: flex;
      gap: 10px;
    }
  }

  .tab-label {
    display: flex;
    align-items: center;
    gap: 5px;

    .tab-badge {
      margin-left: 5px;
    }
  }

  .tab-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 0;
    margin-bottom: 10px;
    border-bottom: 1px solid #ebeef5;

    .tab-tip {
      color: #909399;
      font-size: 13px;
    }
  }

  .notification-list {
    min-height: 300px;

    .notification-item {
      display: flex;
      align-items: flex-start;
      padding: 16px;
      border-radius: 8px;
      margin-bottom: 8px;
      background: #fafafa;
      cursor: pointer;
      position: relative;
      transition: all 0.3s;

      &:hover {
        background: #f0f7ff;
      }

      &.unread {
        background: #fff;
        border-left: 3px solid #409eff;
      }

      .unread-dot {
        position: absolute;
        top: 16px;
        right: 16px;
        width: 8px;
        height: 8px;
        background: #f56c6c;
        border-radius: 50%;
      }

      .item-icon {
        width: 44px;
        height: 44px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 14px;
        flex-shrink: 0;

        :deep(.el-icon) {
          font-size: 20px;
          color: #fff;
        }

        &.icon-system {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }

        &.icon-approval {
          background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        }

        &.icon-file {
          background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
        }
      }

      .item-content {
        flex: 1;
        min-width: 0;

        .item-title {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 6px;

          .title-text {
            font-size: 15px;
            font-weight: 500;
            color: #303133;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            max-width: 400px;
          }

          .item-time {
            font-size: 12px;
            color: #909399;
            flex-shrink: 0;
            margin-left: 10px;
          }
        }

        .item-desc {
          font-size: 13px;
          color: #606266;
          line-height: 1.5;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          padding-right: 60px;
        }
      }

      .item-actions {
        display: flex;
        gap: 5px;
        opacity: 0;
        transition: opacity 0.3s;
        flex-shrink: 0;
      }

      &:hover .item-actions {
        opacity: 1;
      }
    }
  }

  .pagination {
    margin-top: 20px;
    justify-content: flex-end;
  }

  .detail-content {
    .detail-header {
      display: flex;
      align-items: center;
      margin-bottom: 20px;
      padding-bottom: 20px;
      border-bottom: 1px solid #ebeef5;

      .detail-icon {
        width: 50px;
        height: 50px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 15px;

        :deep(.el-icon) {
          font-size: 24px;
          color: #fff;
        }

        &.icon-system {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }

        &.icon-approval {
          background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        }

        &.icon-file {
          background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
        }
      }

      .detail-title-wrap {
        h3 {
          margin: 0 0 5px 0;
          font-size: 18px;
          color: #303133;
        }

        .detail-type {
          margin: 0;
          font-size: 13px;
          color: #909399;
        }
      }
    }

    .detail-body {
      min-height: 100px;

      .detail-text {
        font-size: 14px;
        color: #606266;
        line-height: 1.8;
        white-space: pre-wrap;
        margin: 0;
      }
    }

    .detail-footer {
      margin-top: 20px;
      padding-top: 15px;
      border-top: 1px solid #ebeef5;
      text-align: right;

      .detail-time {
        font-size: 12px;
        color: #909399;
      }
    }
  }
}
</style>
