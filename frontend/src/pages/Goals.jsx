import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getFoodLogs, NUTRIENTS, sumNutrition } from '../utils/foods.js'
import { getWaterLog } from '../utils/water.js'
import { getGoals, parseGoalValue, removeAllGoals, removeGoal, saveGoal } from '../utils/goals.js'
import './Goals.css'

const TARGETS = [...NUTRIENTS, { key: 'waterMl', label: 'Water', unit: 'ml' }]

function today() {
  const date = new Date()
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

function Goals({ userId, active = true }) {
  const [reload, setReload] = useState(0)
  const [result, setResult] = useState({ key: null, goals: null, error: '' })
  const [progress, setProgress] = useState({ key: null, totals: null, error: '' })
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [editingTarget, setEditingTarget] = useState(null)
  const date = today()
  const loadKey = userId ? `${userId}:${date}:${reload}` : null
  const loading = Boolean(userId) && result.key !== loadKey
  const goals = result.key === loadKey ? result.goals : null
  const loadError = result.key === loadKey ? result.error : ''
  const disabled = loading || busy || Boolean(loadError)
  const savedTargets = TARGETS.filter(({ key }) => goals?.[key] != null)
  const totals = progress.key === loadKey ? progress.totals : null
  const progressError = progress.key === loadKey ? progress.error : ''

  useEffect(() => {
    if (!userId || !active) return
    const controller = new AbortController()
    getGoals(userId, controller.signal)
      .then((savedGoals) => {
        if (!controller.signal.aborted) setResult({ key: loadKey, goals: savedGoals, error: '' })
      })
      .catch((loadError) => {
        if (!controller.signal.aborted) setResult({ key: loadKey, goals: null, error: loadError.message })
      })
    return () => controller.abort()
  }, [userId, loadKey, active])

  useEffect(() => {
    if (!userId || !active) return
    const controller = new AbortController()
    Promise.all([
      getFoodLogs(userId, date, controller.signal),
      getWaterLog(userId, date, controller.signal),
    ])
      .then(([foods, water]) => {
        if (!controller.signal.aborted) {
          setProgress({ key: loadKey, totals: { ...sumNutrition(foods), waterMl: water?.amountMl ?? 0 }, error: '' })
        }
      })
      .catch((loadError) => {
        if (!controller.signal.aborted) setProgress({ key: loadKey, totals: null, error: loadError.message })
      })
    return () => controller.abort()
  }, [userId, date, loadKey, active])

  async function performAction(action, message) {
    if (!userId || disabled) return false
    setBusy(true)
    setError('')
    setNotice('')
    try {
      const savedGoals = await action()
      setResult({ key: loadKey, goals: savedGoals, error: '' })
      setNotice(message)
      return true
    } catch (actionError) {
      setError(actionError.message)
      return false
    } finally {
      setBusy(false)
    }
  }

  async function handleSave(event, target) {
    event.preventDefault()
    let quantity
    try {
      quantity = parseGoalValue(new FormData(event.currentTarget).get('amount'))
    } catch (validationError) {
      setNotice('')
      setError(validationError.message)
      return
    }
    const saved = await performAction(
      () => saveGoal(userId, target.key, quantity, goals !== null),
      `${target.label} goal saved.`,
    )
    if (saved) {
      setEditingTarget(null)
    }
  }

  function handleRemove(target) {
    performAction(async () => {
      await removeGoal(userId, target.key)
      return { ...goals, [target.key]: null }
    }, `${target.label} goal removed.`)
  }

  async function handleRemoveAll() {
    const removed = await performAction(async () => {
      await removeAllGoals(userId)
      return null
    }, 'All goals removed.')
    if (removed) setEditingTarget(null)
  }

  if (!userId) {
    return <p className="goals-message">Please <Link to="/login">log in</Link> to manage your goals.</p>
  }

  return (
    <div className="goals-page">
      <div className="goals-header">
        <div>
          <h1 className="home-title">Personal Goals</h1>
          <p className="goals-description">Daily nutrition and water goals.</p>
        </div>
        <div className="goals-actions">
          <button type="button" className="goals-secondary-button" disabled={loading || busy}
            onClick={() => {
              setError(''); setNotice(''); setEditingTarget(null)
              setReload((value) => value + 1)
            }}>
            Refresh
          </button>
          <button type="button" className="goals-remove-button" disabled={disabled || goals === null}
            onClick={handleRemoveAll}>
            Remove all goals
          </button>
        </div>
      </div>

      {!loading && !loadError && (
        <p className="goals-message">
          {savedTargets.length === 0 ? 'No goals set yet.' : `${savedTargets.length} goal(s) set.`}
        </p>
      )}

      {loading && <p className="goals-message" role="status">Loading goals…</p>}
      {(loadError || error) && <p className="goals-error" role="alert">{loadError || error}</p>}
      {notice && <p className="goals-success" role="status">{notice}</p>}
      {progressError && <p className="goals-error" role="alert">Today’s progress is unavailable. {progressError} Use Refresh to try again.</p>}

      <fieldset className="goals-fields" disabled={disabled}>
        {TARGETS.map((target) => {
          const value = goals?.[target.key]
          const isSet = value != null
          const current = totals?.[target.key]
          const progressLabel = `${current == null ? '—' : current} / ${isSet ? value : '—'} ${target.unit}`
          const percentage = (!Number.isFinite(current) || !Number.isFinite(value) || value <= 0)
            ? null : Math.max(0, (current / value) * 100)

          return (
            <section key={target.key} className="goals-target">
              <h2 id={`goal-title-${target.key}`}>{target.label} <span>({target.unit})</span></h2>
              {isSet && (
                <div className="goals-progress-track" role="progressbar">
                  <div className="goals-progress-bar" style={{ width: `${Math.min(100, percentage ?? 0)}%` }} />
                  <span className="goals-progress-label">{progressLabel}</span>
                </div>
              )}
              {editingTarget === target.key ? (
                <form className="goals-edit-form" onSubmit={(event) => handleSave(event, target)}>
                  <label htmlFor={`goal-${target.key}`}>{isSet ? 'New daily target' : 'Daily target'} ({target.unit})</label>
                  <input id={`goal-${target.key}`} name="amount" type="number" min="0" max="9999999.999"
                    step="0.001" defaultValue={value ?? ''} required />
                  <div className="goals-actions">
                    <button type="submit" className="home-primary-button">{busy ? 'Saving…' : isSet ? 'Save' : 'Add goal'}</button>
                    <button type="button" className="goals-secondary-button" onClick={() => { setEditingTarget(null); setError('') }}>Cancel</button>
                  </div>
                </form>
              ) : (
                <div className="goals-actions">
                  <button type="button" className={isSet ? 'goals-secondary-button' : 'home-primary-button'}
                    onClick={() => { setEditingTarget(target.key); setError(''); setNotice('') }}>{isSet ? 'Edit' : 'Add goal'}</button>
                  {isSet && <button type="button" className="goals-remove-button"
                    onClick={() => handleRemove(target)}>Remove</button>}
                </div>
              )}
            </section>
          )
        })}
      </fieldset>
      {savedTargets.length > 0 && <p className="goals-description">Progress uses today’s total food nutrition values and logged water. Missing nutrition values may make totals incomplete.</p>}
    </div>
  )
}

export default Goals
