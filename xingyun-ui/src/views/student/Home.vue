<template>
  <div class="home">
    <!-- 首屏：业务信息 + 搜索 + 主推课程，不使用氛围渐变 -->
    <section class="hero">
      <div class="hero-copy">
        <div class="hero-kicker">星云课堂 · 职业技能学习平台</div>
        <h1>跟着一线讲师，系统学好一门技术</h1>
        <p class="hero-desc">全栈开发 · 人工智能 · 数据科学 · 设计办公，12 门课程，付费课前 3 节免费试看</p>
        <div class="hero-search">
          <el-input
            v-model="keyword"
            size="large"
            placeholder="搜索课程名称或技术方向"
            clearable
            @keyup.enter="search"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
            <template #append><el-button @click="search">搜索</el-button></template>
          </el-input>
        </div>
        <div class="hot-words" v-if="hotList.length">
          <span class="hot-label">热门搜索</span>
          <button v-for="c in hotList.slice(0, 3)" :key="c.id" class="hot-word" @click="goCourse(c.id)">
            {{ c.courseName }}
          </button>
        </div>
      </div>

      <div v-if="featured" class="hero-feature" @click="goCourse(featured.id)">
        <div class="feature-cover">
          <img v-if="featured.cover && !featureCoverError" :src="featured.cover" :alt="featured.courseName" @error="featureCoverError = true" />
          <div v-else class="feature-cover-fallback">{{ featured.courseName?.charAt(0) }}</div>
        </div>
        <div class="feature-body">
          <span class="feature-label">本周热门</span>
          <h3 class="ellipsis">{{ featured.courseName }}</h3>
          <p>{{ featured.teacherName || '星云讲师' }} · {{ featured.studentCount || 0 }} 人在学</p>
          <span class="feature-link">查看课程<el-icon><ArrowRight /></el-icon></span>
        </div>
      </div>
    </section>

    <!-- 公告：单行滚动，点击查看详情 -->
    <div v-if="notices.length" class="notice-bar">
      <span class="notice-label">公告</span>
      <el-carousel
        height="22px"
        direction="vertical"
        :autoplay="true"
        :interval="5000"
        arrow="never"
        indicator-position="none"
        class="notice-scroll"
      >
        <el-carousel-item v-for="n in notices" :key="n.id">
          <button class="notice-text" :title="n.title" @click="openNotice(n)">{{ n.title }}</button>
        </el-carousel-item>
      </el-carousel>
      <button class="notice-more" @click="noticeListVisible = true">全部公告</button>
    </div>

    <el-dialog v-model="noticeVisible" :title="currentNotice.title || '公告详情'" width="520px">
      <div class="notice-meta">
        <el-tag size="small" :type="currentNotice.type === 2 ? 'warning' : 'info'">
          {{ currentNotice.type === 2 ? '课程通知' : '系统公告' }}
        </el-tag>
        <span class="text-gray">{{ minuteText(currentNotice.publishTime || currentNotice.createTime) }}</span>
      </div>
      <div class="notice-content">{{ currentNotice.content }}</div>
    </el-dialog>

    <el-dialog v-model="noticeListVisible" title="全部公告" width="520px">
      <el-scrollbar max-height="380px">
        <button v-for="n in notices" :key="n.id" class="notice-row" @click="openNotice(n)">
          <span class="notice-row-title ellipsis">{{ n.title }}</span>
          <span class="text-gray">{{ minuteText(n.publishTime || n.createTime) }}</span>
        </button>
      </el-scrollbar>
    </el-dialog>

    <!-- 分类入口：文字主导，信息密度优先 -->
    <section class="category-section">
      <div class="section-head">
        <h2 class="section-title">课程分类</h2>
        <button class="head-link" @click="$router.push('/courses')">全部分类<el-icon><ArrowRight /></el-icon></button>
      </div>
      <el-skeleton v-if="loading" :rows="2" animated />
      <div v-else class="category-list">
        <button v-for="c in categories" :key="c.id" class="category-item" @click="goCategory(c.id)">
          <span class="category-name">{{ c.name }}</span>
          <span class="category-sub">{{ c.children?.length || 0 }} 个方向</span>
        </button>
      </div>
    </section>

    <section class="course-section">
      <div class="section-head">
        <h2 class="section-title">热门课程</h2>
        <button class="head-link" @click="$router.push('/courses')">查看全部<el-icon><ArrowRight /></el-icon></button>
      </div>
      <el-skeleton v-if="loading" :rows="5" animated />
      <div v-else class="course-grid">
        <CourseCard v-for="c in hotList" :key="c.id" :course="c" />
      </div>
    </section>

    <!-- 平台能力：真实业务承诺，不做装饰性插图 -->
    <section class="assurance">
      <div class="assurance-item">
        <el-icon><VideoPlay /></el-icon>
        <div><strong>免费试看</strong><span>付费课程前 3 节可直接学习</span></div>
      </div>
      <div class="assurance-item">
        <el-icon><Clock /></el-icon>
        <div><strong>断点续播</strong><span>学习进度云端记录，随时继续</span></div>
      </div>
      <div class="assurance-item">
        <el-icon><ChatDotRound /></el-icon>
        <div><strong>讲师答疑</strong><span>课程问答区由讲师亲自回复</span></div>
      </div>
      <div class="assurance-item">
        <el-icon><Medal /></el-icon>
        <div><strong>学完发证</strong><span>完成全部课时自动颁发证书</span></div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import CourseCard from '@/components/CourseCard.vue'
