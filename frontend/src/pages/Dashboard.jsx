import { useEffect, useState } from 'react'
import { getFoodLogs, sumNutrition } from '../utils/foods.js'
import { getWaterLog } from '../utils/water.js'
import { getBadgeProgress } from '../utils/badges.js'
import { getCurrentUserId } from '../utils/currentUser.js'
import './Dashboard.css'

const today = () => new Date().toLocaleDateString('en-CA')

const formatNumber = (value, maximumFractionDigits = 0) =>
  new Intl.NumberFormat('en-US', { maximumFractionDigits }).format(value || 0)

function Dashboard({ active = true }) {
  const userId = getCurrentUserId()
  const [summary, setSummary] = useState({ foods: [], nutrition: {}, water: null })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [badgeProgress, setBadgeProgress] = useState(null)
  const [badgeError, setBadgeError] = useState('')

  useEffect(() => {
    if (!active || !userId) return
    const controller = new AbortController()
    const date = today()
    Promise.all([
      getFoodLogs(userId, date, controller.signal),
      getWaterLog(userId, date, controller.signal),
    ])
      .then(([foods, water]) => {
        if (!controller.signal.aborted) {
          setSummary({ foods, nutrition: sumNutrition(foods), water })
          setError('')
          setLoading(false)
        }
      })
      .catch((loadError) => {
        if (!controller.signal.aborted) {
          setError(loadError.message)
          setLoading(false)
        }
      })
    return () => controller.abort()
  }, [active, userId])

  useEffect(() => {
    if (!active || !userId) return
    const controller = new AbortController()
    getBadgeProgress(userId, controller.signal)
      .then((progress) => {
        if (!controller.signal.aborted) {
          setBadgeProgress(progress)
          setBadgeError('')
        }
      })
      .catch((loadError) => {
        if (!controller.signal.aborted) setBadgeError(loadError.message)
      })
    return () => controller.abort()
  }, [active, userId])

  if (!userId) {
    return <p className="dashboard-message">Please log in to see your daily dashboard.</p>
  }

  const calories = summary.nutrition.caloriesKcal || 0
  const protein = summary.nutrition.proteinG || 0
  const water = summary.water?.amountMl || 0
  const highestBadge = (type) => {
    if (!badgeProgress) return '—'
    const highest = badgeProgress.badges
      .filter((badge) => badge.type === type)
      .reduce((max, badge) => Math.max(max, badge.number), 0)
    return highest > 0 ? `${highest}-day badge` : 'None yet'
  }
  const streakLabel = (value) => value == null ? '—' : `${value} ${value === 1 ? 'day' : 'days'}`

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <div>
          <p className="dashboard-eyebrow">{new Date().toLocaleDateString()}</p>
          <h1 className="home-title">Today&apos;s Progress</h1>
          <p className="dashboard-subtitle">A quick view of your nutrition and hydration.</p>
        </div>
      </div>

      {loading && <p role="status">Loading dashboard…</p>}
      {error && <p className="dashboard-error" role="alert">{error}</p>}

      <div className="dashboard-cards">
        <article className="dashboard-card">
          <span className="dashboard-card-label">Calories</span>
          <strong>{formatNumber(calories)} kcal</strong>
          <small>from today&apos;s food</small>
        </article>
        <article className="dashboard-card">
          <span className="dashboard-card-label">Protein</span>
          <strong>{formatNumber(protein, 1)} g</strong>
          <small>from today&apos;s food</small>
        </article>
        <article className="dashboard-card">
          <span className="dashboard-card-label">Water</span>
          <strong>{formatNumber(water)} ml</strong>
          <small>logged today</small>
        </article>
      </div>

      <section className="dashboard-foods">
        <h2>Today&apos;s Food</h2>
        {summary.foods.length === 0 ? (
          <p className="dashboard-message">No food has been logged today.</p>
        ) : (
          <ul>
            {summary.foods.map((food) => (
              <li key={food.foodLogId}>
                <span>{food.foodName}</span>
                <strong>{formatNumber(food.nutrition?.caloriesKcal)} kcal</strong>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="dashboard-achievements">
        <h2 className="home-title">Streaks &amp; Badges</h2>
        <p className="dashboard-message">Current counts through yesterday.</p>
        {badgeError && <p className="dashboard-error" role="alert">{badgeError}</p>}
        <div className="dashboard-achievement-row">
          {[
            ['Any goal streak', streakLabel(badgeProgress?.streaks.anyGoal), true],
            ['All goals streak', streakLabel(badgeProgress?.streaks.allGoals), true],
            ['Highest any goal badge', highestBadge('ANY_GOAL_STREAK')],
            ['Highest all goals badge', highestBadge('ALL_GOALS_STREAK')],
          ].map(([label, value, isStreak]) => (
            <article className={`dashboard-achievement-card streak-card${isStreak ? '' : ' dashboard-badge-card'}`} key={label}>
              <span className="dashboard-card-label">{label}</span>
              <strong>{value}</strong>
              <span className={isStreak ? 'streak-fire' : 'dashboard-badge-star'} aria-hidden="true">{isStreak ? 'ঌ' : '★'}</span>
            </article>
          ))}
        </div>
      </section>
    </div>
  )
}

export default Dashboard
