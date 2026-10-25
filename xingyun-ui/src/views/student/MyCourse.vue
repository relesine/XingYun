<template>
  <div class="my-course">
    <div class="page-head">
      <div>
        <h1 class="page-title">我的学习</h1>
        <p class="page-sub">学习进度实时保存，可在任意设备继续上次的课时</p>
      </div>
      <div class="filter-tabs">
        <button v-for="item in filters" :key="item.value" class="filter-tab" :class="{ active: filter === item.value }"
          @click="filter = item.value">{{ item.label }}</button>
      </div>
    </div>

    <div class="content-card">
      <el-empty v-if="!showList.length" description="还没有学习中的课程，先去选一门感兴趣的课">
        <el-button type="primary" @click="$router.push('/courses')">去选课</el-button>
      </el-empty>

      <div v-else class="course-list">
        <div v-for="c in showList" :key="c.id" class="course-row">
          <div class="mini-cover" @click="$router.push(`/course/${c.id}`)">
            <span>{{ c.courseName?.charAt(0) }}</span>
            <img v-if="c.cover && !errSet.has(c.id)" :src="c.cover" :alt="c.courseName" @error="errSet.add(c.id)" />
          </div>

          <div class="row-main">
            <div class="row-title" @click="$router.push(`/course/${c.id}`)">{{ c.courseName }}</div>
            <div class="row-meta">
              <el-tag size="small" :type="c.source === '免费学习' ? 'success' : 'info'">{{ c.source || '已购买' }}</el-tag>
              <span class="text-gray">已完成 {{ c.finishedLessons || 0 }}/{{ c.lessonCount }} 节</span>
            </div>
            <div class="progress-line">
              <el-progress :percentage="c.courseProgress || 0" :stroke-width="6" :show-text="false" />
              <span class="progress-text">{{ c.courseProgress || 0 }}%</span>
            </div>
          </div>

          <div class="row-actions">
            <el-button type="primary" size="small" @click="continueLearn(c)">
              {{ (c.courseProgress || 0) > 0 ? '继续学习' : '开始学习' }}
            </el-button>
            <el-button size="small" @click="$router.push(`/course/${c.id}`)">课程详情</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { courseApi, learnApi } from '@/api'

const router = useRouter()
const list = ref([])
const filter = ref('all')
const filters = [
  { label: '全部', value: 'all' },
  { label: '学习中', value: 'learning' },
  { label: '已学完', value: 'finished' }
]
// 封面加载失败的课程ID集合（失败后回退显示课程首字母，不再重复加载裂图）
const errSet = ref(new Set())
const showList = computed(() => {
  if (filter.value === 'learning') return list.value.filter((c) => (c.courseProgress || 0) < 100)
  if (filter.value === 'finished') return list.value.filter((c) => (c.courseProgress || 0) >= 100)
  return list.value
})
async function continueLearn(c) {
  // 调续播接口，定位到上次课时；没有则进详情
  try {
    const info = await learnApi.continueLesson(c.id)
    if (info?.lessonId) return router.push(`/learn/${c.id}?lessonId=${info.lessonId}`)
  } catch (e) { }
  router.push(`/learn/${c.id}`)
}
onMounted(async () => { list.value = await courseApi.my() })
</script>

<style scoped>
.my-course {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
}

.page-sub {
  margin-top: 4px;
  font-size: 13px;
  color: var(--xy-text-3);
}

.filter-tabs {
  display: flex;
  gap: 20px;
}

.filter-tab {
  border: none;
  background: transparent;
  padding: 0 0 4px;
  font-size: 13px;
  color: var(--xy-text-2);
  cursor: pointer;
}

.filter-tab.active {
  color: var(--xy-primary);
  font-weight: 500;
}

.content-card {
  background: var(--xy-surface);
  border: 1px solid var(--xy-border);
  border-radius: var(--xy-radius);
  padding: 4px 16px;
}

.course-list {
  display: flex;
  flex-direction: column;
}

.course-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid var(--xy-border-light);
}

.course-row:last-child {
  border-bottom: none;
}

.mini-cover {
  width: 120px;
  height: 68px;
  flex-shrink: 0;
  border-radius: var(--xy-radius-sm);
  overflow: hidden;
  background: #f2f3f5;
  position: relative;
  cursor: pointer;
}

.mini-cover img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.mini-cover span {
  display: flex;
  width: 100%;
  height: 100%;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  font-weight: 600;
  color: #b3e7d3;
}

.row-main {
  flex: 1;
  min-width: 0;
}

.row-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--xy-text);
  cursor: pointer;
}

.row-title:hover {
  color: var(--xy-primary);
}

.row-meta {
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.progress-line {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
  max-width: 420px;
}

.progress-line .el-progress {
  flex: 1;
}

.progress-text {
  font-size: 12px;
  color: var(--xy-text-3);
  width: 36px;
}

.row-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

@media (max-width: 720px) {
  .page-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .course-row {
    flex-wrap: wrap;
  }

  .mini-cover {
    width: 96px;
    height: 56px;
  }

  .row-actions {
    width: 100%;
    padding-left: 112px;
  }
}
</style>
