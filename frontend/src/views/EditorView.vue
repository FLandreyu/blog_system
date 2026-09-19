<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { createArticle, fetchArticleDetail, updateArticle } from '../api/article'
import { fetchCategories } from '../api/category'
import { fetchTags } from '../api/tag'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const saving = ref(false)
const categories = ref([])
const tags = ref([])
const formRef = ref()

const form = reactive({
  title: '',
  summary: '',
  content: '',
  cover: '',
  categoryId: null,
  tagIds: [],
  status: 0
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入正文', trigger: 'blur' }]
}

async function save(status) {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  saving.value = true
  try {
    const payload = { ...form, status }
    if (isEdit.value) {
      await updateArticle(route.params.id, payload)
    } else {
      await createArticle(payload)
    }
    ElMessage.success(status === 1 ? '已发布' : '已存为草稿')
    router.push({ name: 'admin-articles' })
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  categories.value = await fetchCategories()
  tags.value = await fetchTags()

  if (isEdit.value) {
    const data = await fetchArticleDetail(route.params.id)
    form.title = data.title
    form.summary = data.summary || ''
    form.content = data.content
    form.cover = data.cover || ''
    form.categoryId = data.categoryId
    form.tagIds = (data.tags || []).map((tag) => tag.id)
    form.status = data.status
  }
})
</script>

<template>
  <el-card shadow="never">
    <template #header>
      <span>{{ isEdit ? '编辑文章' : '写文章' }}</span>
    </template>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" maxlength="200" show-word-limit placeholder="请输入标题" />
      </el-form-item>

      <el-form-item label="摘要">
        <el-input
          v-model="form.summary"
          type="textarea"
          :rows="2"
          maxlength="500"
          show-word-limit
          placeholder="选填，会显示在列表页"
        />
      </el-form-item>

      <el-form-item label="分类">
        <el-select v-model="form.categoryId" placeholder="选择分类" clearable style="width: 220px">
          <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>

      <el-form-item label="标签">
        <el-select v-model="form.tagIds" multiple placeholder="选择标签" style="width: 100%">
          <el-option v-for="tag in tags" :key="tag.id" :label="tag.name" :value="tag.id" />
        </el-select>
      </el-form-item>

      <el-form-item label="正文" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="14"
          placeholder="支持纯文本，换行会被保留"
        />
      </el-form-item>

      <el-form-item>
        <el-button :loading="saving" @click="save(0)">存为草稿</el-button>
        <el-button type="primary" :loading="saving" @click="save(1)">发布</el-button>
        <el-button @click="router.back()">返回</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>
