<template>
  <div class="detail-page">
    <el-skeleton v-if="loading" :rows="8" animated class="detail-skeleton" />

    <template v-else-if="detail">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/courses' }">全部课程</el-breadcrumb-item>
        <el-breadcrumb-item>{{ detail.courseName }}</el-breadcrumb-item>
      </el-breadcrumb>

      <section class="course-head">
        <div class="head-cover">
          <img v-if="detail.cover && !coverError" :src="detail.cover" :alt="detail.courseName" @error="coverError = true" />
          <div v-else class="cover-fallback">{{ detail.courseName?.charAt(0) }}</div>
        </div>

        <div class="head-main">
          <div class="title-row">
            <h1>{{ detail.courseName }}</h1>
            <el-tag v-if="detail.isFree === 1" type="success" size="small">免费课程</el-tag>
          </div>
          <p class="intro">{{ detail.intro }}</p>

          <div class="meta-row">
            <span>讲师：{{ detail.teacherName }}</span>
            <span>课时：{{ detail.lessonCount }} 节</span>
            <span>{{ detail.studentCount }} 人在学</span>
            <span>评分：{{ detail.score }}</span>
            <span>难度：{{ levelText(detail.difficulty) }}</span>
          </div>

          <div class="action-row">
            <template v-if="detail.purchased">
              <div class="progress-block">
                <div class="progress-label">
                  <span>学习进度</span>
                  <span>{{ detail.courseProgress || 0 }}%</span>
                </div>
                <el-progress :percentage="detail.courseProgress || 0" :show-text="false" :stroke-width="6" />
              </div>
              <el-button type="primary" @click="startLearn(detail.chapters[0]?.lessons[0])">
                {{ detail.courseProgress > 0 ? '继续学习' : '开始学习' }}
              </el-button>
            </template>
            <template v-else>
              <div class="price-block">
                <span class="now">¥{{ money(detail.price) }}</span>
                <span v-if="detail.originalPrice" class="origin">¥{{ money(detail.originalPrice) }}</span>
              </div>
              <el-button type="primary" @click="openBuy">立即购买</el-button>
            </template>
            <el-button @click="toggleFav">
              <el-icon><Star /></el-icon>
              <span class="fav-text">收藏</span>
            </el-button>
            <span v-if="detail.purchased" class="purchased-tip">已购买 / 免费开放</span>
          </div>
        </div>
      </section>

      <section class="detail-body">
        <el-tabs v-model="tab">
          <el-tab-pane label="课程介绍" name="intro">
            <div class="desc" v-html="safeDescription || '讲师暂未填写课程介绍'"></div>
          </el-tab-pane>

          <el-tab-pane :label="`章节目录（${detail.lessonCount}）`" name="chapter">
            <el-collapse v-model="activeChapters" class="chapter-collapse">
              <el-collapse-item v-for="(ch, ci) in detail.chapters" :key="ci" :name="ci">
                <template #title>
                  <div class="chapter-head">
                    <span class="chapter-title">{{ ch.chapter }}</span>
                    <span class="chapter-count">{{ ch.lessons.length }} 节</span>
                  </div>
                </template>
                <div
                  v-for="ls in ch.lessons"
                  :key="ls.id"
                  class="lesson-row"
                  @click="playLesson(ls)"
                >
                  <el-icon class="lesson-icon">
                    <VideoPlay v-if="ls.canPlay" />
                    <Lock v-else />
                  </el-icon>
                  <span class="lesson-name">第 {{ ls.sort }} 节 {{ ls.lessonName }}</span>
                  <el-tag v-if="ls.isFreeTrial === 1" size="small" type="success">试看</el-tag>
                  <el-tag v-if="ls.progress >= 100" size="small" type="info">已完成</el-tag>
                  <el-tag v-else-if="ls.progress > 0" size="small" type="warning">学到 {{ ls.progress }}%</el-tag>
                  <span class="lesson-duration">{{ duration(ls.duration) }}</span>
                </div>
              </el-collapse-item>
            </el-collapse>
            <el-alert
              class="trial-tip"
              type="info"
              :closable="false"
              title="试看规则：免费课程全部开放；付费课程前 3 节可免费试看，购买后解锁全部课时。"
            />
          </el-tab-pane>

          <el-tab-pane label="课件资料" name="ware">
            <el-empty v-if="!wareList.length" description="暂无课件资料" :image-size="80" />
            <el-table v-else :data="wareList">
              <el-table-column prop="wareName" label="课件名称" />
              <el-table-column prop="fileType" label="类型" width="100" />
              <el-table-column label="操作" width="100">
                <template #default="{ row }">
                  <el-link type="primary" :href="'/upload/' + row.filePath" target="_blank">下载</el-link>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="课程评论" name="comment">
            <div class="comment-form">
              <div class="form-row">
                <span class="form-label">课程评分</span>
                <el-rate v-model="commentForm.score" show-text :texts="['很差', '较差', '还行', '推荐', '力荐']" />
              </div>
              <el-input
                v-model="commentForm.content"
                type="textarea"
                :rows="3"
                maxlength="500"
                show-word-limit
                placeholder="说说这门课对你的帮助，给其他同学一个参考"
              />
              <div class="form-actions">
                <el-button type="primary" @click="addComment">发表评论</el-button>
              </div>
            </div>

            <div v-if="commentList.length" class="comment-list">
              <div v-for="c in commentList" :key="c.id" class="comment-item">
                <el-avatar :size="32" :src="c.avatar">{{ c.nickname?.charAt(0) }}</el-avatar>
                <div class="comment-body">
                  <div class="comment-head">
                    <span class="comment-user">{{ c.nickname }}</span>
                    <el-rate :model-value="c.score" disabled size="small" />
                  </div>
                  <div class="comment-content">{{ c.content }}</div>
                  <div class="comment-time">{{ minuteText(c.createTime) }}</div>
                </div>
              </div>
            </div>
            <el-empty v-else description="还没有评论，欢迎学完后留下你的评价" :image-size="80" />
          </el-tab-pane>

          <el-tab-pane label="课程问答" name="qa">
            <div class="qa-form">
              <el-input v-model="question" placeholder="向讲师提问，请描述清楚遇到的问题" @keyup.enter="ask">
                <template #append><el-button @click="ask">提问</el-button></template>
              </el-input>
            </div>
            <el-timeline v-if="qaList.length" class="qa-list">
              <el-timeline-item v-for="q in qaList" :key="q.id" :timestamp="minuteText(q.createTime)">
                <div class="qa-item">
                  <p><strong>{{ q.askerName }}</strong>：{{ q.question }}</p>
                  <div v-if="q.replyContent" class="qa-reply">
                    <span class="reply-label">讲师回复</span>
                    <p>{{ q.replyContent }}</p>
                    <span class="text-gray">{{ minuteText(q.replyTime) }}</span>
                  </div>
                </div>
              </el-timeline-item>
            </el-timeline>
            <el-empty v-else description="暂无提问，学习中有疑问可以在这里提问" :image-size="80" />
          </el-tab-pane>
        </el-tabs>
      </section>
    </template>

    <el-dialog v-model="buyVisible" title="确认订单" width="420px">
      <div class="order-course">
        <span class="order-label">课程</span>
        <span class="order-value ellipsis">{{ detail?.courseName }}</span>
      </div>
      <div class="order-course">
        <span class="order-label">课程价格</span>
        <span class="order-value price">¥{{ money(detail?.price) }}</span>
      </div>
      <div class="order-course coupon-row">
        <span class="order-label">优惠券</span>
        <el-select v-model="couponId" placeholder="不使用优惠券" clearable class="coupon-select">
          <el-option v-for="cp in usableCoupons" :key="cp.id" :label="couponLabel(cp)" :value="cp.id" />
        </el-select>
      </div>
      <template #footer>
        <el-button @click="buyVisible = false">取消</el-button>
        <el-button type="primary" :loading="paying" @click="confirmPay">提交订单并支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { money, duration, minuteText, levelText } from '@/utils/format'