import { categoryApi, courseApi, announceApi } from '@/api'
import { minuteText } from '@/utils/format'

const router = useRouter()
const keyword = ref('')
const categories = ref([])
const hotList = ref([])
const notices = ref([])
const loading = ref(true)
const featureCoverError = ref(false)
const noticeVisible = ref(false)
const noticeListVisible = ref(false)
const currentNotice = reactive({})

const featured = computed(() => hotList.value[0] || null)

function search() {
  router.push({ path: '/courses', query: keyword.value ? { keyword: keyword.value } : {} })
}
function goCategory(id) {
  router.push({ path: '/courses', query: { categoryId: id } })
}
function goCourse(id) {
  router.push(`/course/${id}`)
}
function openNotice(n) {
  Object.assign(currentNotice, n)
  noticeListVisible.value = false
  noticeVisible.value = true
}

onMounted(async () => {
  try {
    const [cats, hots, anns] = await Promise.all([
      categoryApi.tree(),
      courseApi.hot(8),
      announceApi.list()
    ])
    categories.value = cats || []
    hotList.value = hots || []
    notices.value = anns || []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.home { display: flex; flex-direction: column; gap: 16px; }

.hero {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(280px, 0.9fr);
  gap: 24px;
  padding: 28px 28px;
  background: #f7faf8;
  border: 1px solid var(--xy-border);
  border-radius: var(--xy-radius);
}
.hero-copy { display: flex; flex-direction: column; justify-content: center; }
.hero-kicker { font-size: 12px; color: var(--xy-primary); letter-spacing: 0.5px; margin-bottom: 8px; }
.hero h1 { font-size: 28px; line-height: 1.4; font-weight: 600; color: var(--xy-text); letter-spacing: 0.3px; }
.hero-desc { margin-top: 10px; font-size: 14px; color: var(--xy-text-2); }
.hero-search { margin-top: 18px; max-width: 520px; }
.hero-search :deep(.el-input__wrapper) { background: #fff; }
.hero-search :deep(.el-input-group__append) { background: var(--xy-primary); border-color: var(--xy-primary); }
.hero-search :deep(.el-input-group__append .el-button) { color: #fff; margin: 0; }
.hot-words { margin-top: 12px; display: flex; flex-wrap: wrap; gap: 10px; align-items: center; }
.hot-label { font-size: 12px; color: var(--xy-text-3); }
.hot-word {
  border: none; background: transparent; padding: 0;
  font-size: 12px; color: var(--xy-text-2); cursor: pointer;
}
.hot-word:hover { color: var(--xy-primary); }

.hero-feature {
  display: flex; flex-direction: column;
  background: #fff; border: 1px solid var(--xy-border);
  border-radius: var(--xy-radius); overflow: hidden; cursor: pointer;
  transition: border-color 0.15s ease;
}
.hero-feature:hover { border-color: #b3e7d3; }
.feature-cover { aspect-ratio: 16 / 9; background: #f2f3f5; overflow: hidden; }
.feature-cover img { width: 100%; height: 100%; object-fit: cover; }
.feature-cover-fallback {
  width: 100%; height: 100%; display: flex; align-items: center; justify-content: center;
  font-size: 32px; font-weight: 600; color: #b3e7d3;
}
.feature-body { padding: 12px 14px 14px; }
.feature-label { font-size: 11px; color: var(--xy-primary); }
.feature-body h3 { margin-top: 4px; font-size: 15px; font-weight: 500; color: var(--xy-text); }
.feature-body p { margin-top: 4px; font-size: 12px; color: var(--xy-text-3); }
.feature-link {
  margin-top: 8px; display: inline-flex; align-items: center; gap: 2px;
  font-size: 12px; color: var(--xy-primary);
}

.notice-bar {
  display: flex; align-items: center; gap: 12px;
  height: 38px; padding: 0 14px;
  background: var(--xy-surface); border: 1px solid var(--xy-border);
  border-radius: var(--xy-radius);
}
.notice-label { font-size: 12px; color: var(--xy-primary); font-weight: 500; flex-shrink: 0; }
.notice-scroll { flex: 1; }
.notice-text {
  width: 100%; text-align: left; border: none; background: transparent;
  font-size: 13px; color: var(--xy-text-2); cursor: pointer;
  line-height: 22px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.notice-text:hover { color: var(--xy-primary); }
.notice-more {
  border: none; background: transparent; font-size: 12px;
  color: var(--xy-text-3); cursor: pointer; flex-shrink: 0;
}
.notice-more:hover { color: var(--xy-primary); }
.notice-meta { display: flex; gap: 10px; align-items: center; margin-bottom: 12px; }
.notice-content { line-height: 1.9; color: var(--xy-text-2); white-space: pre-wrap; font-size: 14px; }
.notice-row {
  width: 100%; display: flex; align-items: center; justify-content: space-between; gap: 12px;
  padding: 10px 4px; border: none; border-bottom: 1px solid var(--xy-border-light);
  background: transparent; cursor: pointer; text-align: left;
}
.notice-row:hover .notice-row-title { color: var(--xy-primary); }
.notice-row-title { font-size: 13px; color: var(--xy-text); }

.section-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.head-link {
  display: inline-flex; align-items: center; gap: 2px;
  border: none; background: transparent; cursor: pointer;
  font-size: 12px; color: var(--xy-text-3);
}
.head-link:hover { color: var(--xy-primary); }

.category-section {
  background: var(--xy-surface); border: 1px solid var(--xy-border);
  border-radius: var(--xy-radius); padding: 16px;
}
.category-list { display: grid; grid-template-columns: repeat(6, 1fr); gap: 8px; }
.category-item {
  display: flex; flex-direction: column; gap: 2px;
  padding: 10px 12px; text-align: left;
  border: 1px solid var(--xy-border-light); border-radius: var(--xy-radius-sm);
  background: #fafbfc; cursor: pointer;
  transition: background-color 0.15s, border-color 0.15s;
}
.category-item:hover { background: var(--xy-primary-light); border-color: #b3e7d3; }
.category-name { font-size: 14px; font-weight: 500; color: var(--xy-text); }
.category-sub { font-size: 12px; color: var(--xy-text-3); }

.course-section {
  background: var(--xy-surface); border: 1px solid var(--xy-border);
  border-radius: var(--xy-radius); padding: 16px;
}
.course-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 16px; }

.assurance {
  display: grid; grid-template-columns: repeat(4, 1fr);
  background: var(--xy-surface); border: 1px solid var(--xy-border);
  border-radius: var(--xy-radius); padding: 16px 8px;
}
.assurance-item {
  display: flex; align-items: flex-start; gap: 10px;
  padding: 4px 16px; border-right: 1px solid var(--xy-border-light);
}
.assurance-item:last-child { border-right: none; }
.assurance-item > .el-icon { font-size: 20px; color: var(--xy-primary); margin-top: 2px; }
.assurance-item strong { display: block; font-size: 13px; font-weight: 500; color: var(--xy-text); }
.assurance-item span { font-size: 12px; color: var(--xy-text-3); }

@media (max-width: 1000px) {
  .hero { grid-template-columns: 1fr; }
  .hero-feature { display: none; }
  .course-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
  .category-list { grid-template-columns: repeat(3, 1fr); }
  .assurance { grid-template-columns: repeat(2, 1fr); gap: 12px 0; }
  .assurance-item:nth-child(2) { border-right: none; }
}
@media (max-width: 640px) {
  .hero { padding: 20px 16px; }
  .hero h1 { font-size: 22px; }
  .course-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; }
  .category-list { grid-template-columns: repeat(2, 1fr); }
  .assurance { grid-template-columns: 1fr; }
  .assurance-item { border-right: none; }
}
</style>
