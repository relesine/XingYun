/**
 * 安全读取 localStorage 中的 JSON 数据：
 * 若数据损坏（非 JSON 或结构异常），返回默认值并清理脏数据，避免页面白屏。
 */
export function safeJsonParse(key, fallback = null) {
  try {
    const raw = localStorage.getItem(key)
    if (!raw) return fallback
    const parsed = JSON.parse(raw)
    return parsed === undefined ? fallback : parsed
  } catch (e) {
    // 脏数据：删除并回退默认值，保证应用可用
    try { localStorage.removeItem(key) } catch (_) { /* ignore */ }
    return fallback
  }
}
