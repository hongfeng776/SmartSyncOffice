import request from '@/utils/request'

export function getNotificationList(params) {
  return request({
    url: '/api/notifications',
    method: 'get',
    params
  })
}

export function getUnreadCount() {
  return request({
    url: '/api/notifications/unread-count',
    method: 'get'
  })
}

export function getUnreadCountByType() {
  return request({
    url: '/api/notifications/unread-count-by-type',
    method: 'get'
  })
}

export function markAsRead(id) {
  return request({
    url: `/api/notifications/${id}/read`,
    method: 'put'
  })
}

export function markAllAsRead() {
  return request({
    url: '/api/notifications/read-all',
    method: 'put'
  })
}

export function batchSendSystemNotification(data) {
  return request({
    url: '/api/notifications/batch-send',
    method: 'post',
    params: data
  })
}

export function deleteNotification(id) {
  return request({
    url: `/api/notifications/${id}`,
    method: 'delete'
  })
}

export function markAllAsReadByType(type) {
  return request({
    url: '/api/notifications/read-all-by-type',
    method: 'put',
    params: { type }
  })
}

export function clearReadNotifications() {
  return request({
    url: '/api/notifications/clear-read',
    method: 'delete'
  })
}

export function getAnnouncementList(params) {
  return request({
    url: '/api/announcements',
    method: 'get',
    params
  })
}

export function getAnnouncementById(id) {
  return request({
    url: `/api/announcements/${id}`,
    method: 'get'
  })
}

export function createAnnouncement(data) {
  return request({
    url: '/api/announcements',
    method: 'post',
    data
  })
}

export function updateAnnouncement(data) {
  return request({
    url: '/api/announcements',
    method: 'put',
    data
  })
}

export function publishAnnouncement(id) {
  return request({
    url: `/api/announcements/${id}/publish`,
    method: 'put'
  })
}

export function withdrawAnnouncement(id) {
  return request({
    url: `/api/announcements/${id}/withdraw`,
    method: 'put'
  })
}

export function deleteAnnouncement(id) {
  return request({
    url: `/api/announcements/${id}`,
    method: 'delete'
  })
}
