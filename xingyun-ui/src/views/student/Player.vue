<template>
  <div v-if="detail" class="player-page">
    <el-page-header @back="$router.back()" class="mb12" :content="detail.courseName" />
    <el-row :gutter="16">
      <el-col :md="17" :sm="24">
        <div class="page-card">
          <div class="video-wrap">
            <video ref="videoEl" :src="videoSrc" controls controlslist="nodownload" disablepictureinpicture
              @loadedmetadata="onLoaded" @timeupdate="onTimeUpdate" @pause="report(true)" @ended="onEnded" />
          </div>
          <h3 class="mt12">第{{ current?.sort }}节 {{ current?.lessonName }}
            <el-tag size="small" v-if="current?.isFreeTrial === 1" type="success">试看</el-tag>
          </h3>
          <div class="flex-between text-gray">
            <span>本节进度：{{ lessonProgress }}%　|　课程进度：{{ detail.courseProgress || 0 }}%</span>
            <span>
              <el-button size="small" :disabled="!prevLesson" @click="switchLesson(prevLesson)">上一节</el-button>
              <el-button size="small" type="primary" :disabled="!nextLesson"
                @click="switchLesson(nextLesson)">下一节</el-button>
            </span>
          </div>
        </div>

        <div class="page-card mt12">
          <el-tabs v-model="tab">
            <el-tab-pane label="随堂测验" name="quiz">
              <el-empty v-if="!questions.length" description="本课时没有随堂测验" />
              <template v-else>
                <div v-for="(q, i) in questions" :key="q.id" class="quiz-q">
                  <div class="q-title">{{ i + 1 }}.【{{ typeName(q.questionType) }}】{{ q.content }}（{{ q.score }}分）</div>
                  <el-radio-group v-if="q.questionType !== 2" v-model="answers[q.id]">
                    <el-radio v-for="op in options(q)" :key="op.k" :label="op.k" class="block">{{ op.k }}. {{ op.v
                      }}</el-radio>
                  </el-radio-group>
                  <el-checkbox-group v-else v-model="multiAnswers[q.id]">
                    <el-checkbox v-for="op in options(q)" :key="op.k" :label="op.k" class="block">{{ op.k }}. {{ op.v
                      }}</el-checkbox>
                  </el-checkbox-group>
                  <div v-if="result" class="analyze" :class="itemResult(q.id) ? 'right' : 'wrong'">
                    {{ itemResult(q.id) ? '回答正确' : '回答错误' }} · 正确答案：{{ answerLabel(q) }} · 解析：{{ q.analysis || '无' }}
                  </div>
                </div>
                <div class="mt12">
                  <el-button type="primary" @click="submitQuiz" :disabled="!!result">交卷判分</el-button>
                  <el-button v-if="result" @click="resetQuiz">重做</el-button>
                  <div v-if="result" class="quiz-result" :class="result.passed ? 'passed' : 'failed'">
                    <span class="quiz-score">{{ result.userScore }} / {{ result.totalScore }} 分</span>
                    <span>答对 {{ result.rightCount }}/{{ result.questionCount }} 题，{{ result.passed ? '测验通过' :
                      '未达及格线，可查看解析后重做' }}</span>
                  </div>
                </div>
              </template>
            </el-tab-pane>

            <el-tab-pane label="课程作业" name="assignment">
              <el-empty v-if="!assignments.length" description="暂无作业" />
              <div v-for="a in assignments" :key="a.assignment.id" class="asg">
                <div class="flex-between">
                  <strong>{{ a.assignment.title }}（满分{{ a.assignment.totalScore }}分）</strong>
                  <el-tag v-if="a.mySubmission?.gradeStatus === 1" type="success">已批 {{ a.mySubmission.score
                    }}分</el-tag>
                  <el-tag v-else-if="a.mySubmission?.gradeStatus === 2" type="danger">已打回</el-tag>
                  <el-tag v-else-if="a.mySubmission" type="warning">待批改</el-tag>
                  <el-tag v-else type="info">未提交</el-tag>
                </div>
                <p class="text-gray mt12">{{ a.assignment.content || '（暂无作业要求说明）' }}</p>
                <el-input v-model="asgContents[a.assignment.id]" type="textarea" :rows="3" placeholder="在此填写作业内容"
                  class="mt12" />
                <el-button size="small" type="primary" class="mt12" @click="submitAsg(a)">提交作业</el-button>
                <div v-if="a.mySubmission?.teacherComment" class="analyze right mt12">讲师评语：{{
                  a.mySubmission.teacherComment }}</div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="课件资料" name="ware">
              <el-empty v-if="!wareList.length" description="暂无课件" />
              <el-table v-else :data="wareList" border>
                <el-table-column prop="wareName" label="课件" />
                <el-table-column prop="fileType" label="类型" width="90" />
                <el-table-column label="下载" width="100">
                  <template #default="{ row }"><el-link type="primary" :href="'/upload/' + row.filePath"
                      target="_blank">下载</el-link></template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-col>

      <el-col :md="7" :sm="24">
        <div class="page-card lesson-panel">
          <div class="panel-title">课程目录</div>
          <el-collapse v-model="openChapters">
            <el-collapse-item v-for="(ch, ci) in detail.chapters" :key="ci" :name="ci" :title="ch.chapter">
              <div v-for="ls in ch.lessons" :key="ls.id" class="ls-row" :class="{ active: ls.id === current?.id }"
                @click="switchLesson(ls)">
                <el-icon>
                  <VideoPlay v-if="ls.canPlay" />
                  <Lock v-else />
                </el-icon>
                <span class="ls-name">第{{ ls.sort }}节 {{ ls.lessonName }}</span>
                <el-icon v-if="ls.progress >= 100" color="#67c23a">
                  <CircleCheckFilled />
                </el-icon>
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { courseApi, videoApi, learnApi, quizApi, assignmentApi, wareApi } from '@/api'
import { duration } from '@/utils/format'

