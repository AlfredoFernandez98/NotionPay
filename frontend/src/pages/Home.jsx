import '../styles/Home.css'

const Home = () => {
  return (
    <div className="home-page">
      <div className="hero">
        <h1>Welcome to NotionPay</h1>
        <p>A modern subscription billing and payment platform</p>
        <div className="hero-buttons">
          <button className="btn-primary">Get Started</button>
          <button className="btn-secondary">Learn More</button>
        </div>
      </div>
      
      <div className="features">
        <div className="feature-card">
          <h3>🔒 Secure Payments</h3>
          <p>Powered by Stripe for secure payment processing</p>
        </div>
        <div className="feature-card">
          <h3>📊 Subscription Management</h3>
          <p>Flexible plans and billing periods</p>
        </div>
        <div className="feature-card">
          <h3>💬 SMS Products</h3>
          <p>Purchase and manage SMS packages</p>
        </div>
      </div>
    </div>
  )
}

export default Home
