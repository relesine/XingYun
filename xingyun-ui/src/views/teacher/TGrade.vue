<template>
  <div class="page-card">
    <div class="page-head">
      <h1 class="page-title">作业批改</h1>
      <span class="page-sub">批改学员提交的课程作业</span>
    </div>
    <div class="toolbar">
      <el-radio-group v-model="gradeStatus" @change="load">
        <el-radio-button :label="0">待批改</el-radio-button>
        <el-radio-button :label="1">已批改</el-radio-button>
        <el-radio-button :label="2">已打回</el-radio-button>
        <el-radio-button :label="''">全部</el-radio-button>
      </el-radio-group>
    </div>

    <el-table :data="list">
      <el-table-column prop="studentName" label="学员" width="120" />
      <el-table-column prop="courseName" label="课程" min-width="160" />
      <el-table-column prop="assignmentTitle" label="作业" min-width="140" />
      <el-table-column label="作答内容" min-width="220">
        <template #default="{ row }">
          <el-link type="primary" @click="viewContent(row)">{{ row.submission.content?.substring(0, 20) || '(无)'
            }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="提交时间" width="160">
        <template #default="{ row }">{{ minuteText(row.submission.submitTime) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="['warning', 'success', 'danger'][row.submission.gradeStatus]">
            {{ ['待批改', '已批改', '已打回'][row.submission.gradeStatus] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="得分" width="80">
        <template #default="{ row }">{{ row.submission.score ?? '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="110">
        <template #default="{ row }">
          <el-button v-if="row.submission.gradeStatus !== 1" size="small" type="primary"
            @click="openGrade(row)">批改</el-button>
          <span v-else class="text-gray">已完成</span>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!list.length" description="暂无提交记录" />

    <el-dialog v-model="contentDialog" title="学员作答" width="520px">
      <div style="white-space:pre-wrap;line-height:1.8">{{ cur?.submission.content }}</div>
      <div v-if="cur?.submission.attachment" class="mt12">附件：<el-link :href="cur.submission.attachment"
          target="_blank">点击查看</el-link></div>
    </el-dialog>

    <el-dialog v-model="gradeDialog" title="批改作业" width="480px">
      <el-descriptions :column="1" border v-if="cur">
        <el-descriptions-item label="学员">{{ cur.studentName }}</el-descriptions-item>
        <el-descriptions-item label="作业">{{ cur.assignmentTitle }}（满分 {{ cur.totalScore }}）</el-descriptions-item>
        <el-descriptions-item label="作答">{{ cur.submission.content }}</el-descriptions-item>
      </el-descriptions>
      <el-form class="mt12" label-width="80px">
        <el-form-item label="得分"><el-input-number v-model="grade.score" :min="0"
            :max="cur?.totalScore || 100" /></el-form-item>
        <el-form-item label="评语"><el-input v-model="grade.teacherComment" type="textarea" :rows="3"
            placeholder="给学员的反馈" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reject">打回重做</el-button>
        <el-button type="primary" @click="pass">提交评分</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { teacherApi } from '@/api'
import { minuteText } from '@/utils/format'

const list = ref([])
const gradeStatus = ref(0)
const contentDialog = ref(false)
const gradeDialog = ref(false)
const cur = ref(null)
const grade = reactive({ score: 80, teacherComment: '' })

async function load() { list.value = await teacherApi.submissions(gradeStatus.value) }
function viewContent(row) { cur.value = row; contentDialog.value = true }
function openGrade(row) {
  cur.value = row
  grade.score = row.totalScore || 100
  grade.teacherComment = row.submission.teacherComment || ''
  gradeDialog.value = true
}
async function doGrade(status) {
  await teacherApi.grade({
    submissionId: cur.value.submission.id,
    score: grade.score, teacherComment: grade.teacherComment, gradeStatus: status
  })
  ElMessage.success(status === 1 ? '已评分' : '已打回')
  gradeDialog.value = false; load()
}
const pass = () => doGrade(1)
const reject = () => doGrade(2)
onMounted(load)
</script>
