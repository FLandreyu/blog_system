import request from './request'

export function fetchTags() {
  return request.get('/tags')
}
