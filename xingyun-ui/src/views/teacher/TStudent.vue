<template>
  <div class="page-card">
    <div class="page-head">
      <h1 class="page-title">学员进度</h1>
      <span class="page-sub">选择课程查看学员学习进度</span>
    </div>
    <div class="toolbar">
      <el-select v-model="courseId" placeholder="请选择我的课程" style="width: 300px" filterable @change="loadStudents">
        <el-option v-for="c in courses" :key="c.id" :value="c.id" :label="c.courseName" />
      </el-select>
      <el-button type="primary" :disabled="!courseId" @click="loadStudents">刷新</el-button>
    </div>

    <el-row :gutter="14" class="mb12" v-if="courseId">
      <el-col :span="8">
        <div class="stat-card">
          <div class="num">{{ students.length }}</div>
          <div class="lbl">报名 / 学习人数</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="num green">{{ finishedCount }}</div>
          <div class="lbl">已学完全部课时</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="num blue">{{ avgProgress }}%</div>
          <div class="lbl">平均学习进度</div>
        </div>
      </el-col>
    </el-row>

    <el-empty v-if="!courseId" description="请先选择一门课程查看学员进度" />
    <el-empty v-else-if="!loading && students.length === 0" description="该课程还没有学员报名或学习" />

    <el-table v-if="courseId" :data="students" v-loading="loading">
      <el-table-column label="学员" min-width="170">
        <template #default="{ row }">
          <el-avatar :size="32" :src="row.avatar">{{ row.nickname?.charAt(0) }}</el-avatar>
          <span class="ml8">{{ row.nickname }}</span>
        </template>
      </el-table-column>
      <el-table-column label="课时完成" width="120" align="center">
        <template #default="{ row }">{{ row.finishedLessons }} / {{ row.totalLessons }}</template>
      </el-table-column>
      <el-table-column label="学习进度" min-width="200">
        <template #default="{ row }">
          <el-progress :percentage="Number(row.progress) || 0" :status="row.finishedAll ? 'success' : ''" />
        </template>
      </el-table-column>
      <el-table-column prop="learnedMinutes" label="累计学习(分)" width="120" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.finishedAll" type="success">已学完</el-tag>
          <el-tag v-else-if="row.lastLearnTime" type="warning">学习中</el-tag>
          <el-tag v-else type="info">未开始</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="最近学习" width="160">
        <template #default="{ row }">{{ minuteText(row.lastLearnTime) }}</template>
      </el-table-column>
      <el-table-column label="报名时间" width="160">
        <template #default="{ row }">{{ row.enrollTime ? minuteText(row.enrollTime) : '免费课/未购' }}</template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { teacherApi } from '@/api'
import { minuteText } from '@/utils/format'

const route = useRoute()
const courses = ref([])
const courseId = ref(null)
const students = ref([])
const loading = ref(false)

const finishedCount = computed(() => students.value.filter((s) => s.finishedAll).length)
const avgProgress = computed(() => {
  if (!students.value.length) return 0
  const sum = students.value.reduce((acc, s) => acc + (Number(s.progress) || 0), 0)
  return Math.round(sum / students.value.length)
})

async function loadCourses() {
  // 拉取讲师自己的全部课程（给大一点的 size 避免分页遗漏）
  const page = await teacherApi.coursePage({ page: 1, size: 100 })
  courses.value = page.records || []
  // 支持从课程管理带 courseId 跳转过来
  const qid = Number(route.query.courseId)
  if (qid && courses.value.some((c) => c.id === qid)) {
    courseId.value = qid
  } else if (courses.value.length) {
    courseId.value = courses.value[0].id
  }
  if (courseId.value) loadStudents()
}

async function loadStudents() {
  if (!courseId.value) return
  loading.value = true
  try {
    students.value = await teacherApi.courseStudents(courseId.value)
  } catch (e) {
    ElMessage.error('加载失败，可能该课程不属于你')
    students.value = []
  } finally {
    loading.value = false
  }
}

onMounted(loadCourses)
</script>

<style scoped>
.stat-card {
  background: #f6f9ff;
  border-radius: 8px;
  padding: 14px 18px;
  text-align: center;
}

.stat-card .num {
  font-size: 26px;
  font-weight: bold;
  color: #409eff;
}

.stat-card .num.green {
  color: #67c23a;
}

.stat-card .num.blue {
  color: #1e6fe0;
}

.stat-card .lbl {
  color: #909399;
  font-size: 13px;
  margin-top: 4px;
}

.ml8 {
  margin-left: 8px;
}
</style>