import { sanitizeHtml } from '@/utils/sanitize'
import { courseApi, orderApi, couponApi, commentApi, qaApi, wareApi, favoriteApi } from '@/api'

const route = useRoute()
const router = useRouter()
const courseId = Number(route.params.id)
const detail = ref(null)
const loading = ref(true)
const coverError = ref(false)
const tab = ref('chapter')
const activeChapters = ref([0])
const wareList = ref([])
const commentList = ref([])
const qaList = ref([])
const commentForm = reactive({ score: 5, content: '' })
const question = ref('')
const safeDescription = computed(() => sanitizeHtml(detail.value?.description))

const buyVisible = ref(false)
const paying = ref(false)
const couponId = ref(null)
const usableCoupons = ref([])

function couponLabel(c) {
  if (c.couponType === 1) return `${c.couponName}：满${money(c.threshold)}减${money(c.discountValue)}元`
  if (c.couponType === 2) return `${c.couponName}：立减${money(c.discountValue)}元`
  return `${c.couponName}：${Number(c.discountValue)}折`
}

async function load() {
  loading.value = true
  try {
    detail.value = await courseApi.detail(courseId)
    document.title = `${detail.value.courseName} · 星云课堂`
    const [wares, comments, qas] = await Promise.all([
      wareApi.list(courseId),
      commentApi.list(courseId),
      qaApi.list(courseId)
    ])
    wareList.value = wares || []
    commentList.value = comments || []
    qaList.value = qas || []
  } finally {
    loading.value = false
  }
}
function playLesson(ls) {
  if (!ls) return
  if (ls.canPlay) router.push(`/learn/${courseId}?lessonId=${ls.id}`)
  else ElMessage.warning(ls.lockReason || '该课时需购买后观看')
}
function startLearn(ls) { playLesson(ls) }

