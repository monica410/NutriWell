import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { addFoodLog, getFoodLogs, importFood, searchFoods } from '../utils/foods.js'

function today() {
  const date = new Date()
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

function caloriesLabel(value) {
  return value == null ? 'Calories unavailable' : `${new Intl.NumberFormat(undefined, { maximumFractionDigits: 2 }).format(value)} kcal`
}

function Nutrition({ userId }) {
  const [date, setDate] = useState(today)
  const [reload, setReload] = useState(0)
  const [logResult, setLogResult] = useState({ key: null, foods: [], error: '' })
  const [showForm, setShowForm] = useState(false)
  const [query, setQuery] = useState('')
  const [search, setSearch] = useState({ foods: [], loading: false, searched: false, error: '' })
  const [selectedFood, setSelectedFood] = useState(null)
  const [selecting, setSelecting] = useState(false)
  const [amount, setAmount] = useState('100')
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState('')
  const [notice, setNotice] = useState('')
  const searchRequest = useRef(null)
  const selectionRequest = useRef(null)
  const logKey = userId ? `${userId}:${date}:${reload}` : null
  const loadingLogs = Boolean(userId) && logResult.key !== logKey
  const foods = logResult.key === logKey ? logResult.foods : []
  const logError = logResult.key === logKey ? logResult.error : ''

  useEffect(() => {
    if (!userId || !date) return
    const controller = new AbortController()
    getFoodLogs(userId, date, controller.signal)
      .then((items) => {
        if (!controller.signal.aborted) setLogResult({ key: logKey, foods: items, error: '' })
      })
      .catch((error) => {
        if (!controller.signal.aborted) setLogResult({ key: logKey, foods: [], error: error.message })
      })
    return () => controller.abort()
  }, [userId, date, logKey])

  useEffect(() => () => {
    searchRequest.current?.abort()
    selectionRequest.current?.abort()
  }, [])

  function closeForm() {
    searchRequest.current?.abort()
    selectionRequest.current?.abort()
    setShowForm(false)
    setSearch({ foods: [], loading: false, searched: false, error: '' })
    setSelectedFood(null)
    setSelecting(false)
    setQuery('')
    setFormError('')
  }

  async function handleSearch(event) {
    event.preventDefault()
    if (!query.trim()) return
    searchRequest.current?.abort()
    selectionRequest.current?.abort()
    const controller = new AbortController()
    searchRequest.current = controller
    setSelectedFood(null)
    setSelecting(false)
    setFormError('')
    setSearch({ foods: [], loading: true, searched: false, error: '' })
    try {
      const result = await searchFoods(query.trim(), controller.signal)
      if (!controller.signal.aborted) {
        setSearch({ foods: result.foods, loading: false, searched: true, error: '' })
      }
    } catch (error) {
      if (!controller.signal.aborted) {
        setSearch({ foods: [], loading: false, searched: true, error: error.message })
      }
    }
  }

  async function selectFood(food) {
    selectionRequest.current?.abort()
    const controller = new AbortController()
    selectionRequest.current = controller
    setSelecting(true)
    setSelectedFood(null)
    setFormError('')
    try {
      const imported = await importFood(food.fdcId, controller.signal)
      if (!controller.signal.aborted) {
        setSelectedFood(imported)
        setAmount('100')
      }
    } catch (error) {
      if (!controller.signal.aborted) setFormError(error.message)
    } finally {
      if (!controller.signal.aborted) setSelecting(false)
    }
  }

  async function handleAddFood(event) {
    event.preventDefault()
    if (!selectedFood || !userId || saving) return
    const quantity = Number(amount)
    if (!Number.isFinite(quantity) || quantity <= 0 || quantity > 9999999.999
      || !/^\d+(\.\d{1,3})?$/.test(amount)) {
      setFormError('Enter a positive amount with up to three decimal places.')
      return
    }
    setSaving(true)
    setFormError('')
    setNotice('')
    try {
      const food = await addFoodLog({
        userId,
        fdcId: Number(selectedFood.fdcId),
        amount: quantity,
        unit: selectedFood.unit,
        loggedDate: date,
      })
      setLogResult((previous) => ({
        key: logKey,
        foods: [...(previous.key === logKey ? previous.foods : []), food],
        error: '',
      }))
      setNotice(`"${food.foodName}" added.`)
      closeForm()
    } catch (error) {
      setFormError(error.message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="home-section">
      <h1 className="home-title">Nutrition</h1>
      <button
        className="home-primary-button"
        type="button"
        disabled={!userId || loadingLogs || Boolean(logError) || showForm}
        onClick={() => { setShowForm(true); setNotice('') }}
      >
        Add Food
      </button>

      {!userId && <p className="nutrition-message"><Link to="/login">Log in</Link> to see and add your foods.</p>}
      {notice && <p className="nutrition-success" role="status">{notice}</p>}

      {showForm && (
        <section id="add-food-form" className="nutrition-add-form">
          <div className="nutrition-section-header">
            <h2 id="add-food-title">Add Food</h2>
            <button className="nutrition-secondary-button" type="button" disabled={saving} onClick={closeForm}>
              Cancel
            </button>
          </div>
          <form className="nutrition-search-form" onSubmit={handleSearch}>
            <label htmlFor="food-search">Search for a food</label>
            <div className="nutrition-search-controls">
              <input
                id="food-search"
                type="search"
                value={query}
                onChange={(event) => setQuery(event.target.value)}
                placeholder="For example, apple"
                disabled={search.loading || saving}
                required
              />
              <button className="home-primary-button" type="submit" disabled={search.loading || saving || !query.trim()}>
                {search.loading ? 'Searching…' : 'Search'}
              </button>
            </div>
          </form>
          {search.error && <p className="nutrition-error" role="alert">{search.error}</p>}
          {search.searched && !search.error && search.foods.length === 0 && (
            <p className="nutrition-message" role="status">No matching foods. Try another name.</p>
          )}
          {search.foods.length > 0 && (
            <ul className="nutrition-search-results">
              {search.foods.map((food) => (
                <li key={food.fdcId}>
                  <button
                    type="button"
                    className="nutrition-food-option"
                    disabled={saving}
                    onClick={() => selectFood(food)}
                  >
                    <span>{food.description}</span>
                    <small>{[food.brandName, food.dataType].filter(Boolean).join(' · ')}</small>
                  </button>
                </li>
              ))}
            </ul>
          )}
          {selecting && <p className="nutrition-message" role="status">Loading food details…</p>}
          {selectedFood && (
            <form className="nutrition-amount-form" onSubmit={handleAddFood}>
              <h3>{selectedFood.name}</h3>
              <label htmlFor="food-amount">Amount ({selectedFood.unit})</label>
              <input
                id="food-amount"
                type="number"
                min="0.001"
                max="9999999.999"
                step="0.001"
                value={amount}
                onChange={(event) => setAmount(event.target.value)}
                disabled={saving}
                required
              />
              <button className="home-primary-button" type="submit" disabled={saving || loadingLogs || Boolean(logError)}>
                {saving ? 'Adding…' : 'Add to Foods'}
              </button>
            </form>
          )}
          {formError && <p className="nutrition-error" role="alert">{formError}</p>}
        </section>
      )}

      <section className="nutrition-foods">
        <div className="nutrition-section-header">
          <div>
            <h2 id="foods-title">Foods</h2>
            <p className="nutrition-description">
              Foods and calories on the selected day for now. More details will be added later.
            </p>
          </div>
          <label className="nutrition-date" htmlFor="food-log-date">
            Date
            <input
              id="food-log-date"
              type="date"
              min="1000-01-01"
              max="3000-12-31"
              value={date}
              disabled={saving}
              required
              onChange={(event) => {
                if (event.target.validity.valid && event.target.value) {
                  setDate(event.target.value)
                  setNotice('')
                }
              }}
            />
          </label>
        </div>
        {loadingLogs && <p className="nutrition-message" role="status">Loading foods…</p>}
        {logError && (
          <div className="nutrition-error" role="alert">
            <p>{logError}</p>
            <button className="nutrition-secondary-button" type="button" disabled={saving} onClick={() => setReload(reload + 1)}>
              Try again
            </button>
          </div>
        )}
        {userId && !loadingLogs && !logError && foods.length === 0 && (
          <p className="nutrition-message">No foods added for this day yet.</p>
        )}
        {foods.length > 0 && (
          <ul className="nutrition-food-list">
            {foods.map((food) => (
              <li key={food.foodLogId} className="nutrition-food-row">
                <div>
                  <h3>{food.foodName}</h3>
                  <p>{food.amount} {food.unit}</p>
                </div>
                <span className="nutrition-calories">{caloriesLabel(food.nutrition?.caloriesKcal)}</span>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  )
}

export default Nutrition
