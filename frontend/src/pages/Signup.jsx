import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'

function Signup() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ username: '', email: '', password: '' })
  const [error, setError] = useState('')

  function updateField(event) {
    setForm({ ...form, [event.target.name]: event.target.value })
  }

  async function signup(event) {
    event.preventDefault()
    setError('')

    try {
      const response = await fetch('http://localhost:8080/api/auth/signup', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form),
      })
      const data = await response.json()
      if (!response.ok) {
        throw new Error(data.error || 'Unable to create account.')
      }
      navigate('/home')
    } catch (requestError) {
      setError(requestError.message)
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-card">
        <h1>Join NutriWell</h1>
        <p>Create an account to track your nutrition and healthy habits.</p>
        <form onSubmit={signup}>
          <label>Username<input name="username" value={form.username} onChange={updateField} required minLength="3" /></label>
          <label>Email<input type="email" name="email" value={form.email} onChange={updateField} required /></label>
          <label>Password<input type="password" name="password" value={form.password} onChange={updateField} required minLength="8" /></label>
          <button type="submit">Create account</button>
        </form>
        {error && <p className="error" role="alert">{error}</p>}
        <p className="auth-switch">Already have an account? <Link to="/login">Log in</Link></p>
      </section>
    </main>
  )
}

export default Signup

