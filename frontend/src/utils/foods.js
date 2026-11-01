const API_BASE_URL = 'http://localhost:8080/api'

async function request(path, options = {}) {
  let response
  try {
    response = await fetch(`${API_BASE_URL}${path}`, options)
  } catch (error) {
    if (error.name === 'AbortError') throw error
    throw new Error('Unable to reach the server. Check that the backend is running and try again.', { cause: error })
  }
  const data = await response.json().catch(() => null)
  if (!response.ok) {
    throw new Error(data?.error || data?.detail || data?.message || `Request failed (${response.status}).`)
  }
  if (data === null) throw new Error('The server returned an unexpected response. Please try again.')
  return data
}

export function searchFoods(query, signal) {
  const params = new URLSearchParams({ query, pageSize: '20' })
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
