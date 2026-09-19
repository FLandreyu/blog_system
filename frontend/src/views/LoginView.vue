<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { login, register } from '../api/auth'
import { setAuth } from '../store/user'

const route = useRoute()
const router = useRouter()

const activeTab = ref('login')
const loading = ref(false)

const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', password: '', nickname: '', email: '' })

async function handleLogin() {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    const data = await login(loginForm)
    setAuth(data.token, data.user)
    ElMessage.success('登录成功')
    // 登录前如果被守卫拦下来过，这里跳回原来想去的页面
    router.replace(route.query.redirect || '/')
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  if (!registerForm.username || !registerForm.password || !registerForm.nickname) {
    ElMessage.warning('请填写完整的注册信息')
    return
  }

  loading.value = true
  try {
    await register(registerForm)
    ElMessage.success('注册成功，请登录')
    // 注册接口只返回用户信息、不签发 token，所以注册完切回登录页
    loginForm.username = registerForm.username
    loginForm.password = ''
    activeTab.value = 'login'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login">
    <el-card shadow="never">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="登录" name="login">
          <el-form label-width="70px" @submit.prevent>
            <el-form-item label="用户名">
              <el-input v-model="loginForm.username" placeholder="admin 或 user" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input
                v-model="loginForm.password"
                type="password"
                show-password
                placeholder="123456"
                @keyup.enter="handleLogin"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="loading" @click="handleLogin">登录</el-button>
            </el-form-item>
          </el-form>

          <el-alert type="info" :closable="false">
            演示账号：admin / 123456（管理员），user / 123456（普通用户）
          </el-alert>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form label-width="70px" @submit.prevent>
            <el-form-item label="用户名">
              <el-input v-model="registerForm.username" placeholder="字母、数字、下划线，3-50 位" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input
                v-model="registerForm.password"
                type="password"
                show-password
                placeholder="至少 6 位"
              />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="registerForm.nickname" placeholder="显示在文章和评论里" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="registerForm.email" placeholder="选填" @keyup.enter="handleRegister" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="loading" @click="handleRegister">注册</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<style scoped>
.login {
  max-width: 460px;
  margin: 40px auto 0;
}
</style>
