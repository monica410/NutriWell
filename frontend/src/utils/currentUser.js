const USER_ID_KEY = 'nutriwell.userId'

export function getCurrentUserId() {
  try {
    const userId = Number(sessionStorage.getItem(USER_ID_KEY))
    return Number.isSafeInteger(userId) && userId > 0 ? userId : null
  } catch {
    return null
  }
}

export function saveCurrentUserId(userId) {
  if (!Number.isSafeInteger(userId) || userId <= 0) {
    throw new Error('The server did not return a valid user ID. Please log in again.')
  }
  sessionStorage.setItem(USER_ID_KEY, String(userId))
}
