<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import { createCategory, deleteCategory, fetchCategories, updateCategory } from '../../api/category'

const loading = ref(false)
const saving = ref(false)
const categories = ref([])

const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref()

const form = reactive({ name: '', sort: 0 })

const rules = {
  name: [{ required: true, message: '请输入分类名', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    categories.value = await fetchCategories()
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  form.name = ''
  form.sort = 0
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  form.name = row.name
  form.sort = row.sort
  dialogVisible.value = true
}

async function submit() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  saving.value = true
  try {
    if (editingId.value) {
      await updateCategory(editingId.value, { ...form })
    } else {
      await createCategory({ ...form })
    }
    ElMessage.success('已保存')
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  // 被文章引用的分类后端会拒绝删除，前端这里先给个更直观的提示
  if (row.articleCount > 0) {
    ElMessage.warning(`该分类下还有 ${row.articleCount} 篇文章，请先移走或删除这些文章`)
    return
  }

  try {
    await ElMessageBox.confirm(`确定删除分类「${row.name}」吗？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  await deleteCategory(row.id)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>

<template>
  <el-card shadow="never">
    <template #header>
      <div class="header">
        <span>分类管理</span>
        <el-button type="primary" @click="openCreate">新增分类</el-button>
      </div>
    </template>

    <el-table v-loading="loading" :data="categories" style="width: 100%">
      <el-table-column prop="name" label="分类名" min-width="160" />
      <el-table-column prop="sort" label="排序" width="100" />
      <el-table-column prop="articleCount" label="文章数" width="100" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" size="small" @click="openEdit(row)">编辑</el-button>
          <el-button text type="danger" size="small" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑分类' : '新增分类'"
      width="420px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="70px">
        <el-form-item label="分类名" prop="name">
          <el-input v-model="form.name" maxlength="50" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
