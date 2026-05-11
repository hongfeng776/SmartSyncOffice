import { defineStore } from 'pinia'
import { login, getUserInfo, logout } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',
    userInfo: null,
    roles: [],
    permissions: [],
    menus: []
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    isAdmin: (state) => state.roles.includes('ADMIN'),
    hasRole: (state) => (role) => state.roles.includes(role),
    hasPermission: (state) => (permission) => state.permissions.includes(permission)
  },

  actions: {
    async login(loginData) {
      const res = await login(loginData)
      this.token = res.data.token
      this.menus = []
      return res
    },

    async getUserInfo() {
      const res = await getUserInfo()
      this.userInfo = res.data
      this.roles = res.data.roles || []
      this.permissions = res.data.permissions || []
      return res
    },

    setMenus(menus) {
      this.menus = menus
    },

    async logout() {
      try {
        await logout()
      } finally {
        this.resetState()
      }
    },

    resetState() {
      this.token = ''
      this.userInfo = null
      this.roles = []
      this.permissions = []
      this.menus = []
    }
  },

  persist: {
    key: 'user-store',
    paths: ['token', 'userInfo', 'roles', 'permissions', 'menus']
  }
})
