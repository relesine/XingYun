<template>
  <div class="cert-page">
    <div class="page-head">
      <h1 class="page-title">我的结业证书</h1>
      <p class="page-sub">完成课程全部课时后自动颁发</p>
    </div>

    <div class="content-card">
      <el-empty v-if="!list.length" description="还没有证书，完成一门课程即可获得" />

      <div v-else class="cert-grid">
        <article v-for="c in list" :key="c.id" class="cert-card">
          <div class="cert-inner">
            <span class="cert-badge">结业证书</span>
            <div class="course">{{ c.courseName || c.certificateName }}</div>
            <div class="no">证书编号：{{ c.certNo }}</div>
            <div class="who">学员：{{ c.studentName }}</div>
            <div class="time">颁发时间：{{ minuteText(c.issueTime) }}</div>
            <el-button type="primary" plain size="small" class="mt12" @click="view(c)">查看证书</el-button>
          </div>
        </article>
      </div>
    </div>

    <el-dialog v-model="dialog" :title="cur?.courseName" width="640px">
      <div v-if="cur" class="cert-large">
        <div class="cl-title">星云在线教育平台 · 结业证书</div>
        <div class="cl-line">兹证明学员 <b>{{ cur.studentName }}</b> 已完成《{{ cur.courseName }}》全部课时学习，成绩合格，特发此证。</div>
        <div class="cl-foot">
          <span>证书编号：{{ cur.certNo }}</span>
          <span>颁发日期：{{ minuteText(cur.issueTime) }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { certApi } from '@/api'
import { minuteText } from '@/utils/format'
const list = ref([])
const dialog = ref(false)
const cur = ref(null)
function view(c) { cur.value = c; dialog.value = true }
onMounted(async () => { list.value = await certApi.my() })
</script>

<style scoped>
.cert-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-head {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.page-sub {
  font-size: 13px;
  color: var(--xy-text-3);
}

.content-card {
  background: var(--xy-surface);
  border: 1px solid var(--xy-border);
  border-radius: var(--xy-radius);
  padding: 16px;
}

.cert-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.cert-card {
  border: 1px solid #e8dcc0;
  border-radius: var(--xy-radius);
  background: #fffdf8;
}

.cert-inner {
  text-align: center;
  padding: 20px 16px;
}

.cert-badge {
  display: inline-block;
  padding: 2px 10px;
  font-size: 12px;
  color: #8a6d1f;
  border: 1px solid #e8dcc0;
  border-radius: 2px;
  background: #fffaf0;
}

.course {
  font-size: 15px;
  font-weight: 600;
  margin: 10px 0 8px;
  color: var(--xy-text);
}

.no,
.who,
.time {
  color: var(--xy-text-3);
  font-size: 12px;
  margin-top: 4px;
}

.cert-large {
  border: 3px double #d4af37;
  border-radius: 4px;
  padding: 40px 30px;
  text-align: center;
  background: #fffdf8;
}

.cl-title {
  font-size: 21px;
  font-weight: 600;
  color: #8a6d1f;
  margin-bottom: 24px;
  letter-spacing: 3px;
}

.cl-line {
  font-size: 15px;
  line-height: 2;
  color: var(--xy-text);
}

.cl-foot {
  display: flex;
  justify-content: space-between;
  margin-top: 36px;
  color: var(--xy-text-3);
  font-size: 13px;
}

@media (max-width: 900px) {
  .cert-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 600px) {
  .cert-grid {
    grid-template-columns: 1fr;
  }
}
</style>
