import { useState } from 'react'
import Dashboard from './Dashboard.jsx'
import Nutrition from './Nutrition.jsx'
import { getCurrentUserId } from '../utils/currentUser.js'
import './Home.css'

const tabs = [
  { id: 'dashboard', label: 'Dashboard' },
  { id: 'nutrition', label: 'Nutrition' },
]

function Home() {
  const [userId] = useState(getCurrentUserId)
  const [activeTab, setActiveTab] = useState('dashboard')

  return (
    <div className="home-page">
      <header className="home-header">
        <span className="home-brand">NutriWell</span>
        <nav className="home-tabs">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              id={`${tab.id}-tab`}
              type="button"
              className="home-tab"
              onClick={() => setActiveTab(tab.id)}
            >
              {tab.label}
            </button>
          ))}
        </nav>
      </header>

      <main className="home-content">
        <section
          id="dashboard-panel"
          hidden={activeTab !== 'dashboard'}
        >
          <Dashboard />
        </section>
        <section
          id="nutrition-panel"
          hidden={activeTab !== 'nutrition'}
        >
          <Nutrition userId={userId} />
        </section>
      </main>
    </div>
  )
}

export default Home
