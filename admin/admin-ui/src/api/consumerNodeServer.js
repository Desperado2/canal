import request from '@/utils/request'

export function getNodeServers(params) {
  return request({
    url: '/taskNodeServers',
    method: 'get',
    params: params
  })
}

export function addNodeServer(data) {
  return request({
    url: '/taskNodeServer',
    method: 'post',
    data
  })
}

export function nodeServerDetail(id) {
  return request({
    url: '/taskNodeServer/' + id,
    method: 'get'
  })
}

export function updateNodeServer(data) {
  return request({
    url: '/taskNodeServer',
    method: 'put',
    data
  })
}

export function deleteNodeServer(id) {
  return request({
    url: '/taskNodeServer/' + id,
    method: 'delete'
  })
}



export function nodeServerLog(id) {
  return request({
    url: '/taskNodeServer/log/' + id,
    method: 'get'
  })
}
