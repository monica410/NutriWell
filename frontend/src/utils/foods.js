import { request } from './api.js'

export function searchFoods(query, pageSize = 20, pageNumber = 1, signal) {
  const params = new URLSearchParams({ query, pageSize: String(pageSize), pageNumber: String(pageNumber) })
  return request(`/foods/search?${params}`, { signal })
}

export function importFood(fdcId, signal) {
  return request(`/foods/${fdcId}/import`, { method: 'POST', signal })
}

export function getFoodLogs(userId, date, signal) {
  const params = new URLSearchParams({ userId: String(userId), date })
  return request(`/food-logs?${params}`, { signal })
}

export function addFoodLog(log) {
  return request('/food-logs', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(log),
  })
}

export function deleteFoodLog(foodLogId, userId) {
  const params = new URLSearchParams({ userId: String(userId) })
  return request(`/food-logs/${foodLogId}?${params}`, { method: 'DELETE' })
}

export function updateFoodLogAmount(foodLogId, userId, amount) {
  return request(`/food-logs/${foodLogId}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId, amount }),
  })
}

export const NUTRIENTS = [
  { key: 'caloriesKcal', label: 'Calories', unit: 'kcal' },
  { key: 'proteinG', label: 'Protein', unit: 'g' },
  { key: 'carbohydratesG', label: 'Carbs', unit: 'g' },
  { key: 'totalFatG', label: 'Fat', unit: 'g' },
  { key: 'saturatedFatG', label: 'Saturated fat', unit: 'g' },
  { key: 'fiberG', label: 'Fiber', unit: 'g' },
  { key: 'totalSugarG', label: 'Sugar', unit: 'g' },
  { key: 'sodiumMg', label: 'Sodium', unit: 'mg' },
]

// Sum available values, skipping missing nutrients in individual food entries.
export function sumNutrition(foods) {
  return Object.fromEntries(NUTRIENTS.map(({ key }) => {
    let total = 0
    for (const food of foods) {
      const value = food.nutrition?.[key]
      if (Number.isFinite(value)) total += value
    }
    return [key, total]
  }))
}