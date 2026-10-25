<template>
  <div class="page-card">
    <div class="page-head">
      <h1 class="page-title">学员答疑</h1>
      <span class="page-sub">回复学员在课程中提出的问题</span>
    </div>
    <el-timeline>
      <el-timeline-item v-for="q in list" :key="q.id" :timestamp="minuteText(q.createTime)" placement="top">
        <el-card>
          <div class="flex-between">
            <div>
              <el-tag size="small" type="info">{{ q.courseName }}</el-tag>
              <strong style="margin-left:8px">{{ q.askerName }}</strong> 提问：{{ q.question }}
            </div>
            <el-button size="small" type="primary" @click="openReply(q)">回复</el-button>
          </div>
        </el-card>
      </el-timeline-item>
    </el-timeline>
    <el-empty v-if="!list.length" description="太棒了，没有待回复问题" />

    <el-dialog v-model="dialog" title="回复学员" width="520px">
      <div class="mb12">问题：{{ cur?.question }}</div>
      <el-input v-model="replyContent" type="textarea" :rows="4" placeholder="请输入专业、耐心的解答" />
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="submit">发送回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { teacherApi } from '@/api'
import { minuteText } from '@/utils/format'

const list = ref([])
const dialog = ref(false)
const cur = ref(null)
const replyContent = ref('')
async function load() { list.value = await teacherApi.pendingQa() }
function openReply(q) { cur.value = q; replyContent.value = ''; dialog.value = true }
async function submit() {
  if (!replyContent.value) return ElMessage.warning('请输入回复内容')
  await teacherApi.reply(cur.value.id, replyContent.value)
  ElMessage.success('回复成功')
  dialog.value = false; load()
}
onMounted(load)
</script>
