/**
 * 简单 HTML 清洗工具（XSS 防护）：
 * 课程介绍等富文本由讲师填写，若直接 v-html 渲染，可能被注入 <script> 或 onerror 等危险代码。
 * 该工具用正则剔除危险标签与危险属性，保留基础排版标签（p/br/strong/em/u/ul/ol/li/h1-h4/a/img）。
 * 说明：这是学习项目级的轻量防护；如需完整健壮性，可替换为 DOMPurify（生产建议）。
 */

const DANGEROUS_TAGS = /<(script|iframe|object|embed|link|meta|style|form|input|button|svg|math|video|audio)[^>]*>[\s\S]*?<\/\1>|<(script|iframe|object|embed|link|meta|style|form|input|button|svg|math|video|audio)[^>]*\/?>/gi

// 事件属性（onclick/onerror/onload...）与危险 URL 协议
const DANGEROUS_ATTR = /\s(on\w+|style|srcdoc|formaction)\s*=\s*("[^"]*"|'[^']*'|[^\s>]+)/gi
const DANGEROUS_URL = /(href|src)\s*=\s*("(javascript|data|vbscript):[^"]*"|'(javascript|data|vbscript):[^']*'|(javascript|data|vbscript):[^\s>]+)/gi

export function sanitizeHtml(html) {
  if (!html) return ''
  let s = String(html)
  // 1. 整段删除危险标签及其内容
  s = s.replace(DANGEROUS_TAGS, '')
  // 2. 删除事件属性 / style / srcdoc
  s = s.replace(DANGEROUS_ATTR, '')
  // 3. 删除 javascript: / data: 等危险链接
  s = s.replace(DANGEROUS_URL, '')
  // 4. 删除剩余的 < 后跟非字母的残留（如 < 与纯文本混淆）
  s = s.replace(/<(?![a-zA-Z/!])/g, '&lt;')
  return s
}
