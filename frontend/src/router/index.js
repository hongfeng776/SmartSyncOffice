import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/modules/user'

const constantRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '系统首页', icon: 'HomeFilled' }
      }
    ]
  }
]

const asyncRoutes = [
  {
    path: '/org',
    component: () => import('@/layout/index.vue'),
    redirect: '/org/dept',
    meta: { title: '组织架构', icon: 'OfficeBuilding', roles: ['ADMIN'] },
    children: [
      {
        path: 'dept',
        name: 'OrgDept',
        component: () => import('@/views/org/dept/index.vue'),
        meta: { title: '部门管理', icon: 'Menu', roles: ['ADMIN'] }
      },
      {
        path: 'employee',
        name: 'OrgEmployee',
        component: () => import('@/views/org/employee/index.vue'),
        meta: { title: '员工列表', icon: 'User', roles: ['ADMIN'] }
      }
    ]
  },
  {
    path: '/system',
    component: () => import('@/layout/index.vue'),
    redirect: '/system/user',
    meta: { title: '系统管理', icon: 'Setting', roles: ['ADMIN'] },
    children: [
      {
        path: 'user',
        name: 'SystemUser',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理', icon: 'User', roles: ['ADMIN'] }
      },
      {
        path: 'role',
        name: 'SystemRole',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理', icon: 'UserFilled', roles: ['ADMIN'] }
      },
      {
        path: 'permission',
        name: 'SystemPermission',
        component: () => import('@/views/system/permission/index.vue'),
        meta: { title: '权限管理', icon: 'Lock', roles: ['ADMIN'] }
      }
    ]
  },
  {
    path: '/approval',
    component: () => import('@/layout/index.vue'),
    redirect: '/approval/create',
    meta: { title: '流程审批', icon: 'Document' },
    children: [
      {
        path: 'create',
        name: 'ApprovalCreate',
        component: () => import('@/views/approval/create/index.vue'),
        meta: { title: '发起审批', icon: 'Plus' }
      },
      {
        path: 'todo',
        name: 'ApprovalTodo',
        component: () => import('@/views/approval/todo/index.vue'),
        meta: { title: '待我审批', icon: 'Clock' }
      },
      {
        path: 'my',
        name: 'ApprovalMy',
        component: () => import('@/views/approval/my/index.vue'),
        meta: { title: '我发起的', icon: 'Tickets' }
      },
      {
        path: 'detail',
        name: 'ApprovalDetail',
        component: () => import('@/views/approval/detail/index.vue'),
        meta: { title: '审批详情', icon: 'View', hidden: true }
      }
    ]
  },
  {
    path: '/todo',
    component: () => import('@/layout/index.vue'),
    children: [
      {
        path: '',
        name: 'Todo',
        component: () => import('@/views/todo/index.vue'),
        meta: { title: '待办事项', icon: 'TodoList' }
      }
    ]
  },
  {
    path: '/file',
    component: () => import('@/layout/index.vue'),
    children: [
      {
        path: '',
        name: 'File',
        component: () => import('@/views/file/index.vue'),
        meta: { title: '文件管理', icon: 'Folder' }
      }
    ]
  },
  {
    path: '/profile',
    component: () => import('@/layout/index.vue'),
    children: [
      {
        path: '',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心', icon: 'User' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes
})

const whiteList = ['/login', '/404', '/403']

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()

  if (userStore.token) {
    if (to.path === '/login') {
      next({ path: '/' })
    } else {
      if (!userStore.userInfo) {
        try {
          await userStore.getUserInfo()
          const accessRoutes = filterAsyncRoutes(asyncRoutes, userStore.roles)
          accessRoutes.forEach(route => {
            router.addRoute(route)
          })
          next({ ...to, replace: true })
        } catch (error) {
          await userStore.logout()
          next({ path: '/login' })
        }
      } else {
        if (hasPermission(to, userStore.roles)) {
          next()
        } else {
          next('/403')
        }
      }
    }
  } else {
    if (whiteList.includes(to.path)) {
      next()
    } else {
      const autoLogin = localStorage.getItem('auto_login')
      const remembered = localStorage.getItem('remembered_login')
      
      if (autoLogin === 'true' && remembered) {
        try {
          const info = JSON.parse(remembered)
          if (info.username && info.password) {
            await userStore.login({ username: info.username, password: info.password })
            await userStore.getUserInfo()
            const accessRoutes = filterAsyncRoutes(asyncRoutes, userStore.roles)
            accessRoutes.forEach(route => {
              router.addRoute(route)
            })
            next({ ...to, replace: true })
            return
          }
        } catch (error) {
          console.error('自动登录失败:', error)
          localStorage.removeItem('auto_login')
        }
      }
      next({ path: '/login', query: { redirect: to.path } })
    }
  }
})

function filterAsyncRoutes(routes, roles) {
  const res = []
  routes.forEach(route => {
    const tmp = { ...route }
    if (hasPermission(tmp, roles)) {
      if (tmp.children) {
        tmp.children = filterAsyncRoutes(tmp.children, roles)
      }
      res.push(tmp)
    }
  })
  return res
}

function hasPermission(route, roles) {
  if (route.meta && route.meta.roles) {
    return roles.some(role => route.meta.roles.includes(role))
  }
  return true
}

export default router
