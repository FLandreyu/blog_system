import { computed, reactive } from 'vue'

/**
 * 登录态。
 *
 * 这个项目规模不大，没必要上 Pinia：一个 reactive 对象 + 几个函数就够了。
 * token 存 localStorage，所以刷新页面登录态不会丢。
 */
const TOKEN_KEY = 'blog_token'
const USER_KEY = 'blog_user'

function readUser() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY) || 'null')
  } catch {
    // localStorage 里的内容被手动改坏时不要让应用直接白屏
    localStorage.removeItem(USER_KEY)
    return null
  }
}

const state = reactive({
  token: localStorage.getItem(TOKEN_KEY) || '',
  user: readUser()
})

export const isLoggedIn = computed(() => !!state.token)
export const isAdmin = computed(() => state.user?.role === 'ADMIN')

export function getToken() {
  return state.token
}

export function getCurrentUser() {
  return state.user
}

export function setAuth(token, user) {
  state.token = token
  state.user = user
  localStorage.setItem(TOKEN_KEY, token)
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function setUser(user) {
  state.user = user
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function clearAuth() {
  state.token = ''
  state.user = null
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}
