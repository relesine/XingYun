<template>
  <div class="page-card" v-loading="loading">
    <div class="page-head">
      <h1 class="page-title">错题本</h1>
      <span class="page-sub">随堂测验答错的题目与解析</span>
    </div>
    <div class="toolbar">
      <span class="text-gray">随堂测验答错的题会自动收录；重新做对后自动移除</span>
      <span class="flex-grow"></span>
      <el-tag type="danger" size="large">错题 {{ list.length }} 道</el-tag>
      <el-button type="primary" @click="load">刷新</el-button>
      <el-button @click="$router.push('/courses')">去课程复习</el-button>
    </div>

    <el-empty v-if="!loading && list.length === 0" description="太棒了，当前没有错题~" />

    <div class="wrong-item" v-for="(item, idx) in list" :key="item.wrong.id || idx">
      <div class="q-head">
        <el-tag size="small" :type="typeTag(item.question.questionType)">{{ typeText(item.question.questionType) }}</el-tag>
        <span class="q-content">{{ idx + 1 }}. {{ item.question.content }}</span>
      </div>
      <ul class="options">
        <li v-for="op in optionList(item.question)" :key="op.key"
          :class="{ right: isRightOp(item.question, op.key) }">
          <b>{{ op.key }}.</b> {{ op.text }}
          <el-tag v-if="isRightOp(item.question, op.key)" type="success" size="small" class="ml8">正确答案</el-tag>
        </li>
      </ul>
      <div class="q-foot">
        <span class="text-gray">正确答案：<b class="green">{{ answerText(item.question) }}</b>（{{ item.question.score }}分）</span>
        <span class="text-gray">错误 {{ item.wrong.wrongCount }} 次 · 最近 {{ minuteText(item.wrong.lastWrongTime) }}</span>
      </div>
      <el-alert v-if="item.question.analysis" class="mt12" type="info" :closable="false" show-icon
        :title="`解析：${item.question.analysis}`" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { quizApi } from '@/api'
import { minuteText } from '@/utils/format'

const loading = ref(false)
const list = ref([])

const typeText = (t) => ({ 1: '单选题', 2: '多选题', 3: '判断题' }[t] || '题目')
const typeTag = (t) => ({ 1: 'primary', 2: 'warning', 3: 'success' }[t] || 'info')

// 把 optionA~D 组装成列表，空选项自动过滤
function optionList(q) {
  return ['A', 'B', 'C', 'D']
    .map((k) => ({ key: k, text: q[`option${k}`] }))
    .filter((o) => o.text !== null && o.text !== undefined && o.text !== '')
}
// 判断题后端答案存 TRUE/FALSE，映射到选项 A(正确)/B(错误)
function judgeAnswerKey(q) {
  const a = String(q.answer || '').toUpperCase()
  if (q.questionType === 3) return a === 'TRUE' || a === 'A' ? 'A' : 'B'
  return a
}
// 判断某选项是否为正确选项；选择题答案为字母串（如 AB），包含即可
function isRightOp(q, key) {
  return judgeAnswerKey(q).includes(key)
}
function answerText(q) {
  if (q.questionType === 3) return judgeAnswerKey(q) === 'A' ? '正确' : '错误'
  return q.answer
}

async function load() {
  loading.value = true
  try {
    list.value = await quizApi.wrongBook()
  } catch (e) {
    ElMessage.error('错题本加载失败')
  } finally {
    loading.value = false
  }
}
onMounted(load)
</script>

<style scoped>
.wrong-item { border: 1px solid #ebeef5; border-radius: 8px; padding: 14px 16px; margin-bottom: 14px; background: #fcfcfd; }
.q-head { display: flex; align-items: flex-start; gap: 8px; font-size: 15px; font-weight: 600; line-height: 24px; }
.q-content { flex: 1; }
.options { list-style: none; margin: 10px 0 6px; padding-left: 8px; }
.options li { padding: 6px 10px; border-radius: 6px; margin-bottom: 6px; color: #606266; }
.options li.right { background: #f0f9eb; color: #67c23a; font-weight: 600; }
.ml8 { margin-left: 8px; }
.green { color: #67c23a; }
.q-foot { display: flex; justify-content: space-between; flex-wrap: wrap; gap: 8px; }
</style>
