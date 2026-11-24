import { useCallback, useEffect, useState } from 'react'
import { getFoodLogs, sumNutrition } from '../utils/foods.js'
import { getWaterLog } from '../utils/water.js'
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

  const loadSummary = useCallback(async (signal) => {
    if (!userId) return

    try {
      const date = today()
      const [foods, water] = await Promise.all([
        getFoodLogs(userId, date, signal),
        getWaterLog(userId, date, signal),
      ])
      if (!signal?.aborted) {
        setSummary({ foods, nutrition: sumNutrition(foods), water })
        setError('')
      }
    } catch (loadError) {
      if (!signal?.aborted && loadError.name !== 'AbortError') setError(loadError.message)
    } finally {
      if (!signal?.aborted) setLoading(false)
    }
  }, [userId])

  useEffect(() => {
    if (!active || !userId) return
    const controller = new AbortController()
    // eslint-disable-next-line react-hooks/set-state-in-effect -- State updates follow the asynchronous requests.
    loadSummary(controller.signal)
    return () => controller.abort()
  }, [loadSummary, active, userId])

  if (!userId) {
    return <p className="dashboard-message">Please log in to see your daily dashboard.</p>
  }

  const calories = summary.nutrition.caloriesKcal || 0
  const protein = summary.nutrition.proteinG || 0
  const water = summary.water?.amountMl || 0

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
    </div>
  )
}

export default Dashboard
