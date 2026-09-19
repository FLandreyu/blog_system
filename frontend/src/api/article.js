import request from './request'

/** 公开列表：只返回已发布文章 */
export function fetchArticles(params) {
  return request.get('/articles', { params })
}

/** 我的文章：含草稿 */
export function fetchMyArticles(params) {
  return request.get('/articles/mine', { params })
}

export function fetchArticleDetail(id) {
  return request.get(`/articles/${id}`)
}

export function createArticle(data) {
  return request.post('/articles', data)
}

export function updateArticle(id, data) {
  return request.put(`/articles/${id}`, data)
}

export function deleteArticle(id) {
  return request.delete(`/articles/${id}`)
}