async function openBuy() {
  couponId.value = null
  usableCoupons.value = await couponApi.available()
  buyVisible.value = true
}
async function confirmPay() {
  paying.value = true
  try {
    const order = await orderApi.create(courseId, couponId.value)
    await orderApi.pay(order.orderNo)
    ElMessage.success('支付成功，开始学习吧')
    buyVisible.value = false
    load()
  } catch (e) { /* 请求层已统一提示 */ } finally { paying.value = false }
}
async function toggleFav() {
  await favoriteApi.toggle(courseId)
  ElMessage.success('已更新收藏状态')
}
async function addComment() {
  if (!commentForm.content) return ElMessage.warning('请填写评论内容')
  await commentApi.add({ courseId, ...commentForm })
  ElMessage.success('评论发布成功')
  commentForm.content = ''
  commentList.value = await commentApi.list(courseId)
}
async function ask() {
  if (!question.value) return
  await qaApi.ask({ courseId, question: question.value })
  ElMessage.success('提问已提交，等待讲师回复')
  question.value = ''
  qaList.value = await qaApi.list(courseId)
}
onMounted(load)
</script>

<style scoped>
.detail-page { display: flex; flex-direction: column; gap: 16px; }
.detail-skeleton {
  background: var(--xy-surface); border: 1px solid var(--xy-border);
  border-radius: var(--xy-radius); padding: 24px;
}
.breadcrumb { font-size: 12px; }

