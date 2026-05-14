import request from '@/utils/request'

export function createTask(data) {
  return request({
    url: '/api/tasks',
    method: 'post',
    data
  })
}

export function assignTask(id, assigneeId, comment) {
  return request({
    url: `/api/tasks/${id}/assign`,
    method: 'post',
    params: { assigneeId, comment }
  })
}

export function startTask(id) {
  return request({
    url: `/api/tasks/${id}/start`,
    method: 'post'
  })
}

export function updateTaskProgress(id, progress, comment) {
  return request({
    url: `/api/tasks/${id}/progress`,
    method: 'post',
    params: { progress, comment }
  })
}

export function completeTask(id, comment) {
  return request({
    url: `/api/tasks/${id}/complete`,
    method: 'post',
    params: { comment }
  })
}

export function cancelTask(id, reason) {
  return request({
    url: `/api/tasks/${id}/cancel`,
    method: 'post',
    params: { reason }
  })
}

export function getCreatedTasks(params) {
  return request({
    url: '/api/tasks/created',
    method: 'get',
    params
  })
}

export function getAssignedTasks(params) {
  return request({
    url: '/api/tasks/assigned',
    method: 'get',
    params
  })
}

export function getAllTasks(params) {
  return request({
    url: '/api/tasks/all',
    method: 'get',
    params
  })
}

export function getTaskDetail(id) {
  return request({
    url: `/api/tasks/${id}`,
    method: 'get'
  })
}
