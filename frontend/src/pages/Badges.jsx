import { Fragment, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getBadgeProgress } from '../utils/badges.js'
import './Badges.css'

const formatDate = (date) => new Date(`${date}T12:00:00`).toLocaleDateString('en-US', {
  month: 'short', day: 'numeric', year: 'numeric',
})
const days = (count) => `${count} ${count === 1 ? 'day' : 'days'}`

function Badges({ userId }) {
  const [reload, setReload] = useState(0)
  const [result, setResult] = useState({ loading: true, error: '', streaks: null, badges: [] })

  useEffect(() => {
    if (!userId) return
    const controller = new AbortController()
    getBadgeProgress(userId, controller.signal)
      .then((data) => {
        if (!controller.signal.aborted) setResult({ ...data, loading: false, error: '' })
      })
      .catch((error) => {
        if (!controller.signal.aborted) {
          setResult({ loading: false, error: error.message, streaks: null, badges: [] })
        }
      })
    return () => controller.abort()
  }, [userId, reload])

  if (!userId) return <p>Please <Link to="/login">log in</Link> to see your badges.</p>

  return (
    <div className="badges-page">
      <div className="badges-header">
        <div>
          <h1 className="home-title">Badges</h1>
        </div>
        <button type="button" className="goals-secondary-button" disabled={result.loading}
          onClick={() => {
            setResult({ loading: true, error: '', streaks: null, badges: [] })
            setReload((value) => value + 1)
          }}>
          {result.loading ? 'Loading…' : 'Refresh'}
        </button>
      </div>

      {result.loading && <p role="status">Loading streaks and badges…</p>}
      {result.error && <p className="badges-error" role="alert">{result.error}</p>}
      {result.streaks && (
        <>
          <h2>Current streaks</h2>
          <div className="badges-streaks">
            <p><span>At least one goal</span><strong>{days(result.streaks.anyGoal)}</strong></p>
            <p><span>All goals</span><strong>{days(result.streaks.allGoals)}</strong></p>
          </div>
          <p className="badges-note">Through {formatDate(result.streaks.throughDate)}. Today does not count yet.</p>

          <h2>Earned badges ({result.badges.length})</h2>
          {[
            { type: 'ANY_GOAL_STREAK', label: 'Any Goal', description: 'At least one goal met' },
            { type: 'ALL_GOALS_STREAK', label: 'All Goals', description: 'All goals met' },
          ].map((group, index) => {
            const badges = result.badges.filter((badge) => badge.type === group.type)
            return (
              <Fragment key={group.type}>
                {index > 0 && <hr className="badges-divider" />}
                <section>
                  <h3 className="badges-group-title" id={`earned-${group.type}`}>{group.label} ({badges.length})</h3>
                  {badges.length === 0 ? (
                    <p>No badges earned in this category yet.</p>
                  ) : (
                    <ul className="badges-list">
                      {badges.map((badge) => (
                        <li className="badges-card" key={badge.badgeId}>
                          <span className="badges-medal" data-milestone={badge.number}>★</span>
                          <div>
                            <h4>{badge.name}</h4>
                            <p>{group.description} · {days(badge.number)}</p>
                            <small>Earned <time dateTime={badge.earnedDate}>{formatDate(badge.earnedDate)}</time></small>
                          </div>
                        </li>
                      ))}
                    </ul>
                  )}
                </section>
              </Fragment>
            )
          })}
        </>
      )}
    </div>
  )
}

export default Badges
