import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/api/login',
    method: 'post',
    data
  })
}

export function getUserInfo() {
  return request({
    url: '/api/user/info',
    method: 'get'
  })
}

export function getUserMenus() {
  return request({
    url: '/api/user/menus',
    method: 'get'
  })
}

export function logout() {
  return request({
    url: '/api/logout',
    method: 'post'
  })
}

export function getRoles() {
  return request({
    url: '/api/roles',
    method: 'get'
  })
}

export function getPermissions() {
  return request({
    url: '/api/permissions',
    method: 'get'
  })
}
