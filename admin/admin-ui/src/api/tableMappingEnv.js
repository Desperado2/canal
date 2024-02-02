import request from '@/utils/request'

export function addTableMappingEnv(data) {
  return request({
    url: '/tableMappingEnv',
    method: 'post',
    data
  })
}

export function getTableMappingEnvList() {
  return request({
    url: '/tableMappingEnv/list',
    method: 'get'
  })
}


export function getTableMappingEnvPageList(params) {
  return request({
    url: '/tableMappingEnv',
    method: 'get',
    params: params
  })
}


export function updateTableMappingEnv(data) {
  return request({
    url: '/tableMappingEnv',
    method: 'put',
    data
  })
}
