import { ref } from 'vue'
import { getUnreadCount } from '@/api/notification'

const unreadCount = ref(0)
const pollingTimer = ref(null)

export function useNotification() {
  const fetchUnreadCount = async () => {
    try {
      const res = await getUnreadCount()
      unreadCount.value = res.data
    } catch (error) {
      console.error('获取未读消息数失败:', error)
    }
  }

  const startPolling = (interval = 30000) => {
    if (pollingTimer.value) {
      clearInterval(pollingTimer.value)
    }
    fetchUnreadCount()
    pollingTimer.value = setInterval(fetchUnreadCount, interval)
  }

  const stopPolling = () => {
    if (pollingTimer.value) {
      clearInterval(pollingTimer.value)
      pollingTimer.value = null
    }
  }

  return {
    unreadCount,
    fetchUnreadCount,
    startPolling,
    stopPolling
  }
}
