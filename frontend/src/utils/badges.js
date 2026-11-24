import { request } from './api.js'

export async function getBadgeProgress(userId, signal) {
  const params = new URLSearchParams({ userId: String(userId) })
  const streaks = await request(`/badges/streaks?${params}`, { signal })
  const badges = await request(`/badges?${params}`, { signal })
  return { streaks, badges }
}