const route = useRoute()
const courseId = Number(route.params.id)
const videoEl = ref(null)
const detail = ref(null)
const allLessons = ref([])
const current = ref(null)
const videoSrc = ref('')
const lessonProgress = ref(0)
let lastReportAt = 0
const tab = ref('quiz')
const openChapters = ref([0])

const questions = ref([])
const answers = reactive({})   // 单选/判断：qid -> 字母
const multiAnswers = reactive({}) // 多选：qid -> [字母]
const result = ref(null)

const assignments = ref([])
const asgContents = reactive({})
const wareList = ref([])

const typeName = (t) => ({ 1: '单选题', 2: '多选题', 3: '判断题' }[t])
function options(q) {
  // 把 optionA~D 组装成 {k:'A',v:'...'} 列表，空选项跳过
  const raw = { A: q.optionA, B: q.optionB, C: q.optionC, D: q.optionD }
  return Object.entries(raw).filter(([, v]) => v).map(([k, v]) => ({ k, v }))
}
const itemResult = (qid) => result.value?.details.find((d) => d.questionId === qid)?.right

const prevLesson = ref(null)
const nextLesson = ref(null)
function refreshPrevNext() {
  const idx = allLessons.value.findIndex((l) => l.id === current.value.id)
  prevLesson.value = idx > 0 ? allLessons.value[idx - 1] : null
  nextLesson.value = idx >= 0 && idx < allLessons.value.length - 1 ? allLessons.value[idx + 1] : null
}

async function switchLesson(ls) {
  if (!ls) return
  if (!ls.canPlay) return ElMessage.warning(ls.lockReason || '请先购买课程')
  current.value = ls
  lessonProgress.value = ls.progress || 0
  refreshPrevNext()
  // 向后端申请防盗链签名
  const s = await videoApi.sign(ls.id)
  videoSrc.value = videoApi.streamUrl(s.sign)
  result.value = null
  Object.keys(answers).forEach((k) => delete answers[k])
  Object.keys(multiAnswers).forEach((k) => delete multiAnswers[k])
  const qd = await quizApi.byLesson(ls.id)
  questions.value = qd.questions || []
  qd.questions.forEach((q) => { multiAnswers[q.id] = [] })
}

function onLoaded() {
  // 断点续播：跳到上次位置
  if (current.value?.positionSeconds && videoEl.value) {
    const t = current.value.positionSeconds
    if (t > 2 && t < (current.value.duration || 0)) {
      videoEl.value.currentTime = t
      ElMessage.info(`已为你续播到 ${duration(t)}`)
    }
  }
}

async function report(force = false) {
  const v = videoEl.value
  if (!v || !current.value) return
  const now = Date.now()
  if (!force && now - lastReportAt < 5000) return // 每 5 秒最多上报一次
  lastReportAt = now
  const pos = Math.floor(v.currentTime)
  const total = Math.floor(v.duration) || current.value.duration || 0
  const r = await learnApi.report({
    courseId, lessonId: current.value.id, positionSec: pos, totalSec: total, device: 'web'
  })
  if (r) {
    lessonProgress.value = r.lessonProgress ?? lessonProgress.value
    detail.value.courseProgress = r.courseProgress ?? detail.value.courseProgress
    if (r.newCertificate) ElMessage.success('恭喜完成全部课时，已获得结业证书！')
  }
}

