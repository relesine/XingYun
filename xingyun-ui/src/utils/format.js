/** 通用格式化工具：金额、时长、时间、状态文案等 */

// 分 → 元（后端金额用 DECIMAL，这里直接展示数字并补两位小数）
export const money = (v) => (v === null || v === undefined || v === '' ? '0.00' : Number(v).toFixed(2))

// 秒 → mm:ss
export function duration(sec) {
  sec = Number(sec || 0)
  const m = Math.floor(sec / 60)
  const s = sec % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

export const percent = (v) => `${Math.round(Number(v || 0))}%`

// 时间字符串截取到分钟（兼容 ISO / yyyy-MM-dd HH:mm:ss）
export const minuteText = (t) => (t ? String(t).replace('T', ' ').substring(0, 16) : '-')

// 课程上下架状态
export const shelfText = (s) => ({ 0: '已下架', 1: '已上架' }[s] ?? '未知')
export const shelfType = (s) => ({ 0: 'info', 1: 'success' }[s] ?? 'info')

// 课程审核状态：0草稿 1待审核 2已通过 3已驳回
export const auditText = (s) => ({ 0: '草稿', 1: '待审核', 2: '已通过', 3: '已驳回' }[s] ?? '未知')
export const auditType = (s) => ({ 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }[s] ?? 'info')

// 订单状态
export const orderText = (s) => ({ 0: '待支付', 1: '已支付', 2: '已取消', 3: '已退款' }[s] ?? '未知')
export const orderType = (s) => ({ 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }[s] ?? 'info')

// 用户状态：0正常 1禁用
export const userStatusText = (s) => (s === 0 ? '正常' : '禁用')

// 难度
export const levelText = (s) => ({ 1: '入门', 2: '中级', 3: '高级' }[s] ?? '入门')
