import { request } from './api.js'

export function getFrequentFoods(userId, signal) {
  const params = new URLSearchParams({ userId: String(userId) })
  return request(`/frequent-foods?${params}`, { signal })
}

export function addFrequentFood(userId, foodId) {
  return request('/frequent-foods', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId, foodId }),
  })
}

export function removeFrequentFood(userId, foodId) {
  const params = new URLSearchParams({ userId: String(userId) })
  return request(`/frequent-foods/${foodId}?${params}`, { method: 'DELETE' })
}
