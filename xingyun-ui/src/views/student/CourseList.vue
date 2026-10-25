<template>
  <div class="course-page">
    <div class="page-head">
      <div>
        <h1 class="page-title">全部课程</h1>
        <p class="page-sub">按技术方向筛选课程，免费课程可直接学习，付费课前 3 节支持试看</p>
      </div>
      <span class="result-count">共 {{ total }} 门课程</span>
    </div>

    <div class="course-layout">
      <aside class="category-panel">
        <div class="panel-title">课程方向</div>
        <el-menu :default-active="activeCategory" class="category-menu" @select="onSelectCate">
          <el-menu-item index="">全部课程</el-menu-item>
          <template v-for="c in categories" :key="c.id">
            <el-sub-menu :index="`g${c.id}`">
              <template #title>{{ c.name }}</template>
              <el-menu-item :index="String(c.id)">全部 · {{ c.name }}</el-menu-item>
              <el-menu-item v-for="ch in c.children" :key="ch.id" :index="String(ch.id)">{{ ch.name }}</el-menu-item>
            </el-sub-menu>
          </template>
        </el-menu>
      </aside>

      <section class="list-panel">
        <div class="list-toolbar">
          <div class="sort-tabs">
            <button
              v-for="item in sortOptions"
              :key="item.value"
              class="sort-tab"
              :class="{ active: query.sort === item.value }"
              @click="changeSort(item.value)"
            >{{ item.label }}</button>
          </div>
          <div class="toolbar-right">
            <el-input
              v-model="query.keyword"
              placeholder="搜索课程名称"
              clearable
              class="keyword-input"
              @keyup.enter="reload"
              @clear="reload"
            >
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-button type="primary" @click="reload">筛选</el-button>
          </div>
        </div>

        <el-skeleton v-if="loading" :rows="6" animated />

        <template v-else>
          <div v-if="list.length" class="course-grid">
            <CourseCard v-for="c in list" :key="c.id" :course="c" />
          </div>
          <div v-else class="empty-wrap">
            <el-empty description="没有找到符合条件的课程">
              <el-button @click="resetFilter">清空筛选条件</el-button>
            </el-empty>
          </div>

          <div v-if="total > query.size" class="pager">
            <el-pagination
              layout="prev, pager, next, total"
              :total="total"
              :page-size="query.size"
              :current-page="query.page"
              @current-change="onPage"
            />
          </div>
        </template>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import CourseCard from '@/components/CourseCard.vue'
import { categoryApi, courseApi } from '@/api'

const route = useRoute()
const categories = ref([])
const list = ref([])
const total = ref(0)
const loading = ref(true)
const query = reactive({
  page: 1, size: 12, keyword: '', categoryId: null, sort: 'hot'
})
const sortOptions = [
  { label: '综合排序', value: 'hot' },
  { label: '最新开课', value: 'new' },
  { label: '价格从低到高', value: 'priceAsc' },
  { label: '价格从高到低', value: 'priceDesc' }
]
const activeCategory = computed(() => (query.categoryId ? String(query.categoryId) : ''))

async function load() {
  loading.value = true
  try {
    const data = await courseApi.page({ ...query })
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}
function reload() { query.page = 1; load() }
function onPage(p) { query.page = p; load() }
function changeSort(sort) { query.sort = sort; reload() }
function onSelectCate(idx) {
  query.categoryId = idx ? Number(idx) : null
  reload()
}
function resetFilter() {
  query.keyword = ''
  query.categoryId = null
  query.sort = 'hot'
  reload()
}

function syncFromRoute() {
  query.keyword = route.query.keyword ? String(route.query.keyword) : ''
  query.categoryId = route.query.categoryId ? Number(route.query.categoryId) : null
  query.page = 1
}

onMounted(async () => {
  categories.value = (await categoryApi.tree()) || []
  syncFromRoute()
  load()
})

// 顶部导航搜索在同一路由下变更 query 时也能刷新列表
watch(() => route.query, () => {
  if (!categories.value.length) return
  syncFromRoute()
  load()
})
</script>

<style scoped>
.course-page { display: flex; flex-direction: column; gap: 16px; }
.page-head { display: flex; align-items: flex-end; justify-content: space-between; }
.page-sub { margin-top: 4px; font-size: 13px; color: var(--xy-text-3); }
.result-count { font-size: 13px; color: var(--xy-text-3); }

.course-layout {
  display: grid;
  grid-template-columns: 200px minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.category-panel {
  background: var(--xy-surface);
  border: 1px solid var(--xy-border);
  border-radius: var(--xy-radius);
  padding: 4px 0 8px;
  position: sticky;
  top: 76px;
}
.panel-title {
  padding: 12px 16px 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--xy-text);
}
.category-menu { border-right: none; }
.category-menu :deep(.el-menu-item) { height: 36px; line-height: 36px; font-size: 13px; padding-left: 16px !important; }
.category-menu :deep(.el-sub-menu__title) { height: 36px; line-height: 36px; font-size: 13px; padding-left: 16px !important; }
.category-menu :deep(.el-menu-item.is-active) { background: var(--xy-primary-light) !important; }

.list-panel {
  background: var(--xy-surface);
  border: 1px solid var(--xy-border);
  border-radius: var(--xy-radius);
  padding: 16px;
  min-height: 420px;
}
.list-toolbar {
  display: flex; align-items: center; justify-content: space-between; gap: 16px;
  padding-bottom: 12px; margin-bottom: 16px;
  border-bottom: 1px solid var(--xy-border-light);
}
.sort-tabs { display: flex; align-items: center; gap: 20px; }
.sort-tab {
  border: none; background: transparent; padding: 0;
  font-size: 13px; color: var(--xy-text-2); cursor: pointer;
  transition: color 0.15s;
}
.sort-tab:hover { color: var(--xy-text); }
.sort-tab.active { color: var(--xy-primary); font-weight: 500; }
.toolbar-right { display: flex; align-items: center; gap: 8px; }
.keyword-input { width: 200px; }

.course-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}
.empty-wrap { padding: 48px 0; }
.pager { display: flex; justify-content: flex-end; margin-top: 20px; }

@media (max-width: 1000px) {
  .course-layout { grid-template-columns: 1fr; }
  .category-panel { position: static; }
  .category-menu { display: flex; flex-wrap: wrap; }
  .category-menu :deep(.el-sub-menu__title) { display: none; }
  .course-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
@media (max-width: 640px) {
  .page-head { flex-direction: column; align-items: flex-start; gap: 4px; }
  .list-toolbar { flex-direction: column; align-items: stretch; }
  .sort-tabs { overflow-x: auto; }
  .toolbar-right { justify-content: space-between; }
  .keyword-input { flex: 1; }
  .course-grid { grid-template-columns: 1fr; }
}
</style>
