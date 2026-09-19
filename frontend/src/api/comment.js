import request from './request'

export function fetchComments(articleId) {
  return request.get('/comments', { params: { articleId } })
}

export function createComment(data) {
  return request.post('/comments', data)
}

export function deleteComment(id) {
  return request.delete(`/comments/${id}`)
}
