/**
 * 后端接口集中封装：页面里 import { authApi, courseApi, ... } 直接调用即可。
 * 方法返回 Promise，成功结果已是 Result.data（request.js 已解包）。
 */
import req from './request'

// 认证与个人中心
export const authApi = {
  login: (data) => req.post('/auth/login', data),
  register: (data) => req.post('/auth/register', data),
  info: () => req.get('/auth/info'),
  updateProfile: (data) => req.put('/auth/profile', data),
  changePwd: (data) => req.put('/auth/password', data),
  logout: () => req.post('/auth/logout')
}

export const categoryApi = {
  tree: () => req.get('/category/tree')
}

export const courseApi = {
  page: (data) => req.post('/course/page', data),
  detail: (id) => req.get(`/course/detail/${id}`),
  hot: (limit = 8) => req.get(`/course/hot?limit=${limit}`),
  my: () => req.get('/course/my')
}

// 学习相关：进度、档案
export const learnApi = {
  report: (data) => req.post('/learn/report', data),
  courseProgress: (cid) => req.get(`/learn/courseProgress/${cid}`),
  continueLesson: (cid) => req.get(`/learn/continue/${cid}`),
  profile: () => req.get('/learn/profile')
}

export const videoApi = {
  sign: (lessonId) => req.post('/video/sign', { lessonId }),
  streamUrl: (sign) => `/api/video/stream?sign=${sign}` // 直接给 <video> 用
}

export const quizApi = {
  byLesson: (lessonId) => req.get(`/quiz/byLesson/${lessonId}`),
  submit: (data) => req.post('/quiz/submit', data),
  wrongBook: () => req.get('/quiz/wrongBook')
}

export const assignmentApi = {
  list: (courseId) => req.get(`/assignment/list/${courseId}`),
  submit: (data) => req.post('/assignment/submit', data)
}

export const orderApi = {
  create: (courseId, couponId) => req.post('/order/create', { courseId, couponId }),
  pay: (orderNo, payType = 1) => req.post('/order/pay', { orderNo, payType }),
  my: () => req.get('/order/my'),
  cancel: (id) => req.post(`/order/cancel/${id}`)
}

export const couponApi = {
  available: () => req.get('/coupon/available'),
  receive: (id) => req.post(`/coupon/receive/${id}`),
  my: (status) => req.get(`/coupon/my${status !== undefined ? `?status=${status}` : ''}`)
}

export const commentApi = {
  list: (courseId) => req.get(`/comment/list/${courseId}`),
  add: (data) => req.post('/comment/add', data)
}

export const qaApi = {
  list: (courseId) => req.get(`/qa/list/${courseId}`),
  ask: (data) => req.post('/qa/ask', data)
}

export const favoriteApi = {
  toggle: (courseId) => req.post(`/favorite/toggle/${courseId}`),
  my: () => req.get('/favorite/my')
}

export const certApi = {
  my: () => req.get('/cert/my'),
  detail: (id) => req.get(`/cert/detail/${id}`)
}

export const wareApi = { list: (courseId) => req.get(`/ware/list/${courseId}`) }

export const announceApi = { list: (type) => req.get(`/announcement/list${type !== undefined && type !== null ? `?type=${type}` : ''}`) }

// 讲师（公开 + 讲师端）
export const teacherApi = {
  list: (keyword) => req.get(`/teacher/list${keyword ? `?keyword=${encodeURIComponent(keyword)}` : ''}`),
  detail: (id) => req.get(`/teacher/detail/${id}`),
  stat: () => req.get('/teacher/stat'),
  coursePage: (data) => req.post('/teacher/course/page', data),
  saveCourse: (data) => req.post('/teacher/course/save', data),
  deleteCourse: (id) => req.delete(`/teacher/course/${id}`),
  shelf: (id, status) => req.post('/teacher/course/shelf', { id, status }),
  saveLesson: (data) => req.post('/teacher/lesson/save', data),
  deleteLesson: (id) => req.delete(`/teacher/lesson/${id}`),
  videoLibrary: () => req.get('/teacher/video/library'),
  courseStudents: (courseId) => req.get(`/teacher/course/students/${courseId}`),
  submissions: (gradeStatus) => req.get(`/teacher/assignment/submissions${gradeStatus !== undefined && gradeStatus !== null && gradeStatus !== '' ? `?gradeStatus=${gradeStatus}` : ''}`),
  grade: (data) => req.post('/teacher/assignment/grade', data),
  pendingQa: () => req.get('/teacher/qa/pending'),
  reply: (qaId, replyContent) => req.post('/teacher/qa/reply', { qaId, replyContent })
}

export const adminApi = {
  dashboard: () => req.get('/admin/dashboard'),
  userPage: (data) => req.post('/admin/user/page', data),
  userStatus: (id, status) => req.post('/admin/user/status', { id, status }),
  resetPwd: (id) => req.post(`/admin/user/resetPwd/${id}`),
  coursePage: (data) => req.post('/admin/course/page', data),
  audit: (data) => req.post('/admin/course/audit', data),
  shelf: (id, status) => req.post('/admin/course/shelf', { id, status }),
  recommend: (id, status) => req.post('/admin/course/recommend', { id, status }),
  deleteCourse: (id) => req.delete(`/admin/course/${id}`),
  orderPage: (data) => req.post('/admin/order/page', data),
  commentPage: (data) => req.post('/admin/comment/page', data),
  commentStatus: (id, status) => req.post('/admin/comment/status', { id, status }),
  categoryList: () => req.get('/admin/category/list'),
  categorySave: (data) => req.post('/admin/category/save', data),
  categoryDelete: (id) => req.delete(`/admin/category/${id}`),
  couponList: () => req.get('/admin/coupon/list'),
  couponSave: (data) => req.post('/admin/coupon/save', data),
  couponDelete: (id) => req.delete(`/admin/coupon/${id}`),
  announceList: () => req.get('/admin/announcement/list'),
  announceSave: (data) => req.post('/admin/announcement/save', data),
  announceDelete: (id) => req.delete(`/admin/announcement/${id}`)
}

// 文件上传（注意要 FormData，让浏览器自动带 multipart 边界）
export const uploadUrl = '/api/file/upload'
