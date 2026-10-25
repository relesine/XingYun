<template>
  <article class="course-card" @click="goDetail">
    <div class="cover">
      <img
        v-if="course.cover && !imgError"
        :src="course.cover"
        :alt="course.courseName"
        loading="lazy"
        @error="imgError = true"
      />
      <div v-else class="cover-fallback">{{ course.courseName?.charAt(0) || '课' }}</div>
      <span v-if="isFree" class="badge free">免费</span>
      <span v-else-if="course.isRecommend === 1" class="badge recommend">推荐</span>
    </div>
    <div class="info">
      <h3 class="name ellipsis-2">{{ course.courseName }}</h3>
      <div class="meta">
        <span>{{ course.teacherName || '星云讲师' }}</span>
        <span class="dot">·</span>
        <span>{{ levelText(course.difficulty) }}</span>
      </div>
      <div class="bottom">
        <span v-if="Number(course.price) > 0" class="price">
          <em>¥</em>{{ money(course.price) }}
        </span>
        <span v-else class="price-free">免费学习</span>
        <span class="students">{{ course.studentCount || 0 }} 人在学</span>
      </div>
    </div>
  </article>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { money, levelText } from '@/utils/format'

const props = defineProps({ course: { type: Object, required: true } })
const router = useRouter()
const imgError = ref(false)
const isFree = computed(() => props.course.isFree === 1 || Number(props.course.price) === 0)

watch(() => props.course.cover, () => { imgError.value = false })

function goDetail() {
  router.push(`/course/${props.course.id}`)
}
</script>

<style scoped>
.course-card {
  background: var(--xy-surface);
  border: 1px solid var(--xy-border);
  border-radius: var(--xy-radius);
  overflow: hidden;
  cursor: pointer;
  transition: border-color 0.15s ease;
}
.course-card:hover { border-color: #b3e7d3; }
.course-card:hover .name { color: var(--xy-primary); }

.cover { position: relative; aspect-ratio: 16 / 9; background: #f2f3f5; overflow: hidden; }
.cover img { width: 100%; height: 100%; object-fit: cover; }
.cover-fallback {
  width: 100%; height: 100%;
  display: flex; align-items: center; justify-content: center;
  font-size: 34px; font-weight: 600; color: #b3e7d3;
  background: #f2f3f5;
}
.badge {
  position: absolute; top: 8px; left: 8px;
  height: 20px; padding: 0 6px;
  font-size: 11px; line-height: 20px; border-radius: 3px;
  color: #fff;
}
.badge.free { background: rgba(0, 168, 112, 0.92); }
.badge.recommend { background: rgba(212, 56, 13, 0.88); }

.info { padding: 10px 12px 12px; }
.name {
  font-size: 14px; font-weight: 500; line-height: 22px;
  height: 44px; color: var(--xy-text);
  transition: color 0.15s ease;
}
.meta {
  margin-top: 4px;
  font-size: 12px; color: var(--xy-text-3);
  display: flex; align-items: center; gap: 4px;
  white-space: nowrap; overflow: hidden;
}
.dot { color: #d5d8dd; }
.bottom {
  margin-top: 8px;
  display: flex; align-items: baseline; justify-content: space-between;
}
.price { font-size: 15px; color: var(--xy-price); font-weight: 600; }
.price em { font-style: normal; font-size: 12px; margin-right: 1px; }
.price-free { font-size: 13px; color: var(--xy-primary); }
.students { font-size: 12px; color: var(--xy-text-3); }
</style>
