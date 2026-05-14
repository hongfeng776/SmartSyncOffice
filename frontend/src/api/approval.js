import request from '@/utils/request'

export function createLeaveApproval(data) {
  return request({
    url: '/api/approvals/leave',
    method: 'post',
    data
  })
}

export function createOvertimeApproval(data) {
  return request({
    url: '/api/approvals/overtime',
    method: 'post',
    data
  })
}

export function createReimbursementApproval(data) {
  return request({
    url: '/api/approvals/reimbursement',
    method: 'post',
    data
  })
}

export function submitApproval(id) {
  return request({
    url: `/api/approvals/${id}/submit`,
    method: 'post'
  })
}

export function approveApproval(id, opinion) {
  return request({
    url: `/api/approvals/${id}/approve`,
    method: 'post',
    params: { opinion }
  })
}

export function rejectApproval(id, opinion) {
  return request({
    url: `/api/approvals/${id}/reject`,
    method: 'post',
    params: { opinion }
  })
}

export function getMyApprovalList(params) {
  return request({
    url: '/api/approvals/my',
    method: 'get',
    params
  })
}

export function getTodoApprovalList(params) {
  return request({
    url: '/api/approvals/todo',
    method: 'get',
    params
  })
}

export function getApprovalDetail(id) {
  return request({
    url: `/api/approvals/${id}`,
    method: 'get'
  })
}

export function urgeApproval(id) {
  return request({
    url: `/api/approvals/${id}/urge`,
    method: 'post'
  })
}

export function withdrawApproval(id, reason) {
  return request({
    url: `/api/approvals/${id}/withdraw`,
    method: 'post',
    params: { reason }
  })
}
