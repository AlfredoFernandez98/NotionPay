import '../styles/Dashboard.css'

const Dashboard = () => {
  return (
    <div className="dashboard-page">
      <h1>Dashboard</h1>
      <div className="dashboard-grid">
        <div className="dashboard-card">
          <h3>Subscription Status</h3>
          <p>Active Plan: Pro</p>
          <p>Next Billing: Jan 1, 2026</p>
        </div>
        
        <div className="dashboard-card">
          <h3>Recent Payments</h3>
          <p>No recent payments</p>
        </div>
        
        <div className="dashboard-card">
          <h3>SMS Balance</h3>
          <p>Remaining: 1000 SMS</p>
        </div>
      </div>
    </div>
  )
}

export default Dashboard
