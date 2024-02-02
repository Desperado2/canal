import request from '@/utils/request'

export function addTableMapping(data) {
  return request({
    url: '/tableMapping',
    method: 'post',
    data
  })
}

export function getTableMappingList() {
  return request({
    url: '/tableMapping/list',
    method: 'get'
  })
}

export function getTableMapping(id) {
  return request({
    url: `/tableMapping/${id}`,
    method: 'get'
  })
}

export function getTableMappingTemplate() {
  return request({
    url: `/tableMapping/template`,
    method: 'get'
  })
}


export function getTableMappingPageList(params) {
  return request({
    url: '/tableMapping',
    method: 'get',
    params: params
  })
}


export function updateTableMapping(data) {
  return request({
    url: '/tableMapping',
    method: 'put',
    data
  })
}

export function updateTableMappingContent(data) {
  return request({
    url: '/tableMapping/content',
    method: 'put',
    data
  })
}

export function deleteTableMapping(id) {
  return request({
    url: '/tableMapping/' + id,
    method: 'delete'
  })
}
