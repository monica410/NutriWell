import { request } from './api.js'

export async function getWaterLog(userId, date, signal) {
  const params = new URLSearchParams({ userId: String(userId), date })
  try {
    return await request(`/water-logs?${params}`, { signal })
  } catch (error) {
    if (error.status === 404 && error.message === 'Water log entry was not found.') return null
    throw error
  }
}

export function logWater(userId, loggedDate, amountMl, signal) {
  return request('/water-logs', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId, loggedDate, amountMl }),
    signal,
  })
}

export function updateWaterLog(waterLogId, userId, amountMl, signal) {
  return request(`/water-logs/${waterLogId}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId, amountMl }),
    signal,
  })
}

export function deleteWaterLog(waterLogId, userId, signal) {
  const params = new URLSearchParams({ userId: String(userId) })
  return request(`/water-logs/${waterLogId}?${params}`, { method: 'DELETE', signal })
}

export function waterTotal(currentAmount, enteredAmount, mode) {
  const quantity = Number(enteredAmount)
  if (!/^\d+(\.\d{1,3})?$/.test(enteredAmount) || quantity <= 0) {
    throw new Error('Enter a positive amount with up to three decimal places.')
  }
  const total = mode === 'add' ? Math.round((currentAmount + quantity) * 1000) / 1000 : quantity
  if (total > 9999999) throw new Error('The daily total must be no more than 9999999 ml.')
  return total
}
