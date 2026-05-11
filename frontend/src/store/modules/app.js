import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    sidebar: {
      collapsed: false
    },
    device: 'desktop'
  }),

  actions: {
    toggleSidebar() {
      this.sidebar.collapsed = !this.sidebar.collapsed
    },

    setDevice(device) {
      this.device = device
    }
  },

  persist: {
    key: 'app-store',
    paths: ['sidebar']
  }
})
