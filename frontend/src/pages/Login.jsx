import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { saveCurrentUserId } from '../utils/currentUser.js'
import './Auth.css'

function Login() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ username: '', password: '' })
  const [error, setError] = useState('')

  function updateField(event) {
    setForm({ ...form, [event.target.name]: event.target.value })
  }

  async function login(event) {
    event.preventDefault()
    setError('')

    try {
      const response = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form),
      })
      const data = await response.json()
      if (!response.ok) {
        throw new Error(data.error || 'Unable to log in.')
      }
      saveCurrentUserId(data.userId)
      navigate('/home')
    } catch (requestError) {
      setError(requestError.message)
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-card">
        <h1>Welcome back</h1>
        <p>Log in to NutriWell to track your nutrition and healthy habits.</p>
        <form onSubmit={login}>
          <label>Username<input name="username" value={form.username} onChange={updateField} required /></label>
          <label>Password<input type="password" name="password" value={form.password} onChange={updateField} required /></label>
          <button type="submit">Log in</button>
        </form>
        {error && <p className="error" role="alert">{error}</p>}
        <p className="auth-switch">Don’t have an account? <Link to="/signup">Create one</Link></p>
      </section>
    </main>
  )
}

export default Login

