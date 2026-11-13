const API_BASE_URL = 'http://localhost:8080/api'

export async function request(path, options = {}) {
  let response
  try {
    response = await fetch(`${API_BASE_URL}${path}`, options)
  } catch (error) {
    if (error.name === 'AbortError') throw error
    throw new Error('Unable to reach the server. Check that the backend is running and try again.', { cause: error })
  }
  if (response.status === 204) return null
  const data = await response.json().catch(() => null)
  if (!response.ok) {
    const error = new Error(data?.error || data?.detail || data?.message || `Request failed (${response.status}).`)
    error.status = response.status
    throw error
  }
  if (data === null) throw new Error('The server returned an unexpected response. Please try again.')
  return data
}