function onTimeUpdate() { report(false) }
async function onEnded() {
  await report(true)
  current.value && ElMessage.success('本节学习完成')
  if (nextLesson.value) switchLesson(nextLesson.value)
}

async function submitQuiz() {
  if (!questions.value.length) return ElMessage.warning('本课时没有测验题目')
  const arr = questions.value.map((q) => {
    let ua = ''
    if (q.questionType === 2) {
      // 多选：选项字母排序后拼接，避免 AB/BA 判分不一致
      ua = (multiAnswers[q.id] || []).slice().sort().join('')
    } else if (q.questionType === 3) {
      // 判断题：前端选项为 A.正确 B.错误，提交统一映射为后端约定的 TRUE/FALSE
      ua = answers[q.id] === 'A' ? 'TRUE' : answers[q.id] === 'B' ? 'FALSE' : (answers[q.id] || '')
    } else {
      ua = answers[q.id] || ''
    }
    return { questionId: q.id, userAnswer: ua }
  })
  result.value = await quizApi.submit({ quizId: questions.value[0].quizId, answers: arr })
}
// 交卷后展示正确答案：优先取后端返回的 rightAnswer（答案不下发策略，未交卷前无正确答案可看）
const answerLabel = (q) => {
  const d = result.value?.details.find((x) => x.questionId === q.id)
  const ans = d ? d.rightAnswer : ''
  return q.questionType === 3 ? (String(ans).toUpperCase() === 'TRUE' ? '正确' : '错误') : (ans || '-')
}
function resetQuiz() {
  result.value = null
  Object.keys(answers).forEach((k) => delete answers[k])
  Object.keys(multiAnswers).forEach((k) => { multiAnswers[k] = [] })
}
async function submitAsg(a) {
  const content = asgContents[a.assignment.id]
  if (!content) return ElMessage.warning('请先填写作业内容')
  await assignmentApi.submit({ assignmentId: a.assignment.id, content })
  ElMessage.success('作业已提交，等待讲师批改')
  await loadAsg()
}
async function loadAsg() {
  assignments.value = await assignmentApi.list(courseId)
  assignments.value.forEach((a) => { if (a.mySubmission) asgContents[a.assignment.id] = a.mySubmission.content })
}

onMounted(async () => {
  detail.value = await courseApi.detail(courseId)
  allLessons.value = detail.value.chapters.flatMap((c) => c.lessons)
  wareList.value = await wareApi.list(courseId)
  await loadAsg()
  const wantId = Number(route.query.lessonId)
  const first = allLessons.value.find((l) => l.id === wantId) || allLessons.value.find((l) => l.canPlay)
  if (first) await switchLesson(first)
})
onBeforeUnmount(() => report(true))
</script>

<style scoped>
.video-wrap { background: #000; border-radius: var(--xy-radius-sm); overflow: hidden; }
.video-wrap video { width: 100%; max-height: 460px; display: block; }
.quiz-q { padding: 14px 0; border-bottom: 1px solid var(--xy-border-light); }
.q-title { font-weight: 500; color: var(--xy-text); margin-bottom: 8px; line-height: 1.6; }
.block { display: block; margin: 6px 0; }
.analyze { border-radius: var(--xy-radius-sm); padding: 8px 10px; margin-top: 8px; font-size: 13px; line-height: 1.7; }
.analyze.right { background: #f2faf5; color: #2f7d4a; }
.analyze.wrong { background: #fdf5f3; color: #b8422a; }
.quiz-result {
  margin-top: 12px; padding: 10px 12px;
  display: flex; align-items: baseline; gap: 12px;
  border-radius: var(--xy-radius-sm); font-size: 13px;
}
.quiz-result.passed { background: #f2faf5; color: #2f7d4a; }
.quiz-result.failed { background: #fdf5f3; color: #b8422a; }
.quiz-score { font-size: 16px; font-weight: 600; }
.asg { border: 1px solid var(--xy-border); border-radius: var(--xy-radius-sm); padding: 12px; margin-bottom: 12px; }
.lesson-panel { max-height: 760px; overflow: auto; }
.panel-title { font-weight: 600; color: var(--xy-text); margin-bottom: 8px; }
.ls-row {
  display: flex; align-items: center; gap: 8px;
  padding: 8px; border-radius: var(--xy-radius-sm);
  cursor: pointer;
}
.ls-row:hover { background: #fafbfc; }
.ls-row.active { background: var(--xy-primary-light); color: var(--xy-primary); }
.ls-name { flex: 1; font-size: 13px; }
</style>
