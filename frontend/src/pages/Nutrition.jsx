import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { addFoodLog, deleteFoodLog, getFoodLogs, importFood, searchFoods, updateFoodLogAmount, NUTRIENTS, sumNutrition } from '../utils/foods.js'
import { deleteWaterLog, getWaterLog, logWater, updateWaterLog, waterTotal } from '../utils/water.js'
import './Nutrition.css'

const FOOD_SEARCH_PAGE_SIZE = 20
const EMPTY_SEARCH = {
  foods: [], loading: false, searched: false, error: '',
  query: '', pageNumber: 1, totalPages: 0,
}

function today() {
  const date = new Date()
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const nutrientFormat = new Intl.NumberFormat(undefined, { maximumFractionDigits: 2 })

function nutrientLabel(value) {
  return Number.isFinite(value) ? nutrientFormat.format(value) : '—'
}

function Nutrition({ userId }) {
  const [date, setDate] = useState(today)
  const [reload, setReload] = useState(0)
  const [logResult, setLogResult] = useState({ key: null, foods: [], error: '' })
  const [showForm, setShowForm] = useState(false)
  const [query, setQuery] = useState('')
  const [search, setSearch] = useState(EMPTY_SEARCH)
  const [selectedFood, setSelectedFood] = useState(null)
  const [selecting, setSelecting] = useState(false)
  const [amount, setAmount] = useState('100')
  const [saving, setSaving] = useState(false)
  const [deletingId, setDeletingId] = useState(null)
  const [editingId, setEditingId] = useState(null)
  const [editAmount, setEditAmount] = useState('')
  const [editError, setEditError] = useState('')
  const [deleteError, setDeleteError] = useState('')
  const [formError, setFormError] = useState('')
  const [notice, setNotice] = useState('')
  const [deleteNotice, setDeleteNotice] = useState('')
  const searchRequest = useRef(null)
  const selectionRequest = useRef(null)
  const logKey = userId ? `${userId}:${date}:${reload}` : null
  const loadingLogs = Boolean(userId) && logResult.key !== logKey
  const foods = logResult.key === logKey ? logResult.foods : []
  const logError = logResult.key === logKey ? logResult.error : ''
  const dailyTotals = sumNutrition(foods)

  async function handleSaveAmount(event, food) {
    event.preventDefault()
    if (saving || deletingId !== null) return
    const quantity = Number(editAmount)
    if (!Number.isFinite(quantity) || quantity <= 0 || quantity > 9999999.999
      || !/^\d+(\.\d{1,3})?$/.test(editAmount)) {
      setEditError('Enter a positive amount up to 9999999.999 with up to three decimal places.')
      return
    }
    setSaving(true)
    setEditError('')
    setNotice('')
    setDeleteNotice('')
    try {
      await updateFoodLogAmount(food.foodLogId, userId, quantity)
      const items = await getFoodLogs(userId, date)
      setLogResult((previous) => previous.key !== logKey ? previous : { key: logKey, foods: items, error: '' })
      setEditingId(null)
      setNotice(`Amount for "${food.foodName}" updated.`)
    } catch (error) {
      setEditError(error.message)
    } finally {
      setSaving(false)
    }
  }

  async function handleDeleteFood(food) {
    if (!userId || saving || deletingId !== null || editingId !== null) return
    setDeletingId(food.foodLogId)
    setDeleteError('')
    setDeleteNotice('')
    setNotice('')
    try {
      await deleteFoodLog(food.foodLogId, userId)
      setLogResult((previous) => previous.key !== logKey ? previous : ({
        ...previous,
        foods: previous.foods.filter((entry) => entry.foodLogId !== food.foodLogId),
      }))
      setDeleteNotice(`"${food.foodName}" deleted.`)
    } catch (error) {
      setDeleteError(error.message)
    } finally {
      setDeletingId(null)
    }
  }

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
    setSearch(EMPTY_SEARCH)
    setSelectedFood(null)
    setSelecting(false)
    setQuery('')
    setFormError('')
  }

  function handleSearch(event) {
    event.preventDefault()
    if (!query.trim()) return
    loadSearchPage(query.trim(), 1)
  }

  async function loadSearchPage(searchQuery, pageNumber) {
    searchRequest.current?.abort()
    selectionRequest.current?.abort()
    const controller = new AbortController()
    searchRequest.current = controller
    setSelectedFood(null)
    setSelecting(false)
    setFormError('')
    setSearch((previous) => ({
      ...(pageNumber === 1 ? EMPTY_SEARCH : previous), loading: true, error: '',
    }))
    try {
      const result = await searchFoods(searchQuery, FOOD_SEARCH_PAGE_SIZE, pageNumber, controller.signal)
      if (!controller.signal.aborted) {
        setSearch({
          foods: result.foods, loading: false, searched: true, error: '',
          query: searchQuery, pageNumber: result.currentPage, totalPages: result.totalPages,
        })
      }
    } catch (error) {
      if (!controller.signal.aborted) {
        setSearch((previous) => ({ ...previous, loading: false, searched: true, error: error.message }))
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
    if (!selectedFood || !userId || saving || deletingId !== null || editingId !== null) return
    const quantity = Number(amount)
    if (!Number.isFinite(quantity) || quantity <= 0 || quantity > 9999999.999
      || !/^\d+(\.\d{1,3})?$/.test(amount)) {
      setFormError('Enter a positive amount with up to three decimal places.')
      return
    }
    setSaving(true)
    setFormError('')
    setNotice('')
    setDeleteNotice('')
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
      <header className="nutrition-page-header">
        <div className="nutrition-heading-actions">
          <h1 className="home-title">Nutrition</h1>
          <button
            className="home-primary-button"
            type="button"
            disabled={!userId || loadingLogs || Boolean(logError) || showForm || deletingId !== null || editingId !== null}
            onClick={() => { setShowForm(true); setNotice(''); setDeleteNotice('') }}
          >
            Add Food
          </button>
        </div>
        <label className="nutrition-date" htmlFor="food-log-date">
          Date
          <input
            id="food-log-date"
            type="date"
            min="1000-01-01"
            max="3000-12-31"
            value={date}
            disabled={saving || deletingId !== null}
            required
            onChange={(event) => {
              if (event.target.validity.valid && event.target.value) {
                setDate(event.target.value)
                setNotice('')
                setDeleteNotice('')
                setDeleteError('')
                setEditingId(null)
                setEditError('')
              }
            }}
          />
        </label>
      </header>

      {!userId && <p className="nutrition-message"><Link to="/login">Log in</Link> to see and add your foods.</p>}

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
          {search.searched && !search.loading && !search.error && search.foods.length === 0 && (
            <p className="nutrition-message" role="status">No matching foods. Try another name.</p>
          )}
          {search.foods.length > 0 && (
            <ul className="nutrition-search-results">
              {search.foods.map((food) => (
                <li key={food.fdcId}>
                  <button
                    type="button"
                    className="nutrition-food-option"
                    disabled={saving || search.loading}
                    onClick={() => selectFood(food)}
                  >
                    <span>{food.description}</span>
                    <small>{[food.brandName, food.dataType].filter(Boolean).join(' · ')}</small>
                  </button>
                </li>
              ))}
            </ul>
          )}
          {search.totalPages > 0 && (
            <nav className="nutrition-search-pagination">
              <button
                className="nutrition-secondary-button"
                type="button"
                disabled={search.loading || saving || search.pageNumber <= 1}
                onClick={() => loadSearchPage(search.query, search.pageNumber - 1)}
              >
                Previous
              </button>
              <span role="status">Page {search.pageNumber} of {search.totalPages}</span>
              <button
                className="nutrition-secondary-button"
                type="button"
                disabled={search.loading || saving || search.pageNumber >= search.totalPages}
                onClick={() => loadSearchPage(search.query, search.pageNumber + 1)}
              >
                Next
              </button>
            </nav>
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
              <button className="home-primary-button" type="submit" disabled={saving || deletingId !== null || editingId !== null || loadingLogs || Boolean(logError)}>
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
              Nutrition for the amounts logged on the selected day.
            </p>
            {notice && <p className="nutrition-success" role="status">{notice}</p>}
            {deleteNotice && <p className="nutrition-success" role="status">{deleteNotice}</p>}
          </div>
        </div>
        {loadingLogs && <p className="nutrition-message" role="status">Loading foods…</p>}
        {deleteError && <p className="nutrition-error" role="alert">{deleteError}</p>}
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
          <>
            <div className="nutrition-table-scroll" role="region" tabIndex={0}>
              <table className="nutrition-table">
                <thead>
                  <tr>
                    <th scope="col">Food</th>
                    <th scope="col">Amount</th>
                    <th scope="col">Actions</th>
                    {NUTRIENTS.map(({ key, label, unit }) => (
                      <th key={key} scope="col">{label} <span className="nutrition-table-unit">({unit})</span></th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  <tr className="nutrition-total-row">
                    <th scope="row">Daily total</th>
                    <td>{foods.length} {foods.length === 1 ? 'entry' : 'entries'}</td>
                    <td />
                    {NUTRIENTS.map(({ key }) => (
                      <td key={key}>{nutrientLabel(dailyTotals[key])}</td>
                    ))}
                  </tr>
                  {foods.map((food) => (
                    <tr key={food.foodLogId}>
                      <th scope="row">{food.foodName}</th>
                      <td>
                        {editingId === food.foodLogId ? (
                          <form className="nutrition-inline-amount" onSubmit={(event) => handleSaveAmount(event, food)}>
                            <div className="nutrition-inline-amount-controls">
                              <input
                                type="number"
                                min="0.001"
                                max="9999999.999"
                                step="0.001"
                                value={editAmount}
                                onChange={(event) => setEditAmount(event.target.value)}
                                disabled={saving}
                                required
                              />
                              <span>{food.unit}</span>
                              <button className="nutrition-secondary-button" type="submit" disabled={saving}>
                                {saving ? 'Saving…' : 'Save'}
                              </button>
                              <button className="nutrition-secondary-button" type="button" disabled={saving}
                                onClick={() => { setEditingId(null); setEditError('') }}>
                                Cancel
                              </button>
                            </div>
                            {editError && <p className="nutrition-error" role="alert">{editError}</p>}
                          </form>
                        ) : (
                          <div className="nutrition-inline-amount-controls">
                            <span>{nutrientLabel(food.amount)} {food.unit}</span>
                            <button className="nutrition-secondary-button" type="button"
                              disabled={saving || deletingId !== null || editingId !== null || showForm}
                              onClick={() => { setEditingId(food.foodLogId); setEditAmount(String(food.amount)); setEditError('') }}>
                              Edit
                            </button>
                          </div>
                        )}
                      </td>
                      <td>
                        <button
                          className="nutrition-delete-button"
                          type="button"
                          disabled={saving || deletingId !== null || editingId !== null}
                          onClick={() => handleDeleteFood(food)}
                        >
                          {deletingId === food.foodLogId ? 'Deleting…' : 'Delete'}
                        </button>
                      </td>
                      {NUTRIENTS.map(({ key }) => (
                        <td key={key}>{nutrientLabel(food.nutrition?.[key])}</td>
                      ))}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <p id="nutrition-table-note" className="nutrition-description">
              — means unavailable. Daily totals sum available values and skip missing nutrients, so totals may be incomplete.
            </p>
          </>
        )}
      </section>

      <WaterLog key={`${userId}:${date}`} userId={userId} date={date} />
    </div>
  )
}

const formatAmount = new Intl.NumberFormat(undefined, { maximumFractionDigits: 3 })
const formLabels = { create: 'Log Water', add: 'Add Amount', update: 'Update Amount' }

function WaterLog({ userId, date }) {
  const [result, setResult] = useState({ entry: null, loading: Boolean(userId), error: '' })
  const [retry, setRetry] = useState(0)
  const [mode, setMode] = useState(null)
  const [amount, setAmount] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const mutation = useRef(null)
  const entry = result.entry

  useEffect(() => {
    if (!userId) return
    const controller = new AbortController()
    getWaterLog(userId, date, controller.signal)
      .then((entry) => {
        if (!controller.signal.aborted) setResult({ entry, loading: false, error: '' })
      })
      .catch((error) => {
        if (!controller.signal.aborted) setResult({ entry: null, loading: false, error: error.message })
      })
    return () => controller.abort()
  }, [userId, date, retry])

  useEffect(() => () => mutation.current?.abort(), [])

  function openForm(nextMode) {
    setMode(nextMode)
    setAmount(nextMode === 'update' ? String(entry.amountMl) : '')
    setError('')
    setNotice('')
  }

  async function saveWater(event) {
    event.preventDefault()
    if (busy || !userId) return
    let total
    try {
      total = waterTotal(entry?.amountMl ?? 0, amount, mode)
    } catch (error) {
      setError(error.message)
      return
    }
    const controller = new AbortController()
    mutation.current = controller
    setBusy(true)
    setError('')
    setNotice('')
    try {
      let saved
      if (mode === 'create') {
        saved = await logWater(userId, date, total, controller.signal)
      } else {
        await updateWaterLog(entry.waterLogId, userId, total, controller.signal)
        saved = { ...entry, amountMl: total }
      }
      if (!controller.signal.aborted) {
        setResult({ entry: saved, loading: false, error: '' })
        setMode(null)
        setAmount('')
        setNotice(mode === 'add' ? 'Water added to the daily total.' : mode === 'update' ? 'Water amount updated.' : 'Water logged.')
      }
    } catch (error) {
      if (!controller.signal.aborted) setError(error.message)
    } finally {
      if (!controller.signal.aborted) setBusy(false)
    }
  }

  async function removeWater() {
    if (busy || !entry) return
    const controller = new AbortController()
    mutation.current = controller
    setBusy(true)
    setError('')
    setNotice('')
    try {
      await deleteWaterLog(entry.waterLogId, userId, controller.signal)
      if (!controller.signal.aborted) {
        setResult({ entry: null, loading: false, error: '' })
        setMode(null)
        setNotice('Water log deleted for the selected day.')
      }
    } catch (error) {
      if (!controller.signal.aborted) setError(error.message)
    } finally {
      if (!controller.signal.aborted) setBusy(false)
    }
  }

  return (
    <section className="nutrition-water">
      <h2 id="water-title">Water</h2>
      <p className="nutrition-description">Water intake for the selected day.</p>
      {notice && <p className="nutrition-success" role="status">{notice}</p>}
      {result.loading && <p className="nutrition-message" role="status">Loading water…</p>}
      {result.error && (
        <div className="nutrition-error" role="alert">
          <p>{result.error}</p>
          <button className="nutrition-secondary-button" type="button" onClick={() => {
            setResult({ entry: null, loading: true, error: '' })
            setRetry(retry + 1)
          }}>Try again</button>
        </div>
      )}
      {!result.loading && !result.error && (
        <>
          {entry && <p className="nutrition-water-total">{formatAmount.format(entry.amountMl)} ml</p>}
          {!mode && (
            <div className="nutrition-water-actions">
              {entry ? (
                <>
                  <button className="home-primary-button" type="button" disabled={busy} onClick={() => openForm('add')}>Add Amount</button>
                  <button className="nutrition-secondary-button" type="button" disabled={busy} onClick={() => openForm('update')}>Update</button>
                  <button className="nutrition-delete-button" type="button" disabled={busy} onClick={removeWater}>{busy ? 'Deleting…' : 'Delete'}</button>
                </>
              ) : (
                <button className="home-primary-button" type="button" disabled={!userId || busy} onClick={() => openForm('create')}>Log Water</button>
              )}
            </div>
          )}
          {mode && (
            <form className="nutrition-water-form" onSubmit={saveWater}>
              <label htmlFor="water-amount">{mode === 'add' ? 'Amount to add (ml)' : mode === 'update' ? 'New daily total (ml)' : 'Amount (ml)'}</label>
              {mode === 'add' && <p className="nutrition-description">This amount will be added to your current total.</p>}
              {mode === 'update' && <p className="nutrition-description">This amount will replace your current total.</p>}
              <input id="water-amount" type="number" min="0.001" max="9999999" step="0.001"
                value={amount} onChange={(event) => setAmount(event.target.value)} disabled={busy} required />
              <div className="nutrition-water-actions">
                <button className="home-primary-button" type="submit" disabled={busy}>{busy ? 'Saving…' : formLabels[mode]}</button>
                <button className="nutrition-secondary-button" type="button" disabled={busy} onClick={() => { setMode(null); setError('') }}>Cancel</button>
              </div>
            </form>
          )}
        </>
      )}
      {error && <p className="nutrition-error" role="alert">{error}</p>}
    </section>
  )
}

export default Nutrition
