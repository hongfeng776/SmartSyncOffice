import request from '@/utils/request'

export function getDeptTree() {
  return request({
    url: '/api/depts',
    method: 'get'
  })
}

export function getDeptList() {
  return request({
    url: '/api/depts/flat',
    method: 'get'
  })
}

export function getDeptById(id) {
  return request({
    url: `/api/depts/${id}`,
    method: 'get'
  })
}

export function createDept(data) {
  return request({
    url: '/api/depts',
    method: 'post',
    data
  })
}

export function updateDept(data) {
  return request({
    url: '/api/depts',
    method: 'put',
    data
  })
}

export function deleteDept(id) {
  return request({
    url: `/api/depts/${id}`,
    method: 'delete'
  })
}

export function getEmployeePage(params) {
  return request({
    url: '/api/employees',
    method: 'get',
    params
  })
}

export function getEmployeeById(id) {
  return request({
    url: `/api/employees/${id}`,
    method: 'get'
  })
}

export function createEmployee(data) {
  return request({
    url: '/api/employees',
    method: 'post',
    data
  })
}

export function updateEmployee(data) {
  return request({
    url: '/api/employees',
    method: 'put',
    data
  })
}

export function deleteEmployee(id) {
  return request({
    url: `/api/employees/${id}`,
    method: 'delete'
  })
}

export function getEmployeesByDept(deptId) {
  return request({
    url: `/api/employees/dept/${deptId}`,
    method: 'get'
  })
}

export function batchUpdateDept(data) {
  return request({
    url: '/api/employees/batch-dept',
    method: 'put',
    data
  })
}
