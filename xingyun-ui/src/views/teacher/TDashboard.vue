<template>
  <div class="dashboard">
    <div class="page-head">
      <h1 class="page-title">数据看板</h1>
      <span class="page-sub">课程、学员与教学任务的实时概览</span>
    </div>

    <el-skeleton v-if="loading" :rows="5" animated class="skeleton-card" />

    <template v-else>
      <section class="stat-panel">
        <div v-for="item in stats" :key="item.label" class="stat-item">
          <span class="stat-label">{{ item.label }}</span>
          <strong class="stat-value" :class="{ 'is-price': item.price }">
            <template v-if="item.price"><em>¥</em>{{ item.value }}</template>
            <template v-else>{{ item.value }}</template>
          </strong>
          <span v-if="item.unit" class="stat-unit">{{ item.unit }}</span>
        </div>
      </section>

      <section class="todo-card">
        <div class="card-head">
          <h2 class="section-title">待处理事项</h2>
          <span class="text-gray">按最近提交排序</span>
        </div>
        <div class="todo-list">
          <button class="todo-item" @click="$router.push('/teacher/grade')">
            <div class="todo-main">
              <span class="todo-title">作业批改</span>
              <span class="todo-desc">学员提交的作业等待评分与评语</span>
            </div>
            <span class="todo-count">{{ stat.pendingGrade || 0 }}</span>
            <el-icon class="todo-arrow"><ArrowRight /></el-icon>
          </button>
          <button class="todo-item" @click="$router.push('/teacher/qa')">
            <div class="todo-main">
              <span class="todo-title">学员答疑</span>
              <span class="todo-desc">课程问答区有学员提问等待回复</span>
            </div>
            <span class="todo-count">{{ stat.pendingQa || 0 }}</span>
            <el-icon class="todo-arrow"><ArrowRight /></el-icon>
          </button>
          <button class="todo-item" @click="$router.push('/teacher/course')">
            <div class="todo-main">
              <span class="todo-title">课程管理</span>
              <span class="todo-desc">维护课程信息、章节课时与视频资源</span>
            </div>
            <el-icon class="todo-arrow"><ArrowRight /></el-icon>
          </button>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { teacherApi } from '@/api'

const stat = ref({})
const loading = ref(true)

const stats = computed(() => [
  { label: '我的课程', value: stat.value.myCourseCount || 0, unit: '门' },
  { label: '累计学员', value: stat.value.totalStudent || 0, unit: '人次' },
  { label: '累计播放', value: stat.value.totalView || 0, unit: '次' },
  { label: '待批改作业', value: stat.value.pendingGrade || 0, unit: '份' },
  { label: '待回复问答', value: stat.value.pendingQa || 0, unit: '条' },
  { label: '累计课程收入', value: stat.value.totalIncome || '0.00', price: true }
])

onMounted(async () => {
  try {
    stat.value = await teacherApi.stat()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.dashboard { display: flex; flex-direction: column; gap: 16px; }
.page-head { display: flex; align-items: baseline; gap: 10px; }
.page-sub { font-size: 13px; color: var(--xy-text-3); }
.skeleton-card { background: var(--xy-surface); border: 1px solid var(--xy-border); border-radius: var(--xy-radius); padding: 20px; }

.stat-panel {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  background: var(--xy-surface);
  border: 1px solid var(--xy-border);
  border-radius: var(--xy-radius);
  overflow: hidden;
}
.stat-item {
  padding: 16px 20px;
  border-right: 1px solid var(--xy-border-light);
  border-bottom: 1px solid var(--xy-border-light);
  display: flex; flex-direction: column; gap: 4px;
}
.stat-item:nth-child(3n) { border-right: none; }
.stat-item:nth-last-child(-n + 3) { border-bottom: none; }
.stat-label { font-size: 13px; color: var(--xy-text-3); }
.stat-value { font-size: 24px; line-height: 1.3; font-weight: 600; color: var(--xy-text); }
.stat-value em { font-style: normal; font-size: 14px; margin-right: 2px; }
.stat-value.is-price { color: var(--xy-price); }
.stat-unit { font-size: 12px; color: var(--xy-text-3); }

.todo-card {
  background: var(--xy-surface);
  border: 1px solid var(--xy-border);
  border-radius: var(--xy-radius);
  padding: 16px 20px 8px;
}
.card-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.todo-list { display: flex; flex-direction: column; }
.todo-item {
  display: flex; align-items: center; gap: 16px;
  padding: 14px 0;
  border: none; border-top: 1px solid var(--xy-border-light);
  background: transparent; cursor: pointer; text-align: left;
}
.todo-item:hover .todo-title { color: var(--xy-primary); }
.todo-main { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.todo-title { font-size: 14px; color: var(--xy-text); transition: color 0.15s; }
.todo-desc { font-size: 12px; color: var(--xy-text-3); }
.todo-count { font-size: 20px; font-weight: 600; color: var(--xy-text); }
.todo-arrow { color: var(--xy-text-3); font-size: 14px; }

@media (max-width: 900px) {
  .stat-panel { grid-template-columns: repeat(2, 1fr); }
  .stat-item:nth-child(3n) { border-right: 1px solid var(--xy-border-light); }
  .stat-item:nth-child(2n) { border-right: none; }
  .stat-item:nth-last-child(-n + 3) { border-bottom: 1px solid var(--xy-border-light); }
  .stat-item:nth-last-child(-n + 2) { border-bottom: none; }
}
</style>
