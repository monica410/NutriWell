import { request } from './api.js'

export async function getGoals(userId, signal) {
  const params = new URLSearchParams({ userId: String(userId) })
  try {
    return await request(`/goals?${params}`, { signal })
  } catch (error) {
    if (error.status === 404 && error.message === 'Personal goals were not found.') return null
    throw error
  }
}

export function saveGoal(userId, target, value, hasGoals, isMax = false, signal) {
  return request('/goals', {
    method: hasGoals ? 'PATCH' : 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId, [target]: value, maxGoals: { [target]: isMax } }),
    signal,
  })
}

export function removeGoal(userId, target, signal) {
  const params = new URLSearchParams({ userId: String(userId) })
  return request(`/goals/${encodeURIComponent(target)}?${params}`, { method: 'DELETE', signal })
}

export function removeAllGoals(userId, signal) {
  const params = new URLSearchParams({ userId: String(userId) })
  return request(`/goals?${params}`, { method: 'DELETE', signal })
}

export function parseGoalValue(value) {
  const quantity = Number(value)
  if (!/^(?:\d+(?:\.\d{1,3})?|\.\d{1,3})$/.test(value)
    || !Number.isFinite(quantity) || quantity < 0 || quantity > 9999999.999) {
    throw new Error('Enter a postive number less than 9999999.999 up to three decimal places.')
  }
  return quantity
}