.course-head {
  display: grid;
  grid-template-columns: 340px minmax(0, 1fr);
  gap: 24px;
  padding: 20px;
  background: var(--xy-surface);
  border: 1px solid var(--xy-border);
  border-radius: var(--xy-radius);
}
.head-cover { aspect-ratio: 16 / 9; background: #f2f3f5; border-radius: var(--xy-radius-sm); overflow: hidden; }
.head-cover img { width: 100%; height: 100%; object-fit: cover; }
.cover-fallback {
  width: 100%; height: 100%; display: flex; align-items: center; justify-content: center;
  font-size: 40px; font-weight: 600; color: #b3e7d3;
}
.head-main { display: flex; flex-direction: column; min-width: 0; }
.title-row { display: flex; align-items: center; gap: 8px; }
.title-row h1 { font-size: 22px; line-height: 1.4; font-weight: 600; color: var(--xy-text); }
.intro { margin-top: 8px; font-size: 13px; line-height: 1.7; color: var(--xy-text-2); }
.meta-row {
  margin-top: 12px; display: flex; flex-wrap: wrap; gap: 6px 18px;
  font-size: 12px; color: var(--xy-text-3);
}
.action-row {
  margin-top: auto; padding-top: 16px;
  display: flex; align-items: center; flex-wrap: wrap; gap: 12px;
}
.price-block { display: flex; align-items: baseline; gap: 8px; }
.price-block .now { font-size: 26px; font-weight: 600; color: var(--xy-price); }
.price-block .origin { font-size: 13px; color: var(--xy-text-3); text-decoration: line-through; }
.progress-block { width: 200px; }
.progress-label { display: flex; justify-content: space-between; margin-bottom: 4px; font-size: 12px; color: var(--xy-text-3); }
.fav-text { margin-left: 4px; }
.purchased-tip { font-size: 12px; color: var(--xy-primary); }

.detail-body {
  background: var(--xy-surface);
  border: 1px solid var(--xy-border);
  border-radius: var(--xy-radius);
  padding: 4px 20px 20px;
}
.desc { line-height: 1.9; color: var(--xy-text-2); font-size: 14px; }
.desc :deep(img) { max-width: 100%; border-radius: 4px; }
.desc :deep(h2), .desc :deep(h3) { margin: 16px 0 8px; color: var(--xy-text); font-weight: 600; }
.desc :deep(p) { margin: 8px 0; }

.chapter-head { display: flex; align-items: center; gap: 10px; }
.chapter-title { font-size: 14px; font-weight: 500; color: var(--xy-text); }
.chapter-count { font-size: 12px; color: var(--xy-text-3); }
.lesson-row {
  display: flex; align-items: center; gap: 10px;
  height: 42px; padding: 0 12px;
  border-top: 1px solid var(--xy-border-light);
  cursor: pointer;
  transition: background-color 0.15s;
}
.lesson-row:hover { background: #fafbfc; }
.lesson-icon { color: var(--xy-text-3); font-size: 14px; }
.lesson-name { flex: 1; font-size: 13px; color: var(--xy-text-2); }
.lesson-row:hover .lesson-name { color: var(--xy-text); }
.lesson-duration { width: 56px; text-align: right; font-size: 12px; color: var(--xy-text-3); }
.trial-tip { margin-top: 14px; }

.comment-form { padding: 4px 0 16px; border-bottom: 1px solid var(--xy-border-light); }
.form-row { display: flex; align-items: center; gap: 12px; margin-bottom: 10px; }
.form-label { font-size: 13px; color: var(--xy-text-2); width: 60px; }
.form-actions { margin-top: 10px; text-align: right; }
.comment-list { padding-top: 16px; }
.comment-item { display: flex; gap: 12px; padding: 12px 0; border-bottom: 1px solid var(--xy-border-light); }
.comment-item:last-child { border-bottom: none; }
.comment-body { flex: 1; min-width: 0; }
.comment-head { display: flex; align-items: center; justify-content: space-between; }
.comment-user { font-size: 13px; font-weight: 500; color: var(--xy-text); }
.comment-content { margin-top: 4px; font-size: 13px; color: var(--xy-text-2); line-height: 1.7; }
.comment-time { margin-top: 4px; font-size: 12px; color: var(--xy-text-3); }

.qa-form { max-width: 720px; margin-bottom: 16px; }
.qa-item p { font-size: 13px; color: var(--xy-text-2); line-height: 1.7; }
.qa-reply { margin-top: 8px; padding: 10px 12px; background: #fafbfc; border: 1px solid var(--xy-border-light); border-radius: var(--xy-radius-sm); }
.reply-label { font-size: 12px; color: var(--xy-primary); }
.qa-reply p { margin-top: 2px; color: var(--xy-text); }

.order-course { display: flex; align-items: center; padding: 10px 0; border-bottom: 1px solid var(--xy-border-light); }
.order-label { width: 80px; font-size: 13px; color: var(--xy-text-3); }
.order-value { flex: 1; font-size: 13px; color: var(--xy-text); }
.coupon-row { border-bottom: none; }
.coupon-select { flex: 1; }

@media (max-width: 900px) {
  .course-head { grid-template-columns: 1fr; gap: 16px; }
  .head-cover { max-width: 480px; }
}
@media (max-width: 640px) {
  .title-row h1 { font-size: 19px; }
  .detail-body { padding: 4px 14px 16px; }
  .action-row { gap: 10px; }
  .progress-block { width: 100%; }
}
</style>
