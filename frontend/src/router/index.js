import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

import { getToken, isAdmin } from '../store/user'

/**
 * 路由表。
 *
 * 页面组件用动态 import 懒加载：Vite 会把每个页面单独打成一个 chunk，
 * 首屏只下载首页需要的代码。
 */
const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('../views/HomeView.vue')
  },
  {
    path: '/article/:id',
    name: 'article-detail',
    component: () => import('../views/ArticleDetailView.vue')
  },
  {
    path: '/editor/:id?',
    name: 'editor',
    component: () => import('../views/EditorView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue')
  },
  {
    path: '/admin/articles',
    name: 'admin-articles',
    component: () => import('../views/admin/ArticleManageView.vue'),
    meta: { requiresAuth: true }
  },
  {
    // 分类的增删改只有 ADMIN 能做，所以比"我的文章"多一层角色校验
    path: '/admin/categories',
    name: 'admin-categories',
    component: () => import('../views/admin/CategoryManageView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

/**
 * 全局前置守卫：没登录就跳登录页，并把原本要去的地址记在 query 里，
 * 登录成功后可以直接跳回去。
 *
 * 这里只挡"有没有登录"这一层；"能不能改这篇文章"是后端的权限判断，
 * 前端拦截只是为了体验，不能当作安全边界。
 */
router.beforeEach((to) => {
  if (to.meta.requiresAuth && !getToken()) {
    ElMessage.warning('请先登录')
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.requiresAdmin && !isAdmin.value) {
    ElMessage.error('无权限访问该页面')
    return { name: 'home' }
  }
  return true
})

export default router
