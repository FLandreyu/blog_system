<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

import { deleteArticle, fetchMyArticles } from '../../api/article'

const router = useRouter()

const loading = ref(false)
const articles = ref([])
const total = ref(0)

const query = reactive({
  page: 1,
  size: 10,
  status: null
})

async function load() {
  loading.value = true
  try {
    const data = await fetchMyArticles({
      page: query.page,
      size: query.size,
      // status 为 null 表示"全部"，不传这个参数后端就不会加状态条件
      status: query.status === null ? undefined : query.status
    })
    articles.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function handleFilter() {
  query.page = 1
  load()
}

function edit(row) {
  router.push({ name: 'editor', params: { id: row.id } })
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确定删除《${row.title}》吗？删除后无法恢复。`, '提示', {
      type: 'warning'
    })
  } catch {
    return
  }
  await deleteArticle(row.id)
  ElMessage.success('已删除')
  // 删掉当前页最后一条时往前翻一页，避免停在空页上
  if (articles.value.length === 1 && query.page > 1) {
    query.page -= 1
  }
  await load()
}

onMounted(load)
</script>

<template>
  <el-card shadow="never">
    <template #header>
      <div class="header">
        <span>我的文章</span>
        <div class="header__right">
          <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px" @change="handleFilter">
            <el-option label="已发布" :value="1" />
            <el-option label="草稿" :value="0" />
          </el-select>
          <el-button type="primary" @click="router.push({ name: 'editor' })">写文章</el-button>
        </div>
      </div>
    </template>

    <el-table v-loading="loading" :data="articles" style="width: 100%">
      <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
      <el-table-column prop="categoryName" label="分类" width="120">
        <template #default="{ row }">{{ row.categoryName || '—' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small" effect="plain">
            {{ row.status === 1 ? '已发布' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览" width="80" />
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" size="small" @click="edit(row)">编辑</el-button>
          <el-button text type="danger" size="small" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="total > 0"
      class="pagination"
      background
      layout="prev, pager, next, total"
      :total="total"
      :current-page="query.page"
      :page-size="query.size"
      @current-change="
        (page) => {
          query.page = page
          load()
        }
      "
    />
  </el-card>
</template>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header__right {
  display: flex;
  gap: 12px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
