<template>
  <div class="dashboard">
    <div class="page-head">
      <h1 class="page-title">运营概览</h1>
      <span class="page-sub">平台核心指标与近期增长趋势</span>
    </div>

    <el-skeleton v-if="loading" :rows="6" animated class="skeleton-card" />

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

      <div class="chart-row">
        <section class="chart-card">
          <div class="card-head">
            <h2 class="section-title">各分类课程分布</h2>
            <span class="text-gray">单位：门</span>
          </div>
          <div v-if="d.categoryDist?.length" class="bar-list">
            <div v-for="item in d.categoryDist" :key="item.name" class="bar-row">
              <span class="bar-label">{{ item.name }}</span>
              <div class="bar-track">
                <div class="bar-fill" :style="{ width: distPercent(item.value) + '%' }"></div>
              </div>
              <span class="bar-value">{{ item.value }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无分类数据" :image-size="64" />
        </section>

        <section class="chart-card">
          <div class="card-head">
            <h2 class="section-title">近 7 天新增学员</h2>
            <span class="text-gray">单位：人</span>
          </div>
          <div v-if="d.weekNewUsers?.length" class="week-chart">
            <div v-for="w in d.weekNewUsers" :key="w.date" class="week-col">
              <span class="week-count">{{ w.count }}</span>
              <div class="week-track">
                <div class="week-bar" :style="{ height: weekHeight(w.count) + '%' }"></div>
              </div>
              <span class="week-date">{{ String(w.date).substring(5) }}</span>
            </div>
          </div>
          <el-empty v-else description="近 7 天无新增学员" :image-size="64" />
        </section>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { adminApi } from '@/api'

const d = ref({})
const loading = ref(true)

const stats = computed(() => [
  { label: '学员数', value: d.value.studentCount || 0, unit: '人' },
  { label: '讲师数', value: d.value.teacherCount || 0, unit: '人' },
  { label: '课程数', value: d.value.courseCount || 0, unit: '门' },
  { label: '销售总额', value: d.value.totalRevenue || '0.00', price: true },
  { label: '待审核课程', value: d.value.pendingCount || 0, unit: '门' },
  { label: '已售订单', value: d.value.orderCount || 0, unit: '笔' },
  { label: '累计学习时长', value: d.value.totalStudyMinutes || 0, unit: '分钟' }
])

const maxCat = computed(() => Math.max(1, ...(d.value.categoryDist || []).map((i) => Number(i.value))))
const distPercent = (v) => Math.round(Number(v) * 100 / maxCat.value)
const maxWeek = computed(() => Math.max(1, ...(d.value.weekNewUsers || []).map((i) => Number(i.count))))
const weekHeight = (v) => Math.max(8, Number(v) * 100 / maxWeek.value)

onMounted(async () => {
  try {
    d.value = await adminApi.dashboard()
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
  grid-template-columns: repeat(4, 1fr);
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
.stat-item:nth-child(4n) { border-right: none; }
.stat-item:nth-last-child(-n + 3) { border-bottom: none; }
.stat-label { font-size: 13px; color: var(--xy-text-3); }
.stat-value { font-size: 24px; line-height: 1.3; font-weight: 600; color: var(--xy-text); }
.stat-value em { font-style: normal; font-size: 14px; margin-right: 2px; }
.stat-value.is-price { color: var(--xy-price); }
.stat-unit { font-size: 12px; color: var(--xy-text-3); }

.chart-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.chart-card {
  background: var(--xy-surface);
  border: 1px solid var(--xy-border);
  border-radius: var(--xy-radius);
  padding: 16px 20px 20px;
}
.card-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }

.bar-list { display: flex; flex-direction: column; gap: 12px; }
.bar-row { display: flex; align-items: center; gap: 12px; }
.bar-label { width: 84px; text-align: right; font-size: 13px; color: var(--xy-text-2); flex-shrink: 0; }
.bar-track { flex: 1; height: 8px; background: #f2f3f5; border-radius: 4px; overflow: hidden; }
.bar-fill { height: 100%; background: var(--xy-primary); border-radius: 4px; transition: width 0.2s ease; }
.bar-value { width: 36px; font-size: 12px; color: var(--xy-text-3); text-align: right; }

.week-chart {
  display: flex; align-items: flex-end; gap: 12px;
  height: 180px; padding-top: 8px;
}
.week-col {
  flex: 1; height: 100%;
  display: flex; flex-direction: column; align-items: center; justify-content: flex-end;
  gap: 4px;
}
.week-count { font-size: 12px; color: var(--xy-text-2); }
.week-track {
  width: 100%; max-width: 32px; flex: 1;
  display: flex; align-items: flex-end;
  background: #fafbfc; border-radius: 2px;
}
.week-bar { width: 100%; background: #66cba6; border-radius: 2px 2px 0 0; transition: height 0.2s ease; }
.week-date { font-size: 12px; color: var(--xy-text-3); }

@media (max-width: 1000px) {
  .stat-panel { grid-template-columns: repeat(2, 1fr); }
  .stat-item:nth-child(2n) { border-right: none; }
  .stat-item:nth-last-child(-n + 3) { border-bottom: 1px solid var(--xy-border-light); }
  .stat-item:last-child { border-bottom: none; }
  .chart-row { grid-template-columns: 1fr; }
}
@media (max-width: 600px) {
  .stat-panel { grid-template-columns: 1fr; }
  .stat-item { border-right: none; }
}
</style>
