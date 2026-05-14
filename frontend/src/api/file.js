import request from '@/utils/request'

export function uploadFile(file, folderId, onProgress) {
  const formData = new FormData()
  formData.append('file', file)
  if (folderId) {
    formData.append('folderId', folderId)
  }

  return request({
    url: '/auth/file/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: onProgress
  })
}

export function uploadFileAsync(file, folderId, onProgress) {
  const formData = new FormData()
  formData.append('file', file)
  if (folderId) {
    formData.append('folderId', folderId)
  }

  return request({
    url: '/auth/file/upload/async',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: onProgress
  })
}

export function checkChunk(uploadId, filename, totalSize, folderId) {
  return request({
    url: '/auth/file/chunk/check',
    method: 'get',
    params: { uploadId, filename, totalSize, folderId }
  })
}

export function uploadChunk(formData, onProgress) {
  return request({
    url: '/auth/file/chunk/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: onProgress
  })
}

export function mergeChunks(uploadId) {
  return request({
    url: '/auth/file/chunk/merge',
    method: 'post',
    params: { uploadId }
  })
}

export function getFolderTree() {
  return request({
    url: '/auth/folder/tree',
    method: 'get'
  })
}

export function createFolder(data) {
  return request({
    url: '/auth/folder',
    method: 'post',
    data
  })
}

export function updateFolder(data) {
  return request({
    url: '/auth/folder',
    method: 'put',
    data
  })
}

export function deleteFolder(folderId) {
  return request({
    url: `/auth/folder/${folderId}`,
    method: 'delete'
  })
}

export function downloadFile(fileId) {
  return request({
    url: `/auth/file/download/${fileId}`,
    method: 'get',
    responseType: 'blob'
  })
}

export function deleteFile(fileId) {
  return request({
    url: `/auth/file/${fileId}`,
    method: 'delete'
  })
}

export function getFileList(data) {
  return request({
    url: '/auth/file/list',
    method: 'post',
    data
  })
}

export function getFileById(fileId) {
  return request({
    url: `/auth/file/${fileId}`,
    method: 'get'
  })
}

export function getFilePreviewUrl(fileId) {
  return request({
    url: `/auth/file/preview/${fileId}`,
    method: 'get'
  })
}
