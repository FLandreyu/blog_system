import axios from 'axios'
import { ElMessage } from 'element-plus'

import router from '../router'
import { clearAuth, getToken } from '../store/user'

/**
 * 全局唯一的 axios 实例。三个统一都在这里：
 *   1. 请求自动带上 Authorization 头
 *   2. 响应自动解包 Result，业务代码直接拿到 data
 *   3. 401 自动清登录态并跳登录页
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  // 成功分支：后端返回 { code, message, data }，这里直接把 data 交给调用方
  (response) => {
    const body = response.data
    if (body && body.code === 200) {
      return body.data
    }
    // 正常情况下走不到这里：后端出错时会把 HTTP 状态码也设成业务 code，
    // 从而落到下面的失败分支。这里只是兜底。
    const message = body?.message || '请求失败'
    ElMessage.error(message)
    return Promise.reject(new Error(message))
  },

  // 失败分支：后端的 4xx / 5xx 都在这里统一处理
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message || '网络异常，请稍后重试'

    if (status === 401) {
      clearAuth()
      ElMessage.error(message)
      const current = router.currentRoute.value
      if (current.name !== 'login') {
        router.push({ name: 'login', query: { redirect: current.fullPath } })
      }
    } else {
      ElMessage.error(message)
    }

    return Promise.reject(error)
  }
)

export default request
