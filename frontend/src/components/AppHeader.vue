<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { clearAuth, getCurrentUser, isAdmin, isLoggedIn } from '../store/user'

const router = useRouter()
const route = useRoute()

const user = computed(() => getCurrentUser())
const loggedIn = computed(() => isLoggedIn.value)
const admin = computed(() => isAdmin.value)

function handleCommand(command) {
  if (command === 'logout') {
    clearAuth()
    ElMessage.success('已退出登录')
    if (route.meta.requiresAuth) {
      router.push({ name: 'home' })
    }
    return
  }
  router.push({ name: command })
}
</script>

<template>
  <header class="header">
    <div class="header__inner">
      <router-link to="/" class="header__brand">个人博客</router-link>

      <nav class="header__nav">
        <el-button text @click="router.push({ name: 'home' })">首页</el-button>

        <template v-if="loggedIn">
          <el-button text @click="router.push({ name: 'editor' })">写文章</el-button>
          <el-button text @click="router.push({ name: 'admin-articles' })">我的文章</el-button>
          <el-button v-if="admin" text @click="router.push({ name: 'admin-categories' })">
            分类管理
          </el-button>

          <el-dropdown @command="handleCommand">
            <span class="header__user">
              {{ user?.nickname || user?.username }}
              <el-tag v-if="admin" size="small" type="danger" effect="plain">管理员</el-tag>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>

        <el-button v-else type="primary" @click="router.push({ name: 'login' })">登录</el-button>
      </nav>
    </div>
  </header>
</template>

<style scoped>
.header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
}

.header__inner {
  max-width: 960px;
  margin: 0 auto;
  padding: 0 16px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header__brand {
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
}

.header__nav {
  display: flex;
  align-items: center;
  gap: 4px;
}

.header__user {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-left: 8px;
  cursor: pointer;
  color: #303133;
  outline: none;
}
</style>
