import { defineStore } from 'pinia'
import { getUnreadCount, getUnreadCountByType } from '@/api/notification'

export const useNotificationStore = defineStore('notification', {
  state: () => ({
    unreadCount: 0,
    unreadCountByType: {
      SYSTEM: 0,
      APPROVAL: 0,
      FILE: 0
    },
    isFetching: false
  }),

  actions: {
    async fetchUnreadCount(force = false) {
      if (this.isFetching && !force) return
      this.isFetching = true
      try {
        const res = await getUnreadCount()
        const count = res.data || 0
        this.unreadCount = count
      } catch (error) {
        console.error('获取未读消息数量失败', error)
      } finally {
        this.isFetching = false
      }
    },

    async fetchUnreadCountByType(force = false) {
      if (this.isFetching && !force) return
      this.isFetching = true
      try {
        const res = await getUnreadCountByType()
        const data = res.data || {}
        this.unreadCountByType = {
          SYSTEM: data.SYSTEM || 0,
          APPROVAL: data.APPROVAL || 0,
          FILE: data.FILE || 0
        }
        this.unreadCount = Object.values(this.unreadCountByType).reduce((a, b) => a + b, 0)
      } catch (error) {
        console.error('获取未读消息数量失败', error)
      } finally {
        this.isFetching = false
      }
    },

    decrementByType(type) {
      if (this.unreadCountByType[type] > 0) {
        this.unreadCountByType[type]--
      }
      this.recalculateTotal()
    },

    markTypeAsRead(type) {
      if (type) {
        this.unreadCountByType[type] = 0
      } else {
        Object.keys(this.unreadCountByType).forEach(key => {
          this.unreadCountByType[key] = 0
        })
      }
      this.recalculateTotal()
    },

    markAllAsRead() {
      Object.keys(this.unreadCountByType).forEach(key => {
        this.unreadCountByType[key] = 0
      })
      this.unreadCount = 0
    },

    recalculateTotal() {
      this.unreadCount = Object.values(this.unreadCountByType).reduce((a, b) => a + b, 0)
    },

    reset() {
      this.unreadCount = 0
      this.unreadCountByType = {
        SYSTEM: 0,
        APPROVAL: 0,
        FILE: 0
      }
    }
  }
})
