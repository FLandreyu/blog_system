<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

import { deleteArticle, fetchArticleDetail } from '../api/article'
import { createComment, deleteComment, fetchComments } from '../api/comment'
import { getCurrentUser, isLoggedIn } from '../store/user'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const article = ref(null)
const comments = ref([])

const content = ref('')

const currentUser = computed(() => getCurrentUser())
const loggedIn = computed(() => isLoggedIn.value)

/** 只有作者本人能编辑 / 删除（后端还会再校验一次，前端只是不显示按钮） */
const isAuthor = computed(
  () => !!article.value && currentUser.value?.id === article.value.authorId
)

const articleId = computed(() => Number(route.params.id))

async function loadArticle() {
  loading.value = true
  try {
    article.value = await fetchArticleDetail(articleId.value)
  } finally {
    loading.value = false
  }
}

async function loadComments() {
  comments.value = await fetchComments(articleId.value)
}

async function submitComment() {
  if (!content.value.trim()) {
    ElMessage.warning('评论内容不能为空')
    return
  }
  submitting.value = true
  try {
    await createComment({ articleId: articleId.value, content: content.value.trim() })
    content.value = ''
    ElMessage.success('评论成功')
    await loadComments()
  } finally {
    submitting.value = false
  }
}

async function reply(comment) {
  let value
  try {
    const result = await ElMessageBox.prompt(`回复 ${comment.nickname}`, '发表回复', {
      inputType: 'textarea',
      inputPlaceholder: '说点什么…',
      confirmButtonText: '发表',
      cancelButtonText: '取消'
    })
    value = result.value
  } catch {
    return // 用户点了取消
  }

  if (!value?.trim()) {
    ElMessage.warning('回复内容不能为空')
    return
  }
  await createComment({
    articleId: articleId.value,
    content: value.trim(),
    parentId: comment.id
  })
  ElMessage.success('回复成功')
  await loadComments()
}

/** 评论作者本人，或文章作者，都可以删 */
function canDeleteComment(comment) {
  if (!currentUser.value) return false
  return currentUser.value.id === comment.userId || isAuthor.value
}

async function removeComment(comment) {
  try {
    await ElMessageBox.confirm('确定删除这条评论吗？', '提示', { type: 'warning' })
  } catch {
    return
  }
  await deleteComment(comment.id)
  ElMessage.success('已删除')
  await loadComments()
}

async function removeArticle() {
  try {
    await ElMessageBox.confirm('删除后无法恢复，确定删除这篇文章吗？', '提示', { type: 'warning' })
  } catch {
    return
  }
  await deleteArticle(articleId.value)
  ElMessage.success('已删除')
  router.push({ name: 'home' })
}

onMounted(async () => {
  await loadArticle()
  await loadComments()
})
</script>

<template>
  <div v-loading="loading">
    <el-card v-if="article" shadow="never">
      <h1 class="title">{{ article.title }}</h1>

      <div class="meta">
        <el-tag v-if="article.categoryName" size="small" effect="plain">
          {{ article.categoryName }}
        </el-tag>
        <span>{{ article.authorName }}</span>
        <span>{{ article.createTime }}</span>
        <span>阅读 {{ article.viewCount }}</span>
      </div>

      <div class="tags">
        <el-tag v-for="tag in article.tags" :key="tag.id" size="small" type="info" effect="plain">
          {{ tag.name }}
        </el-tag>
      </div>

      <el-divider />

      <div class="content">{{ article.content }}</div>

      <div v-if="isAuthor" class="actions">
        <el-button size="small" @click="router.push({ name: 'editor', params: { id: article.id } })">
          编辑
        </el-button>
        <el-button size="small" type="danger" plain @click="removeArticle">删除</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="comments">
      <template #header>
        <span>评论（{{ article?.commentCount ?? 0 }}）</span>
      </template>

      <div v-if="loggedIn" class="comment-form">
        <el-input
          v-model="content"
          type="textarea"
          :rows="3"
          maxlength="1000"
          show-word-limit
          placeholder="说点什么…"
        />
        <el-button type="primary" :loading="submitting" class="comment-form__submit" @click="submitComment">
          发表评论
        </el-button>
      </div>
      <el-alert v-else type="info" :closable="false" title="登录后即可参与评论" />

      <el-empty v-if="comments.length === 0" description="还没有评论" />

      <div v-for="comment in comments" :key="comment.id" class="comment">
        <div class="comment__head">
          <strong>{{ comment.nickname }}</strong>
          <span class="comment__time">{{ comment.createTime }}</span>
        </div>
        <p class="comment__body">{{ comment.content }}</p>

        <div class="comment__actions">
          <el-button v-if="loggedIn" text size="small" @click="reply(comment)">回复</el-button>
          <el-button
            v-if="canDeleteComment(comment)"
            text
            size="small"
            type="danger"
            @click="removeComment(comment)"
          >
            删除
          </el-button>
        </div>

        <div v-if="comment.replies?.length" class="replies">
          <div v-for="child in comment.replies" :key="child.id" class="comment comment--reply">
            <div class="comment__head">
              <strong>{{ child.nickname }}</strong>
              <span class="comment__time">{{ child.createTime }}</span>
            </div>
            <p class="comment__body">{{ child.content }}</p>
            <div class="comment__actions">
              <el-button
                v-if="canDeleteComment(child)"
                text
                size="small"
                type="danger"
                @click="removeComment(child)"
              >
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.title {
  margin: 0 0 12px;
  font-size: 24px;
}

.meta {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #909399;
  font-size: 13px;
}

.tags {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}

.content {
  /* 正文按纯文本渲染，保留换行 */
  white-space: pre-wrap;
  line-height: 1.8;
  font-size: 15px;
}

.actions {
  margin-top: 24px;
  display: flex;
  gap: 8px;
}

.comments {
  margin-top: 16px;
}

.comment-form__submit {
  margin-top: 12px;
}

.comment {
  padding: 12px 0;
  border-bottom: 1px solid #f0f2f5;
}

.comment--reply {
  border-bottom: none;
  padding: 8px 0 0;
}

.comment__head {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
}

.comment__time {
  color: #c0c4cc;
  font-size: 12px;
}

.comment__body {
  margin: 6px 0;
  line-height: 1.6;
  color: #303133;
}

.comment__actions {
  display: flex;
  gap: 4px;
}

.replies {
  margin-top: 8px;
  padding: 8px 12px;
  background: #fafafa;
  border-radius: 4px;
}
</style>
