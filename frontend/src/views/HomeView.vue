<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import { fetchArticles } from '../api/article'
import { fetchCategories } from '../api/category'

const router = useRouter()

const loading = ref(false)
const articles = ref([])
const categories = ref([])
const total = ref(0)

const query = reactive({
  page: 1,
  size: 10,
  categoryId: null,
  keyword: ''
})

async function load() {
  loading.value = true
  try {
    // 空值传 undefined，axios 会直接不拼这个参数，
    // 后端也就不会收到空字符串去参与 WHERE 条件
    const data = await fetchArticles({
      page: query.page,
      size: query.size,
      categoryId: query.categoryId || undefined,
      keyword: query.keyword.trim() || undefined
    })
    articles.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  load()
}

function handleReset() {
  query.categoryId = null
  query.keyword = ''
  handleSearch()
}

function openArticle(id) {
  router.push({ name: 'article-detail', params: { id } })
}

onMounted(async () => {
  categories.value = await fetchCategories()
  await load()
})
</script>

<template>
  <div>
    <el-card shadow="never" class="filter">
      <div class="filter__row">
        <el-select
          v-model="query.categoryId"
          placeholder="全部分类"
          clearable
          style="width: 160px"
          @change="handleSearch"
        >
          <el-option
            v-for="item in categories"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>

        <el-input
          v-model="query.keyword"
          placeholder="搜索文章标题"
          clearable
          style="width: 260px"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />

        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </el-card>

    <div v-loading="loading" class="list">
      <el-empty v-if="!loading && articles.length === 0" description="还没有文章" />

      <el-card
        v-for="article in articles"
        :key="article.id"
        shadow="hover"
        class="article"
        @click="openArticle(article.id)"
      >
        <h3 class="article__title">{{ article.title }}</h3>
        <p v-if="article.summary" class="article__summary">{{ article.summary }}</p>

        <div class="article__meta">
          <el-tag v-if="article.categoryName" size="small" effect="plain">
            {{ article.categoryName }}
          </el-tag>
          <span>{{ article.authorName }}</span>
          <span>{{ article.createTime }}</span>
          <span>阅读 {{ article.viewCount }}</span>
        </div>
      </el-card>
    </div>

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
  </div>
</template>

<style scoped>
.filter {
  margin-bottom: 16px;
}

.filter__row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.article {
  margin-bottom: 12px;
  cursor: pointer;
}

.article__title {
  margin: 0 0 8px;
  font-size: 17px;
}

.article__summary {
  margin: 0 0 12px;
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
}

.article__meta {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #909399;
  font-size: 13px;
}

.pagination {
  margin-top: 20px;
  justify-content: center;
}
</style>
